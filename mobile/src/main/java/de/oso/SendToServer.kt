package de.oso

import android.util.Log
import java.net.MalformedURLException
import java.net.URL

object SendToServerSingleton{
    lateinit var locManager: LocManager

    init {
        println("init complete")
    }

    fun send() {
        val loc = locManager.lastLocation
        //val txt = findViewById<EditText>(R.id.editText)
        val url = "51.38.113.244"  //txt.text.toString()
        val port = 8080;

        Log.d("GUI", "trying sending loc<$loc> to url<$url>")
        //loc ?: return;


        try {
            if (url.startsWith("http")) {
                HttpCommManager(URL(url)).sendLocation(loc);
            } else {
                HttpCommManager(URL("http", url, port, "")).sendLocation(loc)
            }
        }
        catch(e: MalformedURLException) {
            Log.d("GUI", "malformed URL<$url>", e)
        }
    }
}