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

                    previousFaceCenterY = currentCenterY



                    val newOffset: Offset = Offset(
                        x = face.boundingBox.exactCenterX()
                            .toFloat(),
                        y = face.boundingBox.exactCenterY()
                    )


                    val composeRect =
                        rect.toComposeRect()

                    Log.d("face offset",
                        " x = ${ composeRect.topLeft.x.toString() } y = ${composeRect.topLeft.y} " +
                                " h = ${composeRect.height} x = ${composeRect.width}")




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