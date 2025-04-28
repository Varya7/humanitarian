package com.example.hum1


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.hum1.ui.CenterListActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.location.Location
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
import java.util.concurrent.TimeUnit

class MapActivityC : AppCompatActivity(), UserLocationObjectListener, Session.SearchListener, CameraListener, DrivingSession.DrivingRouteListener {

    lateinit var mapview: MapView

    private lateinit var map: Map
    val LOCATION_REQUEST_CODE = 100
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1



    lateinit var jambut: Button
    lateinit var contbut: Button
    lateinit var locationmapkit: UserLocationLayer
    lateinit var searchEdit: EditText

    private lateinit var searchManager: SearchManager
    lateinit var searchSession: Session


    private var mapObjects:MapObjectCollection? = null
    private var drivingRouter: DrivingRouter? = null
    private var drivingSession:DrivingSession? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var latitude: Double = 51.735475
    private var longitude: Double = 36.190570



    private var latitudeM: Double = -1.0
    private var longitudeM: Double = -1.0
    lateinit var currentLocation: Location




    private fun submitQuery(query: String) {
        searchSession = searchManager.submit(
            query,
            VisibleRegionUtils.toPolygon(mapview.map.visibleRegion),
            SearchOptions(),
            this
        )
    }



    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("3c89017d-c56c-4694-b14e-3085f7402ed4")
        MapKitFactory.initialize(this)
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        setContentView(R.layout.activity_map_c)



        mapview = findViewById(R.id.mapview)
        enableEdgeToEdge()



        jambut = findViewById(R.id.jambut)
        contbut = findViewById(R.id.continueButton)

