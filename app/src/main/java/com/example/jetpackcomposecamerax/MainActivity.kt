package com.example.jetpackcomposecamerax

import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.jetpackcomposecamerax.ui.theme.JetpackComposeCameraXTheme
import java.io.File
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    // To store and retrieve the state of the camera
    private var shouldShowCamera: MutableState<Boolean> = mutableStateOf(false)

    // Permission Launcher - prompted initially
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i("Permission", "Camera Permission granted")
            shouldShowCamera.value = true
        } else {
            Log.i("Permission", "Camera Permission denied")
        }
    }

    private lateinit var outputDirectory: File

    // Executor in which the takePicture callback methods will be run
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    private lateinit var photoUri: Uri
    private var shouldShowPhoto: MutableState<Boolean> = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetpackComposeCameraXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.primaryVariant
                ) {
                    if (shouldShowCamera.value) {
                        CameraView(
                            outputDirectory = outputDirectory,
                            executor = cameraExecutor,
                            onImageCaptured = {},
                            onError = { Log.e("onError", "ImageCapture error:", it) }
                        )
                    } else {
                        ErrorView()
                    }
                }
                requestCameraPermission()
                outputDirectory = getOutputDirectory()
            }
        }
    }

    // Permission Check - prompted every time when app start
    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("Permission Check", "Camera permission previously granted")
                shouldShowCamera.value = true
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                CAMERA
            ) -> Log.i("Permission Check", "Show camera permissions dialog")

            else -> requestPermissionLauncher.launch(CAMERA)
        }
    }

    // Creating a new folder in external storage if it’s not created already
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, PATH_NAME).apply { mkdirs() }
        }

        return if ((mediaDir != null) && mediaDir.exists()) {
            Log.i("Folder", mediaDir.path); mediaDir
        } else {
            filesDir
        }

    }

    // shutdown the cameraExecutor service which will stop all actively executing tasks
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val PATH_NAME = "JC CameraX"
    }
}