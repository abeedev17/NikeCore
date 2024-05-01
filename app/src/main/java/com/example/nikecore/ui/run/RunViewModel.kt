package com.example.nikecore.ui.run

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nikecore.repositories.MainRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RunViewModel @Inject constructor(
    val mainRepository: MainRepository
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text
    val selectedCoordinates: MutableLiveData<LatLng?> = MutableLiveData(null)
    fun postSelectedCoordinates(latLng: LatLng?) {
        selectedCoordinates.postValue(latLng)
    }
}