package com.example.myapplication.presentation.permissions

import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myapplication.presentation.composables.helperClasses.CollectedData
import com.example.myapplication.presentation.composables.helperClasses.CollectedDataRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FinishViewModel
@Inject constructor(private val dataRepo: CollectedDataRepository) : ViewModel(){


    val sharedData : StateFlow <List<CollectedData>> = dataRepo.measurements

    fun clear(){
        dataRepo.clear()
    }





}