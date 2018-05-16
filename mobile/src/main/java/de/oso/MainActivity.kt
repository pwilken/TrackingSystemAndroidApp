package de.oso

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import io.flic.lib.FlicManager
import java.net.URL
import java.net.MalformedURLException
import android.widget.Toast
import io.flic.lib.FlicAppNotInstalledException
import io.flic.lib.FlicManagerInitializedCallback
import io.flic.lib.FlicBroadcastReceiverFlags
import io.flic.lib.FlicButton
import android.content.Intent
import io.flic.lib.FlicBroadcastReceiver



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SendToServerSingleton.locManager = LocManagerGooglePlayServices(this)

        // send button
        val btn = findViewById<Button>(R.id.button)
        btn.setOnClickListener( {
            Log.d("GUI", "button clicked")
            SendToServerSingleton.send()
        })

        // flic button
        FlicManager.setAppCredentials("31eda4d5-302d-49b0-8d69-5ab86dc1325a",
                "a4d85782-0bc6-419f-a993-77e999ae9be3",
                "OSOAndroid");
        getFlicButton()
    }



    fun logInGui(msg: String) {
        val log = findViewById<EditText>(R.id.txtLog)
        log.text.append(System.lineSeparator()).append(msg)
    }

    fun iniFlic() {
        // Replace appId and appSecret with your credentials
        // and appName with a friendly name of your app
        FlicManager.setAppCredentials("31eda4d5-302d-49b0-8d69-5ab86dc1325a",
                "a4d85782-0bc6-419f-a993-77e999ae9be3",
                "OSOAndroid");
    }

    // flic app will appear and the user can choose his flic button
    // the flic manager will take the choosed flic button
    fun getFlicButton() {
        try {
            FlicManager.getInstance(this) { manager -> manager.initiateGrabButton(this@MainActivity) }
        } catch (err: FlicAppNotInstalledException) {
            Toast.makeText(this, "Flic App is not installed", Toast.LENGTH_SHORT).show()
        }
    }

    // To receive the button object, we must feed the result into the manager which then returns
    // the button object. With the button object, we register for notifications. In this example,
    // we’re only interested in down, up and remove events.
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        FlicManager.getInstance(this) { manager ->
            val button = manager.completeGrabButton(requestCode, resultCode, data)
            if (button != null) {
                button.registerListenForBroadcast(FlicBroadcastReceiverFlags.UP_OR_DOWN or FlicBroadcastReceiverFlags.REMOVED)
                Toast.makeText(this@MainActivity, "Grabbed a button", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Did not grab any button", Toast.LENGTH_SHORT).show()
            }
        }
    }
}