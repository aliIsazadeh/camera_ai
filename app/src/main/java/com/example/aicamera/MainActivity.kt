package com.example.aicamera

import android.Manifest
import android.R.attr.*
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.Paint
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import com.example.aicamera.ui.theme.AiCameraTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
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


    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestCameraPermission()

        viewModel = ViewModelProvider(this)[CameraViewModel::class.java]

//        faceRepo = FacesRepoImpl()


        lateinit var preview: PreviewView
        setContent {
            AiCameraTheme {
                // A surface container using the 'background' color from the theme


                val selectedColor = Color(R.color.white)

                val facePositionPaint: Paint = Paint()
                facePositionPaint.color = selectedColor

                val idPaint: Paint = Paint()
                idPaint.color = selectedColor

                val boxPaint: Paint = Paint()
                boxPaint.color = selectedColor
                boxPaint.style = PaintingStyle.Stroke
                boxPaint.strokeWidth = 2f


                Box(
                    modifier = Modifier.fillMaxSize()
                ) {

                    val configuration = LocalConfiguration.current
                    val width = configuration.screenWidthDp
                    val height = configuration.screenHeightDp

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

                        when (perm.permission) {
                            Manifest.permission.CAMERA -> {
                                val systemUiController = rememberSystemUiController()
                                systemUiController.setSystemBarsColor(
                                    color = Color.Transparent
                                )
                                if (perm.status.isGranted) {
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
                                            preview = previewView

                                            CoroutineScope(Dispatchers.IO).launch {
                                                startCamera(
                                                    previewView,
                                                    CameraSelector.DEFAULT_FRONT_CAMERA,
                                                    imageAnalyzerFace
                                                )

                                            }
                                            previewView
                                        })

//                                    val faces = remember {
//                                        viewModel?.faces
//                                    }
//`
//
//                                    Log.d("ther coud be  ", "Log" + faces?.value?.size)


                                    var scale: Unit
                                    val state = viewModel?.faceState?.value

                                    var front by remember {
                                        mutableStateOf(true)
                                    }

                                    fun changeCamera(isFront: Boolean) {

                                        scope.launch {
                                            startCamera(
                                                cameraPreview = preview,
                                                cameraDirection = if (isFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA,
                                                imageAnalyzerFace
                                            )
                                        }
                                        front = isFront

                                    }


//                                    Scaffold(bottomBar = {
//                                        Row(verticalAlignment = Alignment.CenterVertically , horizontalArrangement = Arrangement.SpaceEvenly) {
//                                            IconToggleButton(
//                                                checked = front,
//                                                onCheckedChange = { changeCamera(it) },
//                                                modifier = Modifier.size(80.dp)
//                                                    .padding(24.dp)
//                                                    .zIndex(1f)
//                                            ) {
//
//                                                Icon(
//                                                    imageVector = Icons.Filled.Refresh,
//                                                    contentDescription = null,
//                                                    modifier = Modifier.size(48.dp)
//                                                )
//
//                                            }
//                                        }
//                                    }) { paddingvalues ->
                                    Column() {


                                        Box(modifier = Modifier.weight(10f)){
                                            if (front) {
                                                CameraViewFront(faceStates = viewModel!!.faceState)
                                            } else {
                                                CameraViewBack(faceStates = viewModel!!.faceState)
                                            }
                                        }

                                        Row(modifier = Modifier.background(color = MaterialTheme.colors.background).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically , horizontalArrangement = Arrangement.SpaceEvenly) {
                                            IconToggleButton(
                                                checked = front,
                                                onCheckedChange = { changeCamera(it) },
                                                modifier = Modifier
                                                    .padding(12.dp)
                                                    .zIndex(1f)
                                            ) {

                                                Icon(
                                                    imageVector = Icons.Filled.Refresh,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(48.dp),
                                                    tint = MaterialTheme.colors.onBackground
                                                )

                                            }
                                        }

                                    }
                                 //   }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private val faceDetectorAnalyzer by lazy {
        FaceAnalyzer(onFacesDetected = {
//            viewModel?.faces = mutableStateOf(it)
            viewModel?.changeFaceValue(it)

        })
    }


    private val objectDetectorAnalyzer by lazy {
        ObjectAnalyzer(onObjectDetected = {
//            viewModel?.faces = mutableStateOf(it)
            viewModel?.changeObjectValue(it)

        })
    }
    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }


    private val imageAnalyzerFace by lazy {
        ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, faceDetectorAnalyzer)
            }
    }

    private val imageAnalyzerObject by lazy {
        ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, objectDetectorAnalyzer)
            }
    }


    @SuppressLint("RestrictedApi")
    private fun startCamera(cameraPreview: PreviewView, cameraDirection: CameraSelector , imageAnalyzer: ImageAnalysis) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val preview = androidx.camera.core.Preview.Builder()
            .build()

        cameraProviderFuture.addListener(
            Runnable {
                preview.setSurfaceProvider(cameraPreview.surfaceProvider)
                cameraProviderFuture.get().bind(preview, imageAnalyzer, cameraDirection)
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
        imageAnalyzer: ImageAnalysis,
        cameraDirection: CameraSelector
    ) = try {
        unbindAll()
        bindToLifecycle(
            this@MainActivity,
            cameraDirection,
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