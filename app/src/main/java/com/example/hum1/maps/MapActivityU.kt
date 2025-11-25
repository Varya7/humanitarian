package com.example.hum1.maps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.hum1.R
import com.example.hum1.views.ViewCenter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider

/**
 * Активити для отображения карты со всеми одобренными центрами помощи и построения маршрутов к ним
 */
class MapActivityU : AppCompatActivity(), UserLocationObjectListener, Session.SearchListener,
    CameraListener, DrivingSession.DrivingRouteListener, MapObjectTapListener {

    private lateinit var mapview: MapView
    private lateinit var jambut: Button
    private lateinit var locationmapkit: UserLocationLayer
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var searchManager: SearchManager

    private var ROUTE_START_LOCATION = Point(55.700581, 37.520630)
    private val SCREEN_CENTER = Point(51.744059, 36.192162)

    private var mapObjects: MapObjectCollection? = null
    private var centerMarkers: MutableList<PlacemarkMapObject> = mutableListOf()
    private var drivingRouter: DrivingRouter? = null
    private var drivingSession: DrivingSession? = null

    private var latitude: Double = -1.0
    private var longitude: Double = -1.0

    private val centers = mutableListOf<CenterInfo>()
    private var selectedCenter: CenterInfo? = null

    private lateinit var database: FirebaseDatabase

    /**
     * Класс для хранения информации о центре помощи
     */
    data class CenterInfo(
        val id: String,
        val name: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val phone: String,
        val email: String,
        val workTime: String,
        val status: String,
        val fio: String,
        val doc: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("3c89017d-c56c-4694-b14e-3085f7402ed4")
        MapKitFactory.initialize(this)
        enableEdgeToEdge()
        supportActionBar?.hide()

        setContentView(R.layout.activity_map_u)
        mapview = findViewById(R.id.mapview)
        jambut = findViewById(R.id.jambut)

        database = FirebaseDatabase.getInstance()

        mapview.map.move(
            CameraPosition(SCREEN_CENTER, 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 10f), null
        )

        requestLocationPermission()

        val mapKit: MapKit = MapKitFactory.getInstance()
        val trafficLayer = mapKit.createTrafficLayer(mapview.mapWindow)
        trafficLayer.isTrafficVisible = true

        jambut.setOnClickListener {
            if (trafficLayer.isTrafficVisible) {
                trafficLayer.isTrafficVisible = false
                jambut.setBackgroundResource(R.drawable.blueoff)
            } else {
                trafficLayer.isTrafficVisible = true
                jambut.setBackgroundResource(R.drawable.simpleblue)
            }
        }

        locationmapkit = mapKit.createUserLocationLayer(mapview.mapWindow)
        locationmapkit.isVisible = false
        locationmapkit.setObjectListener(this)

        SearchFactory.initialize(this)
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        mapview.map.addCameraListener(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getLastKnownLocation()
        }

        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter()
        mapObjects = mapview.map.mapObjects.addCollection()

        loadCentersFromFirebase()
    }

    /**
     * Загружает данные о центрах из Firebase Realtime Database
     */
    private fun loadCentersFromFirebase() {
        val usersRef = database.getReference("Users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                centers.clear()
                centerMarkers.forEach { it.parent?.remove(it) }
                centerMarkers.clear()

                var totalCenters = 0
                var approvedCenters = 0

                for (userSnapshot in dataSnapshot.children) {
                    val role = userSnapshot.child("role").getValue(String::class.java)

                    if (role == "center") {
                        totalCenters++
                        val status = userSnapshot.child("status").getValue(String::class.java)

                        if (status != null && (status == "Одобрено" || status.equals("одобрено", ignoreCase = true))) {
                            approvedCenters++
                            val center = parseCenterFromSnapshot(userSnapshot)
                            center?.let {
                                centers.add(it)
                                addCenterToMap(it)
                            }
                        }
                    }
                }

                println("Total centers: $totalCenters, Approved: $approvedCenters, Added to map: ${centers.size}")

                if (centers.isEmpty()) {
                    Toast.makeText(this@MapActivityU, "Нет одобренных центров помощи", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@MapActivityU, "Загружено ${centers.size} центров", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@MapActivityU, "Ошибка загрузки центров: ${databaseError.message}", Toast.LENGTH_LONG).show()
                // Повторяем попытку через 2 секунды
                mapview.postDelayed({
                    loadCentersFromFirebase()
                }, 2000)
            }
        })
    }

    /**
     * Парсит данные центра из снимка Firebase
     */
    private fun parseCenterFromSnapshot(snapshot: DataSnapshot): CenterInfo? {
        try {
            val id = snapshot.key ?: return null

            val name = snapshot.child("center_name").getValue(String::class.java) ?: "Неизвестно"
            val address = snapshot.child("address").getValue(String::class.java) ?: "Адрес не указан"
            val phone = snapshot.child("phone_number").getValue(String::class.java) ?: "Телефон не указан"
            val email = snapshot.child("email").getValue(String::class.java) ?: "Email не указан"
            val workTime = snapshot.child("work_time").getValue(String::class.java) ?: "Время работы не указано"
            val status = snapshot.child("status").getValue(String::class.java) ?: "Неизвестно"
            val fio = snapshot.child("fio").getValue(String::class.java) ?: "Контактное лицо не указано"
            val doc = snapshot.child("doc").getValue(String::class.java) ?: "Документы не указаны"

            val latitude = when {
                snapshot.child("latitude").value is Double -> snapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                snapshot.child("latitude").value is String -> snapshot.child("latitude").getValue(String::class.java)?.toDoubleOrNull() ?: 0.0
                snapshot.child("latitude").value is Long -> (snapshot.child("latitude").getValue(Long::class.java) ?: 0).toDouble()
                else -> 0.0
            }

            val longitude = when {
                snapshot.child("longitude").value is Double -> snapshot.child("longitude").getValue(Double::class.java) ?: 0.0
                snapshot.child("longitude").value is String -> snapshot.child("longitude").getValue(String::class.java)?.toDoubleOrNull() ?: 0.0
                snapshot.child("longitude").value is Long -> (snapshot.child("longitude").getValue(Long::class.java) ?: 0).toDouble()
                else -> 0.0
            }

            if (latitude == 0.0 && longitude == 0.0) {
                println("Center $name has zero coordinates, skipping")
                return null
            }

            println("Successfully parsed center: $name at ($latitude, $longitude)")

            return CenterInfo(
                id = id,
                name = name,
                address = address,
                latitude = latitude,
                longitude = longitude,
                phone = phone,
                email = email,
                workTime = workTime,
                status = status,
                fio = fio,
                doc = doc
            )
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error parsing center: ${e.message}")
            return null
        }
    }

    /**
     * Добавляет центр на карту в виде маркера с иконкой
     */
    private fun addCenterToMap(center: CenterInfo) {
        val point = Point(center.latitude, center.longitude)


        val placemark = mapObjects?.addPlacemark(
            point,
            ImageProvider.fromResource(this, R.drawable.search_result)
        )

        placemark?.let {

            it.setIcon(
                ImageProvider.fromResource(this, R.drawable.search_result),
                IconStyle().setAnchor(PointF(0.5f, 0.5f))
                    .setRotationType(RotationType.NO_ROTATION)
                    .setZIndex(1f)
                    .setScale(1.0f)
            )
            it.userData = center
            it.addTapListener(this@MapActivityU)
            centerMarkers.add(it)

            println("Added marker for center: ${center.name} at (${center.latitude}, ${center.longitude})")
        }
    }

    /**
     * Обрабатывает нажатие на маркер центра - сразу открываем ViewCenter
     */
    override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean {
        val centerInfo = mapObject.userData as? CenterInfo
        centerInfo?.let { center ->
            // Сразу открываем ViewCenter с полной информацией
            val intent = Intent(this, ViewCenter::class.java)
            intent.putExtra("id", center.id)
            startActivity(intent)
        }
        return true
    }

    /**
     * Строит маршрут до выбранного центра
     */
    private fun buildRouteToCenter(center: CenterInfo) {
        if (latitude == -1.0 || longitude == -1.0) {
            Toast.makeText(this, "Не удалось определить ваше местоположение", Toast.LENGTH_SHORT).show()
            getLastKnownLocation()
            return
        }

        ROUTE_START_LOCATION = Point(latitude, longitude)
        val routeEndLocation = Point(center.latitude, center.longitude)


        mapObjects?.clear()

        centers.forEach { addCenterToMap(it) }

        val drivingOptions = DrivingOptions()
        val vehicleOptions = VehicleOptions()
        val requestPoints = arrayListOf(
            RequestPoint(ROUTE_START_LOCATION, RequestPointType.WAYPOINT, null),
            RequestPoint(routeEndLocation, RequestPointType.WAYPOINT, null)
        )

        drivingSession = drivingRouter?.requestRoutes(requestPoints, drivingOptions, vehicleOptions, this)
    }

    /**
     * Обрабатывает получение маршрутов
     */
    override fun onDrivingRoutes(routes: MutableList<DrivingRoute>) {
        for (route in routes) {
            mapObjects?.addPolyline(route.geometry).apply {
                this?.setStrokeColor(Color.BLUE)
                this?.strokeWidth = 5f
            }
        }

        selectedCenter?.let { center ->
            Toast.makeText(this, "Маршрут до ${center.name} построен", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDrivingRoutesError(error: Error) {
        Toast.makeText(this, "Ошибка построения маршрута", Toast.LENGTH_SHORT).show()
    }

    /**
     * Получает последнее известное местоположение
     */
    private fun getLastKnownLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful && task.result != null) {
                            val location = task.result
                            latitude = location.latitude
                            longitude = location.longitude
                            ROUTE_START_LOCATION = Point(latitude, longitude)
                        }
                    }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation()
            }
        }
    }

    // Остальные методы интерфейсов...
    override fun onObjectAdded(userLocationView: UserLocationView) {
        locationmapkit.setAnchor(
            PointF((mapview.width * 0.5).toFloat(), (mapview.height * 0.5).toFloat()),
            PointF((mapview.width * 0.5).toFloat(), (mapview.height * 0.83).toFloat())
        )
        userLocationView.arrow.setIcon(ImageProvider.fromResource(this, R.drawable.user_arrow))
        val picIcon = userLocationView.pin.useCompositeIcon()
        picIcon.setIcon(
            "icon",
            ImageProvider.fromResource(this, R.drawable.search_result),
            IconStyle().setAnchor(PointF(0f, 0f))
                .setRotationType(RotationType.ROTATE)
                .setZIndex(0f)
                .setScale(1f)
        )
        picIcon.setIcon(
            "pin",
            ImageProvider.fromResource(this, R.drawable.nothing),
            IconStyle().setAnchor(PointF(0.5f, 0.5f))
                .setRotationType(RotationType.ROTATE)
                .setZIndex(1f)
                .setScale(0.5f)
        )
        userLocationView.accuracyCircle.fillColor = Color.BLUE and -0x66000001
    }

    override fun onObjectRemoved(p0: UserLocationView) {}
    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {}
    override fun onSearchResponse(response: Response) {}
    override fun onSearchError(error: Error) {}
    override fun onCameraPositionChanged(
        map: Map,
        cameraPosition: CameraPosition,
        cameraUpdateReason: CameraUpdateReason,
        finished: Boolean
    ) {}

    override fun onStop() {
        mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        mapview.onStart()
        MapKitFactory.getInstance().onStart()
        super.onStart()
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }
}