        map = mapview.map

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                MapActivityC.LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getLastKnownLocation()
        }

        checkLocationPermission()

        locationRequest = LocationRequest().apply {

            interval = TimeUnit.SECONDS.toMillis(60)
            fastestInterval = TimeUnit.SECONDS.toMillis(30)
            maxWaitTime = TimeUnit.MINUTES.toMillis(2)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }


        placeUserMarker()
        requestLocationPermission()
        var mapKit: MapKit = MapKitFactory.getInstance()
        var traffic_jam = mapKit.createTrafficLayer(mapview.mapWindow)
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

        contbut.setOnClickListener{
            if (latitudeM==-1.0 || longitudeM==-1.0){
                Toast.makeText(this, "Выберите локацию", Toast.LENGTH_SHORT).show()
            }
            else{
                val intent = Intent(this, CenterListActivity::class.java)
                intent.putExtra("latitude", latitudeM)
                intent.putExtra("longitude", longitudeM)
                startActivity(intent)
                finish()
            }
        }

        locationmapkit = mapKit.createUserLocationLayer(mapview.mapWindow)
        locationmapkit.isVisible = false

        locationmapkit.setObjectListener(this)
        SearchFactory.initialize(this)

        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        mapview.map.addCameraListener(this)
        searchEdit = findViewById(R.id.search_edit)

        map.addInputListener(inputListener)

        searchEdit.setOnEditorActionListener { y, actionId, event
            ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                submitQuery(searchEdit.text.toString())
            }
            false
        }


        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter()
        mapObjects = mapview.map.mapObjects.addCollection()

    }

    val inputListener = object : InputListener {

        private var currentPlacemark: PlacemarkMapObject? = null
        //private val mapObjects = map.mapObjects
        private lateinit var searchManager: SearchManager


        override fun onMapTap(map: Map, point: Point) {

            currentPlacemark?.let {
                map.mapObjects.remove(it)
                Log.d("MapTap", "Removed previous placemark")
            }

            currentPlacemark = map.mapObjects.addPlacemark(point, ImageProvider.fromResource(applicationContext, R.drawable.user_arrow))
            currentPlacemark?.setIcon(ImageProvider.fromResource(applicationContext, R.drawable.user_arrow))

            currentPlacemark?.let { showCoordinates(it.geometry) }

        }

        override fun onMapLongTap(map: Map, point: Point) {
            currentPlacemark?.let {
                map.mapObjects.remove(it)
                Log.d("MapTap", "Removed previous placemark")
            }

            currentPlacemark = map.mapObjects.addPlacemark(point, ImageProvider.fromResource(applicationContext, R.drawable.search_result))
            currentPlacemark?.setIcon(ImageProvider.fromResource(applicationContext, R.drawable.search_result))
            currentPlacemark?.let { showCoordinates(it.geometry) }

        }




        private fun showCoordinates(geometry: Point) {
            if (geometry is Point) {
                val latitude = geometry.latitude
                val longitude = geometry.longitude
                Toast.makeText(applicationContext, "Latitude: $latitude, Longitude: $longitude", Toast.LENGTH_LONG).show()
                latitudeM = latitude
                longitudeM = longitude
                Log.d("Coordinates", "Latitude: $latitude, Longitude: $longitude")

            } else {
                Log.w("Coordinates", "Unexpected geometry type: ${geometry::class.java.simpleName}")
            }
        }


    }

    private fun placeUserMarker() {
        val userLocation = Point(latitude, longitude)
        //val userMarker = map.mapObjects.addPlacemark(userLocation, ImageProvider.fromResource(this, R.drawable.user_arrow)) // Replace your_marker_icon with your actual icon drawable resource.
        mapview.map.move(
            CameraPosition(Point(latitude, longitude), 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 10f), null
        )

        //userMarker.setIcon(ImageProvider.fromResource(this, R.drawable.user_arrow))
        //userMarker.isDraggable = false // prevent dragging of the marker

    }
    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MapActivityC.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation()
            }
        }
    }

    private fun requestLocationUpdates() {
        if (checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(
                LocationRequest.create().apply {
                    interval = 10000 // Update every 10 seconds
                    fastestInterval = 5000 // Fastest 5 seconds
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                },
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.lastLocation?.let {
                            latitude = it.latitude
                            longitude = it.longitude
                            placeUserMarker()
                        }
                    }
                },
                Looper.getMainLooper()
            )
        } else {
            requestLocationPermission()
        }
    }

    private fun getLastKnownLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful && task.result != null) {
                        val location = task.result
                        latitude = location!!.latitude
                        longitude = location!!.longitude
                    }
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }



    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

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


    override fun onObjectAdded(userLocationView: UserLocationView) {


        locationmapkit.


        setAnchor(
            PointF((mapview.width() *0.5).toFloat(), (mapview.height()*0.5).toFloat()),
            PointF((mapview.width() *0.5).toFloat(), (mapview.height()*0.83).toFloat()))
        userLocationView.arrow.setIcon(ImageProvider.fromResource(this, R.drawable.user_arrow))
        val picIcon = userLocationView.pin.useCompositeIcon()
        picIcon.setIcon("icon", ImageProvider.fromResource(this, R.drawable.search_result), IconStyle().
        setAnchor(PointF(0f, 0f))
            .setRotationType(RotationType.ROTATE).setZIndex(0f).setScale(1f)
        )
        picIcon.setIcon("pin", ImageProvider.fromResource(this, R.drawable.nothing),
            IconStyle().setAnchor(PointF(0.5f, 0.5f)).setRotationType(RotationType.ROTATE).setZIndex(1f).setScale(0.5f))
        userLocationView.accuracyCircle.fillColor = Color.BLUE and -0x66000001
    }

    override fun onObjectRemoved(p0: UserLocationView) {

    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {

    }

    override fun onSearchResponse(response: Response) {
        val mapObjects: MapObjectCollection = mapview.map.mapObjects
        for (searchResult in response.collection.children) {
            val resultLocation = searchResult.obj!!.geometry[0].point!!
            if (response != null) {
                mapObjects.addPlacemark(
                    resultLocation,
                    ImageProvider.fromResource(this, R.drawable.search_result)
                )
            }
        }
    }

    override fun onSearchError(error: Error) {
        var errorMessage = "Неизвестная Ошибка!"
        if (error is RemoteError) {
            errorMessage = "Беспроводная ошибка!"
        } else if (error is NetworkError) {
            errorMessage = "Проблема с интернетом!"
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCameraPositionChanged(
        map: Map,
        cameraPosition: CameraPosition,
        cameraUpdateReason: CameraUpdateReason,
        finished: Boolean
    ) {
        if(finished){
            submitQuery(searchEdit.text.toString())
        }
    }

    override fun onDrivingRoutes(p0: MutableList<DrivingRoute>) {
        for(route in p0) {
            mapObjects!!.addPolyline(route.geometry)
        }
    }

    override fun onDrivingRoutesError(p0: Error) {
        var errorMessage = "Неизвестная ошибка!"
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT)
    }





    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }

    private fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            false
        } else {
            true
        }
    }

}