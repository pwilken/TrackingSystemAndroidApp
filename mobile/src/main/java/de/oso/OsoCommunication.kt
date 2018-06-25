package de.oso

import android.location.Location
import android.os.AsyncTask
import android.util.Log
import khttp.post
import java.net.URL

interface CommManager {
    fun sendLocation(location: Location?)
}

class HttpCommManager(val baseUrl: URL) : CommManager {
    init {
        Log.d("COMM", "creating HttpCommManager using baseUrl<$baseUrl>")
    }

    override fun sendLocation(location: Location?) {
        Log.d("OSO", "Start sending location.")

        object : AsyncTask<Unit, Unit, Unit>() {
            override fun doInBackground(vararg params: Unit?) {
                post(
                    "${baseUrl}/emergency/emit",
                    json = mapOf(
                        "helpRequesterId" to 2,
                        mapOf(
                            "latitude" to location?.latitude,
                            "longitude" to location?.longitude
                        ) to "coordinates"
                    )
                )
            }
        }.execute()
    }
}