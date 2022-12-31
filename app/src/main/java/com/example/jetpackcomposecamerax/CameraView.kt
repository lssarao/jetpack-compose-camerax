package com.example.jetpackcomposecamerax

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import kotlin.coroutines.resume


private const val FILENAME = "dd-MM-yy-HH-mm-ss-SSS"
private const val PHOTO_EXTENSION = ".jpg"

@Composable
fun CameraView(
    outputDirectory: File,
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    // current context
    val context = LocalContext.current
    // current lifecycle owner
    val lifecycleOwner = LocalLifecycleOwner.current
    // Creating a new preview object using the default configuration
    val preview = Preview.Builder().build()
    // Selecting the back-facing camera
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    val previewView = remember { PreviewView(context) }
    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    // UI
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // embedding an native android view
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopStart)
                .padding(
                    top = 20.dp,
                    start = 20.dp,
                    end = 20.dp
                )
        )
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.Bottom
        ) {
            IconButton(
                modifier = Modifier.padding(bottom = 20.dp),
                onClick = {
                    takePhoto(
                        imageCapture = imageCapture,
                        outputDirectory = outputDirectory,
                        executor = executor,
                        onImageCaptured = onImageCaptured,
                        onError = onError
                    )
                    Log.i("On Click", "Button Pressed")
                },
                content = {

                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_lens_24),
                        contentDescription = "Take picture",
                        tint = Color.White,
                        modifier = Modifier
                            .size(80.dp)
                            .border(1.dp, Color.White, CircleShape)
                    )
                }
            )
        } // Column end
    } // Box end
}

fun takePhoto(
    outputDirectory: File,
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    // Time stamped name and MediaStore entry
    val photoFile = File(
        outputDirectory,
        SimpleDateFormat(
            FILENAME,
            Locale.US
        ).format(System.currentTimeMillis()) + PHOTO_EXTENSION
    )
    // Output options object which contains file + metadata
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    // Set up image capture listener, which is triggered after photo has been taken
    imageCapture.takePicture(
        outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                Log.e("Take Picture", "Take photo error:", exception)
                onError(exception)
            }

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                onImageCaptured(savedUri)
                Log.e("Take Picture", "Photo capture succeeded: $savedUri")
            }
        }
    )
}

// Getting an instance of the ProcessCameraProvider with default configuration
suspend fun Context.getCameraProvider(): ProcessCameraProvider {
    return suspendCancellableCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(
                    cameraProvider.get()
                )
            }, ContextCompat.getMainExecutor(this))
        }
    }
}

// Error ui composable function
@Composable
fun ErrorView() {
    Box(contentAlignment = Alignment.Center) {
        Text(
            text = "Something Went Wrong, Try Again!",
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
        )
    }
}