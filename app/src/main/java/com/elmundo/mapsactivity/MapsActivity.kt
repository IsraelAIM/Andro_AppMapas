package com.elmundo.mapsactivity

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.elmundo.mapsactivity.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val permisoFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permisoCourseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION

    private val CODIGO_SOLICITUD_PERMISO = 100
    var fusedLocationClient: FusedLocationProviderClient? = null
    var locationRequest: LocationRequest? = null
    var callback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = FusedLocationProviderClient(this)
        inicializarLocationRequest()

        callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (ubicacion in locationResult?.locations!!) {

                    if (mMap !=null){
                        mMap
                        Toast.makeText(
                            applicationContext,
                            ubicacion.latitude.toString() + " , " + ubicacion.longitude.toString(),
                            Toast.LENGTH_SHORT
                        ).show()

                        val position = LatLng(ubicacion.latitude, ubicacion.longitude)
                        mMap.addMarker(MarkerOptions().position(position).title("Aqui estoy"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(position))
                    }

                }
            }
        }

        /// Aqui yase el mapa ////
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
    }

    override fun onStart() {
        super.onStart()
        if (validarPermisosUbicacion()) {
            obtenerUbicacion()
        } else {
            pedirPermisos()
        }
    }
    override fun onPause() {
        super.onPause()
        detenerActualizacionUbicacion()
    }


    /// INICIO FUNCIONES PERMISOS
    private fun inicializarLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest?.interval = 10000
        locationRequest?.fastestInterval = 5000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun validarPermisosUbicacion(): Boolean {
        val hayUbicacionPrecisa = ActivityCompat.checkSelfPermission(
            this,
            permisoFineLocation
        ) == PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria = ActivityCompat.checkSelfPermission(
            this,
            permisoCourseLocation
        ) == PackageManager.PERMISSION_GRANTED
        return hayUbicacionPrecisa && hayUbicacionOrdinaria

    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion() {
        fusedLocationClient?.requestLocationUpdates(
            locationRequest as LocationRequest,
            callback as LocationCallback, Looper.myLooper()!!
        )


    }

    private fun pedirPermisos() {
        val deboProveerContexto =
            ActivityCompat.shouldShowRequestPermissionRationale(this, permisoFineLocation)
        if (deboProveerContexto) {
            //mandar mensaje con explicacion adicional
            solicitudPermiso()
        } else {
            solicitudPermiso()
        }
    }

    private fun solicitudPermiso() {
        requestPermissions(
            arrayOf(permisoFineLocation, permisoCourseLocation),
            CODIGO_SOLICITUD_PERMISO
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CODIGO_SOLICITUD_PERMISO -> {
                //Granted=0
                //Denied=-1
                if (grantResults.size > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    obtenerUbicacion()
                    Toast.makeText(this, "Diste permiso", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(
                        this,
                        "No diste permiso para acceder a la ubicacion",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }
    }
    private fun detenerActualizacionUbicacion() {
        callback?.let { fusedLocationClient?.removeLocationUpdates(it) }
    }
    ///FIN FUNCIONES PERMISO
    //Se ejecuta cuando está listo

}