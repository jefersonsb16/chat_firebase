package com.jefersonsalazar.testquicksas.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jefersonsalazar.testquicksas.model.User
import com.jefersonsalazar.testquicksas.network.Callback
import com.jefersonsalazar.testquicksas.network.FirestoreService

class ListChatsCurrentUserViewModel : ViewModel() {

    private val fireStoreService = FirestoreService()
    var listChats: MutableLiveData<List<User>> = MutableLiveData()
    var isLoading = MutableLiveData<Boolean>()

    fun getChatsCurrentUserFromFirebase() {
        fireStoreService.getChatsCurrentUserFirebase(object : Callback<List<User>> {
            override fun onSuccess(result: List<User>?) {
                listChats.postValue(result)
                processFinished()
            }

            override fun onFailed(exception: String) {
                Log.w("Error Obtener Users", exception)
                processFinished()
            }
        })
    }

    fun processFinished() {
        isLoading.value = true
    }

}