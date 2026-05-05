package com.example.hum1.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.hum1.LocaleUtil
import com.example.hum1.R
import com.example.hum1.ui.CenterListActivity
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError

/**
 * Активность для работы с картой на базе Yandex MapKit.
 * Обеспечивает отображение карты, поиск локаций,
 * получение текущего местоположения пользователя,
 * отображение трафика и выбор точки на карте.
 */
class MapActivityC : AppCompatActivity(), UserLocationObjectListener, Session.SearchListener, CameraListener, DrivingSession.DrivingRouteListener {

    lateinit var mapview: MapView
    private lateinit var map: Map
    private lateinit var locationManager: LocationManager
    private var locationListener: LocationListener? = null

    lateinit var jambut: Button
    lateinit var contbut: Button
    lateinit var locationmapkit: UserLocationLayer
    lateinit var searchEdit: EditText

    private lateinit var searchManager: SearchManager
    lateinit var searchSession: Session

    private var mapObjects: MapObjectCollection? = null
    private var drivingRouter: DrivingRouter? = null
    private var drivingSession: DrivingSession? = null
    private var latitude: Double = 55.700581
    private var longitude: Double = 37.520630

    private var latitudeM: Double = -1.0
    private var longitudeM: Double = -1.0

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 123
        private var isMapKitInitialized = false
    }

    /**
     * Отправляет поисковый запрос на основании введённого текста и текущего видимого региона карты.
     * @param query Текст поискового запроса.
     */
    private fun submitQuery(query: String) {
        if (query.isNotEmpty()) {
            searchSession = searchManager.submit(
                query,
                VisibleRegionUtils.toPolygon(mapview.map.visibleRegion),
                SearchOptions(),
                this
            )
        }
    }

    /**
     * Метод жизненного цикла Activity, вызывается при создании.
     * Инициализирует MapKit, элементы интерфейса,
     * проверяет и запрашивает разрешения на локацию,
     * настраивает слушатели и элементы управления.
     */
    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleUtil.initAppLocale(this)
        super.onCreate(savedInstanceState)

        YandexMapKitInitializer.init(this)

        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        setContentView(R.layout.activity_map_c)
        enableEdgeToEdge()

        val intent = intent
        val centerName = intent.getStringExtra("center_name")
        val address = intent.getStringExtra("address")
        val email = intent.getStringExtra("email")
        val password = intent.getStringExtra("password")
        val fio = intent.getStringExtra("fio")
        val workTime = intent.getStringExtra("work_time")
        val workStart = intent.getStringExtra("work_start")
        val workEnd = intent.getStringExtra("work_end")
        val interval = intent.getStringExtra("appointment_interval_minutes")
        val workingDays = intent.getStringExtra("working_days")
        val phoneNumber = intent.getStringExtra("phone_number")
        val doc = intent.getStringExtra("doc")

        mapview = findViewById(R.id.mapview)
        jambut = findViewById(R.id.jambut)
        contbut = findViewById(R.id.continueButton)
        searchEdit = findViewById(R.id.search_edit)

        map = mapview.map

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

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
            startLocationUpdates()
        }

        placeUserMarker()

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

        contbut.setOnClickListener {
            if (latitudeM == -1.0 || longitudeM == -1.0) {
                Toast.makeText(this, getString(R.string.error_select_location), Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, CenterListActivity::class.java)
                intent.putExtra("center_name", centerName)
                intent.putExtra("address", address)
                intent.putExtra("email", email)
                intent.putExtra("password", password)
                intent.putExtra("fio", fio)
                intent.putExtra("work_time", workTime)
                intent.putExtra("work_start", workStart)
                intent.putExtra("work_end", workEnd)
                intent.putExtra("appointment_interval_minutes", interval)
                intent.putExtra("working_days", workingDays)
                intent.putExtra("phone_number", phoneNumber)
                intent.putExtra("doc", doc)
                intent.putExtra("latitude", latitudeM)
                intent.putExtra("longitude", longitudeM)
                startActivity(intent)
                finish()
            }
        }

        locationmapkit = mapKit.createUserLocationLayer(mapview.mapWindow)
        locationmapkit.isVisible = true
        locationmapkit.setObjectListener(this)

        SearchFactory.initialize(this)
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        mapview.map.addCameraListener(this)

        map.addInputListener(inputListener)

        searchEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                submitQuery(searchEdit.text.toString())
            }
            false
        }

        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter()
        mapObjects = mapview.map.mapObjects.addCollection()
    }

    /**
     * Обработчик касания по карте для установки маркера.
     * Позволяет пользователю выбрать точку на карте.
     */
    val inputListener = object : InputListener {
        private var currentPlacemark: PlacemarkMapObject? = null

        override fun onMapTap(map: Map, point: Point) {
            currentPlacemark?.let {
                map.mapObjects.remove(it)
            }

            currentPlacemark = map.mapObjects.addPlacemark(
                point,
                ImageProvider.fromResource(applicationContext, R.drawable.user_arrow)
            )
            currentPlacemark?.setIcon(
                ImageProvider.fromResource(applicationContext, R.drawable.user_arrow)
            )
            currentPlacemark?.let { showCoordinates(it.geometry) }
        }

        override fun onMapLongTap(map: Map, point: Point) {
            currentPlacemark?.let {
                map.mapObjects.remove(it)
            }

            currentPlacemark = map.mapObjects.addPlacemark(
                point,
                ImageProvider.fromResource(applicationContext, R.drawable.search_result)
            )
            currentPlacemark?.setIcon(
                ImageProvider.fromResource(applicationContext, R.drawable.search_result)
            )
            currentPlacemark?.let { showCoordinates(it.geometry) }
        }

        /**
         * Показывает координаты выбранной точки и сохраняет их.
         * @param geometry Точка с координатами.
         */
        private fun showCoordinates(geometry: Point) {
            if (geometry is Point) {
                latitudeM = geometry.latitude
                longitudeM = geometry.longitude
            }
        }
    }

    /**
     * Перемещает камеру карты на координаты пользователя.
     */
    private fun placeUserMarker() {
        if (latitude != -1.0 && longitude != -1.0) {
            mapview.map.move(
                CameraPosition(Point(latitude, longitude), 14.0f, 0.0f, 0.0f),
                Animation(Animation.Type.SMOOTH, 1.0f),
                null
            )
        }
    }

    /**
     * Получает последнее известное местоположение пользователя через LocationManager.
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
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * Запускает активное обновление местоположения.
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

    override fun onStop() {
        stopLocationUpdates()
        mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        MapKitFactory.getInstance().onStart()
        mapview.onStart()
        super.onStart()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation()
                startLocationUpdates()
            }
        }
    }

    /**
     * Настраивает пользовательский маркер на карте при добавлении.
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
        userLocationView.accuracyCircle.fillColor = Color.argb(30, 66, 133, 244)
        userLocationView.accuracyCircle.strokeColor = Color.argb(80, 66, 133, 244)
        userLocationView.accuracyCircle.strokeWidth = 1f
    }

    override fun onObjectRemoved(p0: UserLocationView) {}

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {}

    /**
     * Обрабатывает ответ поискового запроса, отображая найденные точки на карте.
     */
    override fun onSearchResponse(response: Response) {
        val mapObjects: MapObjectCollection = mapview.map.mapObjects
        for (searchResult in response.collection.children) {
            val resultLocation = searchResult.obj?.geometry?.firstOrNull()?.point
            if (resultLocation != null) {
                mapObjects.addPlacemark(
                    resultLocation,
                    ImageProvider.fromResource(this, R.drawable.search_result)
                )
            }
        }
    }

    /**
     * Обрабатывает ошибку поискового запроса.
     * Выводит сообщение об ошибке пользователю.
     */
    override fun onSearchError(error: Error) {
        var errorMessage = getString(R.string.error_unknown)
        if (error is RemoteError) {
            errorMessage = getString(R.string.error_remote)
        } else if (error is NetworkError) {
            errorMessage = getString(R.string.error_network)
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    /**
     * Вызывается при изменении положения камеры на карте.
     * Если движение камеры завершено, отправляет поисковый запрос.
     */
    override fun onCameraPositionChanged(
        map: Map,
        cameraPosition: CameraPosition,
        cameraUpdateReason: CameraUpdateReason,
        finished: Boolean
    ) {
        if (finished && searchEdit.text.toString().isNotEmpty()) {
            submitQuery(searchEdit.text.toString())
        }
    }

    /**
     * Обрабатывает успешный ответ с маршрутом.
     * Отображает маршрут на карте.
     */
    override fun onDrivingRoutes(p0: MutableList<DrivingRoute>) {
        for (route in p0) {
            mapObjects!!.addPolyline(route.geometry)
        }
    }

    /**
     * Обрабатывает ошибку при построении маршрута.
     */
    override fun onDrivingRoutesError(p0: Error) {
        Toast.makeText(this, getString(R.string.error_unknown), Toast.LENGTH_SHORT).show()
    }
}
