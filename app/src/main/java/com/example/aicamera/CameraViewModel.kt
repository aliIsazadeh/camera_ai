package com.example.aicamera

import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.face.Face

class CameraViewModel()  : ViewModel(){


    var faces : MutableState<List<Face>> = mutableStateOf<List<Face>>(mutableListOf())



    fun changeValue(){
        Log.d("in the view model" , "value has changed")
    }




}