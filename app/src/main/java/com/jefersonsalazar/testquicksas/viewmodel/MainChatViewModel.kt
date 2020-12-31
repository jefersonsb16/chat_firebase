package com.jefersonsalazar.testquicksas.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jefersonsalazar.testquicksas.model.User
import com.jefersonsalazar.testquicksas.network.Callback
import com.jefersonsalazar.testquicksas.network.FirestoreService

class MainChatViewModel : ViewModel() {

    val fireStoreService = FirestoreService()
    var listUsers: MutableLiveData<List<User>> = MutableLiveData()

    fun getUsersFromFirebaseWithListener() {
        fireStoreService.getUsersFirebaseWithListener(object : Callback<List<User>> {
            override fun onSuccess(result: List<User>?) {
                listUsers.postValue(result)
            }

            override fun onFailed(exception: String) {
                Log.w("Error Obtener Users", exception)
            }
        })
    }
}