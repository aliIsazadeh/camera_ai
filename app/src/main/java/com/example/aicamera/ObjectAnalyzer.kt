package com.example.aicamera

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
class ObjectAnalyzer(private val onObjectDetected : (List<DetectedObject>) -> Unit) : ImageAnalysis.Analyzer {


    val TAG = "OBJECT_ANALYZER"
    val streamMode = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
        .enableClassification()  // Optional
        .build()

    // Multiple object detection in static images
    val singleImageMode = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
        .enableMultipleObjects()
        .enableClassification()  // Optional
        .build()


    val objectDetector = ObjectDetection.getClient(streamMode)

    override fun  analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            // Pass image to an ML Kit Vision API
            // ...
            objectDetector.process(InputImage.fromMediaImage(mediaImage  , image.rotationDegrees)).addOnSuccessListener { objects ->

                onObjectDetected(objects)

            }
                .addOnFailureListener { e ->
                Log.e(TAG, "Face detection failed", e)
            }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }


}