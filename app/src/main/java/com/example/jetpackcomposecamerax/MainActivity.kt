package com.example.jetpackcomposecamerax

import android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
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
            // Camera ON
            shouldShowCamera.value = true
            //
            requestExternalStoragePermissions()
        } else {
            Log.i("Permission", "Camera Permission denied")
        }
    }

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
                    color = MaterialTheme.colors.background
                ) {
                    when {
                        shouldShowCamera.value -> {
                            CameraView(
                                executor = cameraExecutor,
                                onImageCaptured = { afterImageCapture(it) },
                                onError = { Log.e("onError", "ImageCapture error:", it) }
                            )
                        }
                        shouldShowPhoto.value -> {
                            PhotoView(
                                uri = photoUri,
                                afterPreview = { restartCamera() })
                        }
                        else -> {
                            ErrorView()
                        }
                    }
                }
                requestCameraPermission()
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

    //
    private fun requestExternalStoragePermissions() {
        val intent = Intent()
        intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION

        val uri = Uri.fromParts("package", this.packageName, null)

        intent.data = uri

        startActivity(intent)
    }

    // changing view from CameraView to PhotoView
    private fun afterImageCapture(uri: Uri) {
        // Camera OFF
        shouldShowCamera.value = false
        // passing uri
        photoUri = uri
        // External storage permissions check
        if (Environment.isExternalStorageManager()) {
            // Photo view ON
            shouldShowPhoto.value = true
            Log.i("Capturing URI", "Capturing URI: $uri")
        } else {
            requestExternalStoragePermissions()
            Log.i("StoragePermissions", "Ask Storage Permissions")
        }
    }

    private fun restartCamera() {
        // Camera ON
        shouldShowCamera.value = true
    }

    // shutdown the cameraExecutor service which will stop all actively executing tasks
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}