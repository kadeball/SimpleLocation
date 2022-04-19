package com.bignerdranch.android.simplelocation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bignerdranch.android.simplelocation.PermissionUtils.requestAccessFineLocationPermission
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null

    companion object {
        fun newInstance() = MainFragment()
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
    }

    private lateinit var latTextView: TextView
    private lateinit var longTextView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.main_fragment, container, false)

        // WIRE UP WIDGETS
        latTextView = view.findViewById(R.id.lat_text_view)
        longTextView = view.findViewById(R.id.long_text_view)
        mapView = view.findViewById(R.id.mapView)

        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    private fun setUpLocationListener() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)

        // CREATE A HIGH-ACCURACY LOCATIONREQUEST THAT CHECKS LOCATION EVERY 2 SECONDS AT HIGH ACCURACY

        val locationRequest = LocationRequest()
            .setInterval(2000)
            .setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)



        // PASS THAT LOCATIONREQUEST OBJECT TO THE FUSEDLOCATIONPROVIDER
        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
            object: LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)

                    for (location in locationResult!!.locations) {
                        latTextView.text = location.latitude.toString()
                        longTextView.text = location.longitude.toString()

                        googleMap?.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)).title("ET Building"))
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))
                    }
                }
            },
            Looper.myLooper()

        )

        // WHEN WE GET A LOCATION BACK FROM THE PROVIDER...

    }

    override fun onStart() {
        super.onStart()

        // CHECK TO SEE IF THE USER HAS GIVEN US PERMISSION TO USE THEIR LOCATION
        if (PermissionUtils.isAccessFineLocationGranted(context!!)) {
            // CHECK TO SEE IF THE GPS ON THE DEVICE IS ENABLED
            if (PermissionUtils.isLocationEnabled(context!!)) {
                setUpLocationListener()
            }
            // IF NO GPS, SHOW A DIALOG
            else {
                PermissionUtils.showGPSNotEnabledDialog(context!!)
            }
        }
        // IF NO PERMISSIONS, ASK FOR PERMISSION
        else {
            requestAccessFineLocationPermission(this, LOCATION_PERMISSION_REQUEST_CODE)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // CHECK TO SEE IF THE RESPONSE IS FOR THE PERMISSION CHECK WE MADE (999)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {

            // CHECK TO SEE IF THE USER HAS GIVEN US PERMISSION TO USE THEIR LOCATION
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                // CHECK TO SEE IF THE GPS ON THE DEVICE IS ENABLED
                if (PermissionUtils.isLocationEnabled((context!!))) {
                    setUpLocationListener()
                }
                // IF NO GPS, SHOW A DIALOG
                else {
                    PermissionUtils.showGPSNotEnabledDialog(context!!)
                }

            }

            // IF NO PERMISSIONS, ASK FOR PERMISSION
            else{
                Toast.makeText(context!!,
                    getString(R.string.location_permission_not_granted),
                    Toast.LENGTH_LONG).show()
            }

        }
    }

    override fun onMapReady(gMap: GoogleMap) {

        googleMap = gMap


        googleMap?.moveCamera(CameraUpdateFactory.zoomTo(15.0F))


        mapView.onResume()
    }
}