package com.example.aicamera

import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.camera.core.*
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.permissions.*
import com.google.mlkit.vision.face.Face
import java.util.concurrent.Executor

@OptIn(ExperimentalPermissionsApi::class)
@ExperimentalGetImage
@Composable
fun CameraView() {
    val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    LaunchedEffect(key1 = Unit) {
        if (!cameraPermissionState.status.isGranted && !cameraPermissionState.status.shouldShowRationale) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (cameraPermissionState.status.isGranted) {
        // Permission is granted, we can show the camera preview
        ExampleCameraPreview()
    } else {
        // In this screen you should notify the user that the permission
        // is required and maybe offer a button to start another
        // camera perission request
        Box(modifier = Modifier.fillMaxSize()){
            Text(text = "no access to camera" , Modifier.align(Alignment.Center))
        }
    }
}
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
private fun  ExampleCameraPreview() {
    val context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }
    val imageAnalayser : ImageAnalyszer = ImageAnalyszer()
    cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA





    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val mainExecutor: Executor = ContextCompat.getMainExecutor(context)
                    cameraController.takePicture(mainExecutor, object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            // Process the captured image here

                            imageAnalayser.analyze(image)
                        }
                    })
                    cameraController.setImageAnalysisAnalyzer(Executor {  }, imageAnalayser)
                }
            ) {
//                val paint = android.graphics.Paint().apply {
//                    // Set other paint properties as desired (e.g., strokeWidth, etc.)
//                    val imageAnalyszer = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also {
//                        it.setAnalyzer(Executor {  }, ima)
//                    }

                var myFaces = mutableListOf<Face>()
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(Executor {  }, FaceAnalyzer { faces ->
                            // Once you have detected the faces, use the Canvas API to draw a rectangle around them on the screen
                            Log.d("the number of faces is detected is : " , faces.size.toString())

                            myFaces = faces as MutableList<Face>

                        })
                    }
//                cameraController.setImageAnalysisAnalyzer(Executor {  })

//                Canvas(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp),
//
//                ){
//                    myFaces.forEach {
//                        it.boundingBox.let {rect ->
//                            drawIntoCanvas {canvas ->
//                                val paint = android.graphics.Paint().apply {
//                                this.style = android.graphics.Paint.Style.STROKE
//                                this.strokeWidth = strokeWidth
//                                this.color = color
//                            }
//
//                                canvas.nativeCanvas.drawRect(
//                                    rect.left.toFloat(),
//                                    rect.top.toFloat(),
//                                    rect.right.toFloat(),
//                                    rect.bottom.toFloat(),
//                                    paint
//                                )
//                            }
//                        }
//
//                    }
//                }


//                        Icon(
//                            modifier = Modifier.size(40.dp),
//                            imageVector = Icons.Default.AddCircle,
//                            contentDescription = null

               // )
            }
        }
    ) { innerPadding: PaddingValues ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = { context ->
                PreviewView(context).apply {
                    setBackgroundColor(Color.White.toArgb())
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    scaleType = PreviewView.ScaleType.FILL_START
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }.also { previewView ->
                    previewView.controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)
                }
            },
            onRelease = {
                cameraController.unbind()
            }
        )
    }
}