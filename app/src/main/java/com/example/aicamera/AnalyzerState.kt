package com.example.aicamera

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.objects.DetectedObject

data class AnalyzerState(
    val faceList: MutableState<List<Face>> = mutableStateOf(mutableListOf()),
    val objectList: MutableState<List<DetectedObject>> = mutableStateOf(
        mutableListOf()
    )
)
