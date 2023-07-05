package com.example.aicamera

import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.objects.DetectedObject

class CameraViewModel()  : ViewModel(){


   // var faces : MutableState<List<Face>> = mutableStateOf<List<Face>>(mutableListOf())


    private val _faceState : MutableState<List<Face>?> = mutableStateOf(null)
    var faceState : State<List<Face>?> = _faceState


    private val _objectsState : MutableState<List<DetectedObject>?> = mutableStateOf(null)
    var objectsState : State<List<DetectedObject>?> = _objectsState








    fun changeFaceValue(faceList : List<Face>){
        _faceState.value = faceList
    }

    fun changeObjectValue(objectList : List<DetectedObject>){
        _objectsState.value = objectList
    }





}