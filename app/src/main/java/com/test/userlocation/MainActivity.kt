package com.test.userlocation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.example.example.RouteResponseModel
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.layers.addLayerAbove
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.test.userlocation.Bird.BirdInterface
import com.test.userlocation.Bird.BirdModel
import com.test.userlocation.Routes.CustomRoute
import com.test.userlocation.Routes.RouteCustomResponse
import com.test.userlocation.Routes.RouteInterface
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.ref.WeakReference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private var userLongitude : Double = 0.0
    private var userLatitude : Double = 0.0



    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
        // Rotate the map by 90 degrees
        rotateMap(90.0)
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
        // Rotate the map by 90 degrees
        rotateMap(90.0)

        userLatitude = it.latitude()
        userLongitude = it.longitude()

    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accessToken = getString(R.string.YOUR_MAPBOX_ACCESS_TOKEN)
        val resourceOptions = ResourceOptions.Builder()
            .accessToken(accessToken)
            .build()

        mapView = MapView(this, MapInitOptions(this, resourceOptions))

        setContentView(mapView)
        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }
    }
    private fun onMapReady() {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
            setupGesturesListener()
            fetchHotspotsInRegion()
        }
    }

    private fun getDirections() {

    }


    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(
                    this@MainActivity,
                    R.drawable.img
                ),
                shadowImage = AppCompatResources.getDrawable(
                    this@MainActivity,
                    R.drawable.img
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
    }
    private fun rotateMap(angle: Double) {
        val cameraOptions = CameraOptions.Builder()
            .bearing(angle)
            .build()
        mapView.getMapboxMap().setCamera(cameraOptions)
    }

    private fun onCameraTrackingDismissed() {
        Toast.makeText(this, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    private fun fetchHotspotsInRegion() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.ebird.org/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val birdService = retrofit.create(BirdInterface::class.java)
        val regionCode = "NL" // Adjust this as needed
        val back = 30
        val fmt = "json"
        val apiKey = "cgg84aonm2ti" // Update with your API key

        val call = birdService.getHotspotsInRegion(regionCode, back, fmt, apiKey)
        call.enqueue(object : Callback<List<BirdModel>> {
            override fun onResponse(
                call: Call<List<BirdModel>>,
                response: Response<List<BirdModel>>
            ) {
                if (response.isSuccessful) {
                    val hotspots = response.body()
                    if (hotspots != null) {
                        Log.e("API success", "-->$hotspots")
                        addHotspotsToMap(hotspots)
                    } else {
                        Log.e("API Error", "Response body is null")
                    }
                } else {
                    Log.e("API Error", "API request failed with status code: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<BirdModel>>, t: Throwable) {
                // Handle the failure here, e.g., display an error message
                Log.e("API Error", "API request failed with exception: $t")
            }
        })
    }


    private fun addHotspotsToMap(hotspots: List<BirdModel>) {

        for (hotspot in hotspots) {

            bitmapFromDrawableRes(
                this@MainActivity,
                R.drawable.img
            )?.let {
                val hotspotLongitude = hotspot.longitude ?: 0.0
                val hotspotLatitude = hotspot.latitude ?: 0.0

                val annotationApi = mapView.annotations
                val pointAnnotationManager = annotationApi.createPointAnnotationManager()
                val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(hotspotLongitude, hotspotLatitude))
                    .withIconImage(it)
                pointAnnotationManager.create(pointAnnotationOptions)

                PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(hotspotLongitude, hotspotLatitude))
                    .withIconImage("img")
                    .withIconSize(0.3)
                //    .withTextField(hotspot.name ?: "")   Display Hotspot name on map. uncode if u want that. it displays in popup
                pointAnnotationManager.create(pointAnnotationOptions)
            }
        }
    }

    private fun addAnnotationToMap() {
        bitmapFromDrawableRes(
            this@MainActivity,
            R.drawable.img
        )?.let {
            val annotationApi = mapView.annotations
            val pointAnnotationManager = annotationApi.createPointAnnotationManager()
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(28.80, -30.36))
                .withIconImage(it)
            pointAnnotationManager.create(pointAnnotationOptions)

            PointAnnotationOptions()
                .withPoint(Point.fromLngLat(28.80, -30.36))
                .withIconImage("img")
                .withIconSize(0.3)
            //    .withTextField(hotspot.name ?: "")   Display Hotspot name on map. uncode if u want that. it displays in popup
            pointAnnotationManager.create(pointAnnotationOptions)

        }
    }
    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
    // copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }


}

private fun <T> Call<T>.enqueue(callback: Callback<RouteCustomResponse>) {
    TODO("Not yet implemented")
}


