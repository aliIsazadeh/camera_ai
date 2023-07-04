package com.example.aicamera

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import com.example.aicamera.ui.theme.AiCameraTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
class MainActivity : ComponentActivity() {
    val scope = CoroutineScope(context = Dispatchers.Default)

    private var viewModel: CameraViewModel? = null
//
//    private lateinit var  faceRepo : FacesRepo


    val faces = mutableListOf<Face>()
    @OptIn(ExperimentalPermissionsApi::class)
    override fun  onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestCameraPermission()

        viewModel = ViewModelProvider(this)[CameraViewModel::class.java]

//        faceRepo = FacesRepoImpl()


        setContent {
            AiCameraTheme {
                // A surface container using the 'background' color from the theme

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {

                    Log.d("ther coud be a ", "Log")
                    val context = LocalContext.current
                    val permissionsState = rememberMultiplePermissionsState(
                        permissions = listOf(
                            Manifest.permission.CAMERA,
                        )
                    )
                    val lifecycleOwner = LocalLifecycleOwner.current

                    DisposableEffect(
                        key1 = lifecycleOwner,
                        effect = {
                            val observer = LifecycleEventObserver { _, event ->
                                if (event == Lifecycle.Event.ON_START) {
                                    permissionsState.launchMultiplePermissionRequest()
                                }
                            }
                            lifecycleOwner.lifecycle.addObserver(observer)

                            onDispose {
                                lifecycleOwner.lifecycle.removeObserver(observer)
                            }
                        }
                    )

                    permissionsState.permissions.forEach { perm ->
                        Log.d("ther coud be 1 ", "Log")

                        when (perm.permission) {
                            Manifest.permission.CAMERA -> {
                                val systemUiController = rememberSystemUiController()
                                systemUiController.setSystemBarsColor(
                                    color = Color.Transparent
                                )
                                if(perm.status.isGranted){
                                    //permission is granted
                                    AndroidView(modifier = Modifier.fillMaxSize(),
                                        factory = { context ->
                                            val previewView =
                                                PreviewView(context).apply {
                                                    this.scaleType =
                                                        scaleType
                                                    layoutParams =
                                                        ViewGroup.LayoutParams(
                                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                                            ViewGroup.LayoutParams.MATCH_PARENT
                                                        )
                                                }

                                            CoroutineScope(Dispatchers.IO).launch {
                                                startCamera(previewView)

                                            }
                                            previewView
                                        })
                                    Log.d("ther coud be 2 ", "Log")

                                    val faces = remember {
                                        viewModel?.faces
                                    }


                                    Log.d("ther coud be  ", "Log" + faces?.value?.size)


                                    faces?.value?.forEach {
                                        Log.d("View model" , "face detected")
                                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                            val rect = it.boundingBox

                                            drawRect(
                                                Color.Red,
                                                topLeft = Offset(
                                                    x = rect.left.toFloat(),
                                                    y = rect.top.toFloat()
                                                ),
                                                style = Stroke(width = 2f,)
                                            )
                                        }
                                    }



                                }
                            }
                        }
                    }

                }
            }
        }

    }

    fun setFaces(faceList : List<Face>) {
        faces.clear()
        faces.addAll(faceList)
    }

    private val faceDetectorAnalyzer by lazy {
        FaceAnalyzer(onFacesDetected = {
           viewModel?.faces = mutableStateOf(it)
            viewModel?.changeValue()

            Log.d("the number of faces" ,it.size.toString())
        })
    }
    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }


    private val imageAnalyzer by lazy {
        ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor , faceDetectorAnalyzer)
            }
    }


    @SuppressLint("RestrictedApi")
    private fun startCamera(cameraPreview: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val preview = androidx.camera.core.Preview.Builder()
            .build()

        cameraProviderFuture.addListener(
            Runnable {
                preview.setSurfaceProvider(cameraPreview.surfaceProvider)
                cameraProviderFuture.get().bind(preview, imageAnalyzer)
            },
            ContextCompat.getMainExecutor(this)
        )

        cameraPreview.setOnTouchListener { view: View, motionEvent: MotionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> return@setOnTouchListener true
                MotionEvent.ACTION_UP -> {
                    val factory =
                        cameraPreview.meteringPointFactory

                    val point = factory.createPoint(
                        motionEvent.x,
                        motionEvent.y
                    )

                    val action =
                        FocusMeteringAction.Builder(
                            point
                        ).build()

                    preview.camera?.cameraControl?.startFocusAndMetering(
                        action
                    )
                    view.performClick()
//                    viewModel?.cameraFocusPoint = Offset(motionEvent.x, motionEvent.y)
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }


    }

    fun requestCameraPermission() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Log.i("kilo", "Permission granted")
            } else {
                Log.i("kilo", "Permission denied")
            }
        }
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("kilo", "Permission previously granted")
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> Log.i("kilo", "Show camera permissions dialog")

            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    private fun ProcessCameraProvider.bind(
        preview: androidx.camera.core.Preview,
        imageAnalyzer: ImageAnalysis
    ) = try {
        unbindAll()
        bindToLifecycle(
            this@MainActivity,
            CameraSelector.DEFAULT_FRONT_CAMERA,
            preview,
            imageAnalyzer
        )
    } catch (ise: IllegalStateException) {
        // Thrown if binding is not done from the main thread
//        Log.e(TAG, "Binding failed", ise)
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AiCameraTheme {
        Greeting("Android")
    }
}