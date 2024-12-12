package com.example.uts_vego

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sqrt

class PopActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var shakeThreshold = 7.0f  // Threshold for shake detection
    private var lastShakeTime: Long = 0
    private var lastAccelX = 0f
    private var lastAccelY = 0f
    private var lastAccelZ = 0f
    private var isPromoCodeShown = false  // Flag to track if promo code dialog is shown

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pop)

        // Initialize SensorManager and Accelerometer
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculate the change in acceleration (delta) since the last reading
            val deltaX = Math.abs(x - lastAccelX)
            val deltaY = Math.abs(y - lastAccelY)
            val deltaZ = Math.abs(z - lastAccelZ)

            val gForce = deltaX + deltaY + deltaZ  // Sum of deltas

            // Log the gForce value for debugging
            println("gForce: $gForce")

            // Check if the shake is strong enough and if the promo code dialog is not shown
            if (gForce > shakeThreshold && !isPromoCodeShown) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastShakeTime > 1000) { // Prevent multiple triggers in short time
                    lastShakeTime = currentTime
                    println("Shake detected!")
                    showPromoCodeDialog()
                }
            }

            // Save the current accelerometer readings for the next event
            lastAccelX = x
            lastAccelY = y
            lastAccelZ = z
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used for this implementation
    }

    private fun showPromoCodeDialog() {
        val promoCode = generatePromoCode()
        isPromoCodeShown = true  // Set flag to true when the dialog is shown

        AlertDialog.Builder(this)
            .setTitle("Your Promo Code")
            .setMessage("Here is your exclusive promo code: $promoCode")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                isPromoCodeShown = false  // Reset flag when user presses OK
            }
            .create()
            .show()
    }

    private fun generatePromoCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8)
            .map { chars.random() }
            .joinToString("")
    }
}
