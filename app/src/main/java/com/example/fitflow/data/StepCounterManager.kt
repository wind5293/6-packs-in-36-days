package com.example.fitflow.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StepCounterManager(
    context: Context,
    private val listener: Listener
) : SensorEventListener {

    interface Listener {
        fun onCounterValue(counterValue: Int)
        fun onStepDetected()
        fun onSensorUnavailable()
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private var activeSensorType: Int? = null

    fun start(): Boolean {
        when {
            stepCounterSensor != null -> {
                activeSensorType = Sensor.TYPE_STEP_COUNTER
                return sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST, 0)
            }
            stepDetectorSensor != null -> {
                activeSensorType = Sensor.TYPE_STEP_DETECTOR
                return sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST, 0)
            }
            else -> {
                activeSensorType = null
                listener.onSensorUnavailable()
                return false
            }
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensorType = event?.sensor?.type ?: return
        when (sensorType) {
            Sensor.TYPE_STEP_COUNTER -> {
                val rawCounter = event.values.firstOrNull()?.toInt() ?: return
                listener.onCounterValue(rawCounter)
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                listener.onStepDetected()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
