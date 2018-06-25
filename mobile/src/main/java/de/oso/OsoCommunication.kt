package de.oso

import android.location.Location
import android.os.AsyncTask
import android.util.Log
import android.widget.EditText
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
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
        val obj = URL(baseUrl, "emergency/emit")
        val data = JSONObject()
                .put("latitude", location?.latitude)
                .put("longitude", location?.longitude)
                .toString()

        Log.d("COMM", "Sending<$data> to<$obj>")

        object : AsyncTask<Unit, Unit, Unit>() {
            override fun doInBackground(vararg params: Unit?) {
                with(obj.openConnection() as HttpURLConnection) {
                    // optional default is GET
                    requestMethod = "POST"

                    BufferedWriter(OutputStreamWriter(outputStream)).use {
                        it.write(data)
                    }

                    Log.d("COMM", "got response<$responseCode> with message<$responseMessage>")

                    BufferedReader(InputStreamReader(inputStream)).use {
                        val response = StringBuffer()

                        var inputLine = it.readLine()
                        while (inputLine != null) {
                            response.append(inputLine)
                            inputLine = it.readLine()
                        }

                        Log.d("COMM", "read response<$response>")
                    }
                }
            }
        }.execute()
    }
}