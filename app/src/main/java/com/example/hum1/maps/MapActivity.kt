package com.example.hum1.maps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.hum1.LocaleUtil
import com.example.hum1.R
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
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
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
 * Активити для отображения карты с использованием Yandex MapKit и получения маршрута между двумя точками.
 * Поддерживает отображение пробок, определение текущего местоположения пользователя,
 * поиск по карте и построение маршрутов для автомобиля.
 */
class MapActivity : AppCompatActivity(), UserLocationObjectListener, Session.SearchListener, CameraListener, InputListener, DrivingSession.DrivingRouteListener {

    lateinit var mapview: MapView
    lateinit var jambut: Button
    private lateinit var useGpsButton: Button
    private lateinit var chooseStartButton: Button
    private lateinit var openYandexButton: Button
    lateinit var locationmapkit: UserLocationLayer
    private lateinit var locationManager: LocationManager
    private var locationListener: LocationListener? = null

    lateinit var searchManager: SearchManager
    lateinit var searchSession: Session
    private var ROUTE_START_LOCATION = Point(51.737062, 36.189906)
    private var ROUTE_END_LOCATION = Point(47.214004, 39.794605)
    public var mapObjects: MapObjectCollection? = null
    public var drivingRouter: DrivingRouter? = null
    private var drivingSession: DrivingSession? = null
    private var latitude: Double = -1.0
    private var longitude: Double = -1.0
    private var latitudeM: Double = -1.0
    private var longitudeM: Double = -1.0
    private var waitingForStartPoint: Boolean = false

    companion object {
        private var isMapKitInitialized = false
    }

