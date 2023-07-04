package com.example.aicamera

import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FacesRepoImpl : FacesRepo {
    override fun sendFaces(faces : List<Face>): Flow<List<Face>> {
        return flow {  emit(faces) }
    }
}