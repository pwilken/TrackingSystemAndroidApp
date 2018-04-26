package de.oso

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import com.google.android.gms.location.*

interface LocManager {
    val lastLocation: Location?
}

class LocManagerPrimitive(val context: Context) : LocManager {
    override var lastLocation: Location? = null
        get() {
            var bestLocation: Location? = null

            locationManager.allProviders.forEach({

                try {
                    val l = locationManager.getLastKnownLocation(it)
                    Log.d("LOC", l.toString())
                    bestLocation = isBetterLocation(l, bestLocation)
                }
                catch(e: SecurityException) {
                    // TODO Exception-Handling
                }

            })

            return bestLocation
        }
    lateinit var locationManager: LocationManager

    init {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        /*
        val locationListener = MyLocationListener()

        locationManager.allProviders.forEach( {
                Log.d("LOC", "Provider<$it>")
                locationManager.requestLocationUpdates(it, 0, 0f, locationListener)
        })
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
        */
    }

    protected fun isBetterLocation(location: Location,
                                   currentBestLocation: Location?): Location {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return location
        }

        // Check whether the new location fix is newer or older
        val timeDelta = location.time - currentBestLocation.time
        val isSignificantlyNewer = timeDelta > (1000 * 60)
        val isSignificantlyOlder = timeDelta < -(1000 * 60)
        val isNewer = timeDelta > 0

        // If it's been more than two minutes since the current location, use
        // the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return location
            // If the new location is more than two minutes older, it must be
            // worse
        } else if (isSignificantlyOlder) {
            return currentBestLocation
        }

        // Check whether the new location fix is more or less accurate
        val accuracyDelta = (location.accuracy - currentBestLocation
                .accuracy).toInt()
        val isLessAccurate = accuracyDelta > 0
        val isMoreAccurate = accuracyDelta < 0
        val isSignificantlyLessAccurate = accuracyDelta > 200

        // Check if the old and new location are from the same provider
        val isFromSameProvider = isSameProvider(location.provider,
                currentBestLocation.provider)

        // Determine location quality using a combination of timeliness and
        // accuracy
        if (isMoreAccurate) {
            return location
        } else if (isNewer && !isLessAccurate) {
            return location
        } else if (isNewer && !isSignificantlyLessAccurate
                && isFromSameProvider) {
            return location
        }
        return currentBestLocation
    }

    private fun isSameProvider(provider1: String?, provider2: String?): Boolean {
        return if (provider1 == null) {
            provider2 == null
        } else provider1 == provider2
    }

    private inner class MyLocationListener: LocationListener {
        override fun onLocationChanged(loc: Location) {
            var bestLocation: Location? = lastLocation

            locationManager.allProviders.forEach({

                try {
                    val l = locationManager.getLastKnownLocation(it)
                    Log.d("LOC", l.toString())
                    bestLocation = isBetterLocation(l, bestLocation)
                }
                catch(e: SecurityException) {
                    // TODO Exception-handling
                }

            })
            //bestLocation = isBetterLocation(gps, net)
            //bestLocation = isBetterLocation(bestLocation, lastLocation)
            if (bestLocation != null)
                lastLocation = bestLocation
        }


            // LOCATION TO ADDRESS
            //val loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            /*
            Toast.makeText(
                    baseContext,
                    "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                    + loc.getLongitude(), Toast.LENGTH_SHORT).show()
            val longitude = "Longitude: " + loc.getLongitude()
            Log.v(FragmentActivity.TAG, longitude)
            val latitude = "Latitude: " + loc.getLatitude()
            Log.v(FragmentActivity.TAG, latitude)

             /*------- To get city name from coordinates -------- */
                    var cityName:String? = null
            val gcd = Geocoder(baseContext, Locale.getDefault())
            val addresses:List<Address>
            try
            {
            addresses = gcd.getFromLocation(loc.getLatitude(),
            loc.getLongitude(), 1)
            if (addresses.size > 0)
            {
            System.out.println(addresses[0].getLocality())
            cityName = addresses[0].getLocality()
            }
            }
            catch (e:IOException) {
            e.printStackTrace()
            }

            val s = (longitude + "\n" + latitude + "\n\nMy Current City is: "
            + cityName)
            editLocation.setText(s)
*/
        override fun onProviderDisabled(provider:String) {}

        override fun onProviderEnabled(provider:String) {}

        override fun onStatusChanged(provider:String, status:Int, extras: Bundle) {}
    }
}

class LocManagerGooglePlayServices(val context: Context) : LocManager, LifecycleObserver {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override var lastLocation: Location? = null

    init {
        Log.d("LOC", "creating locmanager using google play services")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                Log.d("LOC", "new location result<$locationResult>")
                locationResult ?: return
                lastLocation = locationResult.locations.minBy { it.elapsedRealtimeNanos }
            }
        }

        if(context is MainActivity) {
            Log.d("LOC", "installing lifecycle-observer")
            context.lifecycle.addObserver(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun startLocationUpdates() {
        Log.d("LOC", "start location-update")
        if(context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("LOC", "permission granted")
            val locationRequest = LocationRequest()
            // Use high accuracy
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            // Set the update interval to 5 seconds
            locationRequest.setInterval(5000);
            // Set the fastest update interval to 1 second
            locationRequest.setFastestInterval(1000);

            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null /* Looper */)
        } else {
            Log.d("LOC", "permission denied")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun stopLocationUpdates() {
        Log.d("LOC", "stop location-update")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}