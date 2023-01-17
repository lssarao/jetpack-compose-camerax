package com.example.jetpackcomposecamerax

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
import androidx.core.net.toUri
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
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
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    // Current context
    val context = LocalContext.current
    // Current lifecycle owner
    val lifecycleOwner = LocalLifecycleOwner.current
    // Creating a new preview object using the default configuration
    val preview = Preview.Builder().build()
    // Selecting the back-facing camera
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()
    // Implementing camerax's preview
    val previewView = remember { PreviewView(context) }
    // take picture
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
                        executor = executor,
                        onImageCaptured = onImageCaptured,
                        onError = onError
                    )
                    Log.i("CameraViewButton", "Button Pressed")
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
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    // Output directory to save captured image
    val outputDirectory = File(
        Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM
        ), "Camera"
    )

    // Time stamped name and MediaStore entry
    val photoFile = File(
        outputDirectory,
        "CameraX-" + SimpleDateFormat(
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
                Log.e("TakePicture", "Take photo error:", exception)
                onError(exception)
            }

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                onImageCaptured(savedUri)
                Log.e("TakePicture", "Photo capture succeeded: $savedUri")
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

// Photo screen
@Composable
fun PhotoView(uri: Uri, afterPreview: () -> Unit) {
    val ctx = LocalContext.current
    Log.i("PhotoView", "URI Passing to painter: $uri")
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(uri)
            .build(),
        onState = { state: AsyncImagePainter.State ->
            Log.d("PreviewCheck", "PhotoView: $state")
            if (state is AsyncImagePainter.State.Error) {
                Log.d("PreviewCheck", "PhotoView: ${state.result.throwable}")
            }
        }
    )

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.padding(20.dp))

        Image(
            painter = painter,
            contentDescription = "Captured Image",
            modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp)
        )

        OutlinedButton(
            onClick = afterPreview,
            colors = ButtonDefaults.buttonColors(Color.White),
            shape = RoundedCornerShape(10.dp),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 10.dp,
                pressedElevation = 15.dp
            )
        ) {
            Text(
                text = "Take Photo",
                color = Color.Blue
            )
        }
        Log.i("PhotoViewButton", "Button Pressed")

        // Changing uri parameter text
        val changedURI = uri.toString().replace("file", "content").toUri()

        OutlinedButton(
            onClick = {
                // Creating an intent with send action
                val i = Intent(Intent.ACTION_SEND)

                //passing uri
                i.putExtra(Intent.EXTRA_STREAM, changedURI)
                Log.i("Intent", "Passing URI:$changedURI ")
                // setting type of intent
                i.type = "image/jpeg"

                //starting activity to open applications.
                ctx.startActivity(i)
            },
            colors = ButtonDefaults.buttonColors(Color.White),
            shape = RoundedCornerShape(10.dp),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 10.dp,
                pressedElevation = 15.dp
            )
        ) {
            Text(
                text = "Share Photo",
                color = Color.Blue,
            )
        }
    }
}

// Error screen
@Composable
fun ErrorView() {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Spacer(modifier = Modifier.padding(20.dp))

            Text(
                text = "We need permission to access your device's storage for saving and previewing the image.",
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.padding(20.dp))

            LinearProgressIndicator(
                color = Color.Blue
            )
        }
    }
}