package de.oso

import android.Manifest
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import java.net.HttpURLConnection
import java.net.URL
import android.location.LocationManager
import android.location.LocationListener
import android.location.Location
import android.util.Log
import java.io.*
import android.os.StrictMode




class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val btn = findViewById<Button>(R.id.button)

        btn.setOnClickListener( {
            Log.d("Tag", "button clicked")
            sendData()
        })
        // POST /emergency JSON latitute longitute
    }

    fun sendData() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val locationListener = MyLocationListener()

        try {
            val loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            //val latitute = loc.latitude
            //val longitute = loc.longitude

            //Log.d("Tag", "long:$longitute, lang:$latitute")
            send(""""{longitute":"12.34","latitute":"56.78"}""")
            //locationManager.requestLocationUpdates(
            //        LocationManager.GPS_PROVIDER, 5000, 10f, locationListener)
        }
        catch(e: SecurityException) {
            Log.d("Tag", "exception")
        }
    }

    fun send(data: String) {

        val txt = findViewById<EditText>(R.id.editText)
        val url = txt.text.toString()
        val obj = URL(url)

        with(obj.openConnection() as HttpURLConnection) {
            // optional default is GET
            requestMethod = "GET"


            Log.d("Tag", "\nSending 'GET' request to URL : $url")
            Log.d("Tag", "Response Code : $responseCode")


            BufferedReader(InputStreamReader(inputStream)).use {
                val response = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                Log.d("Tag", "Response:${response.toString()}")
            }
        }
    }

    private inner class MyLocationListener:LocationListener {
        override fun onLocationChanged(loc:Location) {

            val latitute = loc.latitude
            val longitute = loc.longitude

            Log.d("Tag", "long:$longitute, lang:$latitute")

            val data = """"{"latitute":"$latitute","longitute":"$longitute"}"""

            send(data);

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
    }

    override fun onProviderDisabled(provider:String) {}

    override fun onProviderEnabled(provider:String) {}

    override fun onStatusChanged(provider:String, status:Int, extras:Bundle) {}
    }
}
