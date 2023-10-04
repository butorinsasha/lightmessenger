package local.pushkin.lightmessenger

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private var isFlashOn = false
    private var morseCodeMessage = "SOS" // Change this to your Morse code message
    private val dotDuration = 100L // Duration of a dot in milliseconds
    private val dashDuration = dotDuration * 3 // Duration of a dash (3 dots)
    private val spaceBetweenSymbols = dotDuration // Space between dots and dashes
    private val spaceBetweenLetters = dotDuration * 3 // Space between letters
    private val spaceBetweenWords = dotDuration * 7 // Space between words

    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.startButton)

        // Check for camera permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
            }
        }

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList[1]

        startButton.setOnClickListener(View.OnClickListener {
            if (isFlashOn) {
                stopMorseCode()
            } else {
                startMorseCode()
            }
        })
    }

    private fun flashOn() {
        try {
            cameraManager.setTorchMode(cameraId, true)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun flashOff() {
        try {
            cameraManager.setTorchMode(cameraId, false)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun startMorseCode() {
        Toast.makeText(
            /* context = */     this,
            /* text = */        "startMorseCode is going to be executed now",
            /* duration = */    Toast.LENGTH_SHORT).show()
        isFlashOn = true
        val morseCodeThread = thread(start = true) {
            for (symbol in morseCodeMessage) {
                when (enCodeMorse(symbol)) {
                    "...." -> {
                        flashOn()
                        handler.postDelayed({ flashOff() }, dotDuration)
                        Thread.sleep(dotDuration + spaceBetweenSymbols)
                    }
                    "---" -> {
                        flashOn()
                        handler.postDelayed({ flashOff() }, dashDuration)
                        Thread.sleep(dashDuration + spaceBetweenSymbols)
                    }
                }
            }
            isFlashOn = false
        }
    }

    private fun stopMorseCode() {
        isFlashOn = false
    }

    private fun enCodeMorse(char: Char): String {
        return when (char) {
            's', 'S' -> "..."
            'o', 'O' -> "---"
            else -> "***"
        }
    }

    private fun deCodeMorse(morseEncodedMessage: String): String {
        val messageBuilder = StringBuilder()
        val byTriplet = Regex("[.+]{3}")
        val matches = byTriplet.findAll(morseEncodedMessage)

        matches.forEach { triplet ->
            when(triplet.toString()) {
                "---" -> messageBuilder.append("s")
                "..." -> messageBuilder.append("o")
            }
        }
        return messageBuilder.toString()
    }
}
