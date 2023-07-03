package com.example.aicamera

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
@ExperimentalGetImage
class FaceAnalyzer(private val onFacesDetected: (List<Face>) -> Unit) : ImageAnalysis.Analyzer {
    val highAccuracyOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    // Real-time contour detection
    val realTimeOpts = FaceDetectorOptions.Builder()
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .build()
    private val detector = FaceDetection.getClient(realTimeOpts)

    override fun  analyze(image: ImageProxy) {
        val mediaImage = image.image ?: return

        val rotation = image.imageInfo.rotationDegrees
//        val visionImage = FirebaseVisionImage.fromMediaImage(mediaImage, rotation)

        detector.process(InputImage.fromMediaImage(mediaImage , image.imageInfo.rotationDegrees))
            .addOnSuccessListener { faces ->
                // Once you have detected the faces, call the onFacesDetected callback
                onFacesDetected(faces)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Face detection failed", e)
            }
            .addOnCompleteListener {
                image.close()
            }
    }

    companion object {
        private const val TAG = "FaceAnalyzer"
    }
}