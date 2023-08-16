package com.example.aicamera

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toComposeRect
import com.google.mlkit.vision.face.Face

@Composable
fun CameraViewFront(faceStates : State<List<Face>?>) {

    val  state = faceStates.value

    Box(modifier = Modifier.scale(scaleX = -1f, scaleY = 1f)) {
        state?.let { faces ->
            faces.onEach { face ->

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val rect = face.boundingBox




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
                        topLeft = composeRect.topLeft.copy(y = composeRect.top + ( 10000 * 8 / composeRect.height) , x = composeRect.left+ ( 10000 * 2 / composeRect.width)),
                        style = Stroke(width = 2f),
                        size = Size(
                            rect.width().toFloat() * 2f ,
                            rect.height().toFloat() * 2f
                        )
                    )
                }
            }
        }
    }
}