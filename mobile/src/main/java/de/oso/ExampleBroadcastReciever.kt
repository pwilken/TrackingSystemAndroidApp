package de.oso

import android.content.Context
import android.util.Log
import io.flic.lib.FlicBroadcastReceiver
import io.flic.lib.FlicButton
import io.flic.lib.FlicManager

class ExampleBroadcastReceiver : FlicBroadcastReceiver() {

    protected override fun onRequestAppCredentials(context: Context) {
        // Set app credentials by calling FlicManager.setAppCredentials here
        FlicManager.setAppCredentials("31eda4d5-302d-49b0-8d69-5ab86dc1325a",
                "a4d85782-0bc6-419f-a993-77e999ae9be3",
                "OSOAndroid");
    }

    override fun onButtonUpOrDown(context: Context, button: FlicButton, wasQueued: Boolean,
                                  timeDiff: Int, isUp: Boolean, isDown: Boolean) {
        if (isUp) {
            // Code for button up event here
            Log.d("FLIC", "button up")

        } else {
            // Code for button down event here
            Log.d("FLIC", "button down")
            SendToServerSingleton.send()
        }
    }

    override fun onButtonRemoved(context: Context, button: FlicButton) {
        // Button was removed
    }
}