    /**
     * Инициализация активити, установка ключа API, инициализация карты,
     * запрос разрешений и инициализация компонентов для работы с картой и маршрутизацией
     * @param savedInstanceState Состояние активити при создании
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleUtil.initAppLocale(this)
        super.onCreate(savedInstanceState)

        YandexMapKitInitializer.init(this)

        enableEdgeToEdge()
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        setContentView(R.layout.activity_map)
        mapview = findViewById(R.id.mapview)
        jambut = findViewById(R.id.jambut)
        useGpsButton = findViewById(R.id.btn_use_gps)
        chooseStartButton = findViewById(R.id.btn_choose_route_start)
        openYandexButton = findViewById(R.id.btn_open_yandex_maps)

        latitudeM = intent.getDoubleExtra("latitude", 0.0)
        longitudeM = intent.getDoubleExtra("longitude", 0.0)
        ROUTE_END_LOCATION = Point(latitudeM, longitudeM)

        mapview.map.move(
            CameraPosition(Point(51.744059, 36.192162), 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 10f), null
        )

        val mapKit: MapKit = MapKitFactory.getInstance()
        val traffic_jam = mapKit.createTrafficLayer(mapview.mapWindow)
        traffic_jam.isTrafficVisible = true

        jambut.setOnClickListener {
            if (traffic_jam.isTrafficVisible == false) {
                traffic_jam.isTrafficVisible = true
                jambut.setBackgroundResource(R.drawable.simpleblue)
            } else {
                traffic_jam.isTrafficVisible = false
                jambut.setBackgroundResource(R.drawable.blueoff)
            }
        }

        useGpsButton.setOnClickListener { askGpsAndUseCurrentPoint() }
        chooseStartButton.setOnClickListener {
            waitingForStartPoint = true
            Toast.makeText(this, getString(R.string.map_tap_start), Toast.LENGTH_SHORT).show()
        }
        openYandexButton.setOnClickListener { openInYandexMaps() }

        locationmapkit = mapKit.createUserLocationLayer(mapview.mapWindow)
        locationmapkit.isVisible = true
        locationmapkit.setObjectListener(this)

        SearchFactory.initialize(this)
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        mapview.map.addCameraListener(this)
        mapview.map.addInputListener(this)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter()
        mapObjects = mapview.map.mapObjects.addCollection()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MapActivityC.LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            askGpsAndUseCurrentPoint()
        }

        submitRequest()
    }

    private fun askGpsAndUseCurrentPoint() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.map_gps_title))
                .setMessage(getString(R.string.map_gps_message))
                .setPositiveButton(getString(R.string.map_gps_enable)) { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton(getString(android.R.string.cancel), null)
                .show()
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MapActivityC.LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        getLastKnownLocation()
        startLocationUpdates()
    }

    private fun openInYandexMaps() {
        if (latitudeM == 0.0 && longitudeM == 0.0) {
            Toast.makeText(this, getString(R.string.error_coordinates_unavailable), Toast.LENGTH_SHORT).show()
            return
        }
        val routeText = if (latitude != -1.0 && longitude != -1.0) {
            "$latitude,$longitude~$latitudeM,$longitudeM"
        } else {
            "$latitudeM,$longitudeM"
        }
        val appUri = if (routeText.contains("~")) {
            Uri.parse("yandexmaps://maps.yandex.ru/?rtext=$routeText&rtt=auto")
        } else {
            Uri.parse("yandexmaps://maps.yandex.ru/?pt=$longitudeM,$latitudeM&z=16")
        }
        val appIntent = Intent(Intent.ACTION_VIEW, appUri).setPackage("ru.yandex.yandexmaps")
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://yandex.ru/maps/?rtext=$routeText&rtt=auto"))
        try {
            startActivity(appIntent)
        } catch (e: Exception) {
            startActivity(webIntent)
        }
    }

    /**
     * Запрашивает разрешение на определение местоположения у пользователя,
     * если оно еще не предоставлено.
     */
    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 0)
            return
        }
    }

    /**
     * Обрабатывает событие остановки активности — останавливает MapKit и MapView,
     * а также прекращает обновление местоположения.
     */
    override fun onStop() {
        stopLocationUpdates()
        mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    /**
     * Обрабатывает событие старта активности — запускает MapKit и MapView.
     */
    override fun onStart() {
        MapKitFactory.getInstance().onStart()
        mapview.onStart()
        super.onStart()
    }

    /**
     * Получает последнее известное местоположение устройства через LocationManager.
     * Устанавливает начальную точку маршрута.
     */
    private fun getLastKnownLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                val providers = locationManager.getProviders(true)
                var bestLocation: Location? = null

                for (provider in providers) {
                    val location = locationManager.getLastKnownLocation(provider)
                    if (location != null) {
                        if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                            bestLocation = location
                        }
                    }
                }

                bestLocation?.let { location ->
                    latitude = location.latitude
                    longitude = location.longitude
                    ROUTE_START_LOCATION = Point(latitude, longitude)
                    submitRequest()
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * Запускает активное обновление местоположения через GPS провайдер.
     */
    private fun startLocationUpdates() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            locationListener = LocationListener { location ->
                latitude = location.latitude
                longitude = location.longitude
                ROUTE_START_LOCATION = Point(latitude, longitude)
                submitRequest()
            }

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                10f,
                locationListener!!
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * Прекращает обновление местоположения.
     */
    private fun stopLocationUpdates() {
        locationListener?.let {
            locationManager.removeUpdates(it)
            locationListener = null
        }
    }

    /**
     * Обрабатывает результат запроса разрешений.
     * При успешном предоставлении разрешения запрашивает последнее местоположение.
     * @param requestCode Код запроса разрешения
     * @param permissions Запрошенные разрешения
     * @param grantResults Результаты разрешений
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MapActivityC.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                askGpsAndUseCurrentPoint()
            }
        }
    }

    /**
     * Срабатывает при добавлении объекта текущего местоположения на карту.
     * Настраивает иконки и стили отображения пользователя.
     * @param userLocationView Вид текущего местоположения пользователя на карте
     */
    override fun onObjectAdded(userLocationView: UserLocationView) {
        mapview.post {
            val width = mapview.width.toFloat()
            val height = mapview.height.toFloat()
            locationmapkit.setAnchor(
                PointF(width * 0.5f, height * 0.5f),
                PointF(width * 0.5f, height * 0.83f)
            )
        }

        userLocationView.arrow.setIcon(ImageProvider.fromResource(this, R.drawable.user_arrow))
        userLocationView.pin.setIcon(ImageProvider.fromResource(this, R.drawable.nothing))

        // Настройка круга точности
        userLocationView.accuracyCircle.fillColor = Color.argb(45, 66, 133, 244)
        userLocationView.accuracyCircle.strokeColor = Color.argb(100, 66, 133, 244)
        userLocationView.accuracyCircle.strokeWidth = 1f
    }

    override fun onObjectRemoved(p0: UserLocationView) {

    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {

    }

    override fun onSearchResponse(response: Response) {

    }

    override fun onSearchError(error: Error) {

    }

    override fun onCameraPositionChanged(
        map: Map,
        cameraPosition: CameraPosition,
        cameraUpdateReason: CameraUpdateReason,
        finished: Boolean
    ) {

    }

    override fun onMapTap(map: Map, point: Point) {
        if (!waitingForStartPoint) {
            return
        }
        waitingForStartPoint = false
        latitude = point.latitude
        longitude = point.longitude
        ROUTE_START_LOCATION = point
        Toast.makeText(this, getString(R.string.map_start_selected), Toast.LENGTH_SHORT).show()
        submitRequest()
    }

    override fun onMapLongTap(map: Map, point: Point) {
        waitingForStartPoint = true
        onMapTap(map, point)
    }

    /**
     * Обрабатывает получение маршрутов от роутера.
     * Добавляет линии маршрута на карту.
     * @param routes Список маршрутов для отображения
     */
    override fun onDrivingRoutes(p0: MutableList<DrivingRoute>) {
        for (route in p0) {
            mapObjects?.addPolyline(route.geometry)
        }
    }

    /**
     * Обрабатывает ошибки построения маршрутов.
     * @param error Объект ошибки
     */
    override fun onDrivingRoutesError(p0: Error) {
        Toast.makeText(this, getString(R.string.error_unknown), Toast.LENGTH_SHORT).show()
    }

    /**
     * Отправляет запрос на построение маршрута между начальной и конечной точками.
     */
    public fun submitRequest() {
        if (drivingRouter == null) {
            return
        }
        if (latitudeM == 0.0 && longitudeM == 0.0) {
            Toast.makeText(this, getString(R.string.error_coordinates_unavailable), Toast.LENGTH_SHORT).show()
            return
        }
        val drivingOptions = DrivingOptions()
        val vehicleOptions = VehicleOptions()
        val requestPoints: ArrayList<RequestPoint> = ArrayList()
        mapObjects?.clear()
        requestPoints.add(RequestPoint(ROUTE_START_LOCATION, RequestPointType.WAYPOINT, null, null))
        requestPoints.add(RequestPoint(ROUTE_END_LOCATION, RequestPointType.WAYPOINT, null, null))
        drivingSession = drivingRouter!!.requestRoutes(requestPoints, drivingOptions, vehicleOptions, this)
    }
}
