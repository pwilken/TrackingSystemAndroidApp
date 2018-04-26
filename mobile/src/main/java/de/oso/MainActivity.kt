package de.oso

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import java.net.URL
import java.net.MalformedURLException


class MainActivity : AppCompatActivity() {
    lateinit var locManager: LocManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locManager = LocManagerGooglePlayServices(this)

        setContentView(R.layout.activity_main)

        val btn = findViewById<Button>(R.id.button)

        btn.setOnClickListener( {
            Log.d("GUI", "button clicked")
            send()
        })
    }

    fun send() {
        val loc = locManager.lastLocation
        val txt = findViewById<EditText>(R.id.editText)
        val url = txt.text.toString()

        Log.d("GUI", "trying sending loc<$loc> to url<$url>")
        loc ?: return;

        try {
            if (url.startsWith("http")) {
                HttpCommManager(URL(url)).sendLocation(loc);
            } else {
                HttpCommManager(URL("http", url, "")).sendLocation(loc)
            }
        }
        catch(e: MalformedURLException) {
            Log.d("GUI", "malformed URL<$url>", e)
            logInGui("malformedUrl<$url>")
        }
    }

    fun logInGui(msg: String) {
        val log = findViewById<EditText>(R.id.txtLog)

        log.text.append(System.lineSeparator()).append(msg)
    }

}