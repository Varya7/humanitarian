package com.example.hum1

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.hum1.MapActivityC.Companion.LOCATION_PERMISSION_REQUEST_CODE
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
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
import com.yandex.mapkit.map.MapObjectCollection
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

class MapActivity : AppCompatActivity(), UserLocationObjectListener, Session.SearchListener, CameraListener, DrivingSession.DrivingRouteListener {
    lateinit var mapview: MapView

    lateinit var jambut: Button
    lateinit var locationmapkit: UserLocationLayer
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    lateinit var searchManager: SearchManager
    lateinit var searchSession: Session
    private var ROUTE_START_LOCATION = Point(43.414663, 39.950500)
    private var ROUTE_END_LOCATION = Point(47.214004, 39.794605)
    private val SCREEN_CENTER = Point(
        (ROUTE_START_LOCATION.latitude+ROUTE_END_LOCATION.latitude)/2,
        (ROUTE_START_LOCATION.longitude+ROUTE_END_LOCATION.longitude)/2)
    private var mapObjects:MapObjectCollection? = null
    private var drivingRouter: DrivingRouter? = null
    private var drivingSession:DrivingSession? = null
    private var latitude: Double = -1.0
    private var longitude: Double = -1.0
    private var latitudeM: Double = -1.0
    private var longitudeM: Double = -1.0

    private fun submitQuery(query: String) {
        searchSession = searchManager.submit(
            query,
            VisibleRegionUtils.toPolygon(mapview.map.visibleRegion),
            SearchOptions(),
            this
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("3c89017d-c56c-4694-b14e-3085f7402ed4")
        MapKitFactory.initialize(this)
        enableEdgeToEdge()
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        setContentView(R.layout.activity_map)
        mapview = findViewById(R.id.mapview)
        jambut = findViewById(R.id.jambut)


        latitudeM = intent.getDoubleExtra("latitude", 0.0)
        longitudeM = intent.getDoubleExtra("longitude", 0.0)
        ROUTE_END_LOCATION = Point(latitudeM, longitudeM)

        mapview.map.move(
            CameraPosition(Point(51.744059, 36.192162), 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 10f), null
        )
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
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                MapActivityC.LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getLastKnownLocation()
        }



        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter()
        mapObjects = mapview.map.mapObjects.addCollection()
        submitRequest()
    }


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
                        ROUTE_START_LOCATION = Point(latitude, longitude)
                    }
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
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
        /*
        val mapObjects: MapObjectCollection = mapview.map.mapObjects
        //mapObjects.clear()
        for (searchResult in response.collection.children) {
            val resultLocation = searchResult.obj!!.geometry[0].point!!
            if (response != null) {
                mapObjects.addPlacemark(
                    resultLocation,
                    ImageProvider.fromResource(this, R.drawable.search_result)
                )
            }
        }
        */

    }

    override fun onSearchError(error: Error) {
        /*
        var errorMessage = "Неизвестная Ошибка!"
        if (error is RemoteError) {
            errorMessage = "Беспроводная ошибка!"
        } else if (error is NetworkError) {
            errorMessage = "Проблема с интернетом!"
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }

         */
    }


    override fun onCameraPositionChanged(
        map: Map,
        cameraPosition: CameraPosition,
        cameraUpdateReason: CameraUpdateReason,
        finished: Boolean
    ) {
        //if(finished){
        //    submitQuery(searchEdit.text.toString())
        // }
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


    private fun submitRequest() {
        val drivingOptions = DrivingOptions()
        val vehicleOptions = VehicleOptions()
        val requestPoints: ArrayList<RequestPoint> = ArrayList()
        requestPoints.add(RequestPoint(ROUTE_START_LOCATION, RequestPointType.WAYPOINT, null))
        requestPoints.add(RequestPoint(ROUTE_END_LOCATION,RequestPointType.WAYPOINT, null))
        drivingSession = drivingRouter!!.requestRoutes(requestPoints, drivingOptions, vehicleOptions, this)
    }

}
