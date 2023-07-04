package com.example.aicamera

import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.flow.Flow

interface FacesRepo {
    fun sendFaces(faces : List<Face>) : Flow<List<Face>>
}