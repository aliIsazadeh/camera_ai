package com.example.aicamera

import android.Manifest
import android.R.attr.*
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Rect
import androidx.compose.ui.graphics.Paint
import android.icu.number.Scale
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.platform.LocalConfiguration
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

                                            CoroutineScope(Dispatchers.IO).launch {
                                                startCamera(previewView)

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
                                    Box(modifier = Modifier.scale(scaleX = -1f, scaleY = 1f)) {
                                        state?.let { faces ->
                                            faces.onEach { face ->

                                                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                                    val rect = face.boundingBox


                                                    val mapedRec = mapRectOnView(mlKitRect = rect, viewHeight = height, viewWidth = width)


                                                    // Class variables
                                                    var yOffset = 0
                                                    var previousFaceCenterY = 0f

// On each detect
                                                    val currentFace = face
                                                    val currentCenterY = currentFace.boundingBox.exactCenterY()

                                                    if(currentCenterY > previousFaceCenterY) {
                                                        // Face moved down
                                                        yOffset++
                                                    } else if (currentCenterY < previousFaceCenterY) {
                                                        // Face moved up
                                                        yOffset--
                                                    }

// Update previous for next time
                                                    previousFaceCenterY = currentCenterY

// Apply offset
                                                    mapedRec.offset(0, -yOffset)

//                                                        drawIntoCanvas {
//                                                            it.drawRect(rect = face.boundingBox.toComposeRect() , paint =  boxPaint)
//                                                        }



                                                    val newOffset: Offset = Offset(
                                                        x = face.boundingBox.exactCenterX()
                                                            .toFloat(),
                                                        y = face.boundingBox.exactCenterY()
                                                    )



//                                                    Log.d("Left", rect.left.toString())
//                                                    Log.d("right", rect.right.toString())
//                                                    Log.d("top", rect.top.toString())
//                                                    Log.d("bottom", rect.bottom.toString())
                                                    //
                                                    //
                                                    //                                                    val faceCenterX = it.boundingBox.centerX()
                                                    //                                                    val faceCenterY = it.boundingBox.centerY()

                                                    val composeRect =
                                                        rect.toComposeRect()

                                                    Log.d("face offset",
                                                        " x = ${ composeRect.topLeft.x.toString() } y = ${composeRect.topLeft.y} " +
                                                                " h = ${composeRect.height} x = ${composeRect.width}")
                                                    //

                                                    //
                                                    //                                                drawCircle(
                                                    //                                                    color = Color.Red,
                                                    //                                                    radius = rect.right.toFloat() - rect.centerX()
                                                    //                                                        .toFloat(),
                                                    //                                                    center = composeRect.center,
                                                    //                                                    style = Stroke(width = 2f),
                                                    //                                                )

                                                    //


                                                    drawRect(
                                                        Color.Red,
                                                        topLeft = composeRect.topLeft.copy(y = composeRect.top + ( 1000 * 4 / composeRect.height) , x = composeRect.left ),
                                                        style = Stroke(width = 2f),
                                                        size = Size(
                                                            rect.width().toFloat() * 1.5f ,
                                                            rect.height().toFloat() * 1.5f
                                                        )
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
        }
    }

    fun mapRectOnView(mlKitRect: Rect, viewWidth: Int, viewHeight: Int): Rect {

        // Get scale factor between mlKitRect and view
        val xScale = viewWidth.toFloat() / mlKitRect.width()
        val yScale = viewHeight.toFloat() / mlKitRect.height()

        // Scale the coordinates
        val left = mlKitRect.left * xScale
        val top = mlKitRect.top * yScale
        val right = left + (mlKitRect.width() * xScale)
        val bottom = top + (mlKitRect.height() * yScale)

        // Return mapped rect
        return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())

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


    private val imageAnalyzer by lazy {
        ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, faceDetectorAnalyzer)
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