package com.jefersonsalazar.testquicksas.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jefersonsalazar.testquicksas.model.User
import com.jefersonsalazar.testquicksas.network.Callback
import com.jefersonsalazar.testquicksas.network.FirestoreService
import com.jefersonsalazar.testquicksas.util.Constants

class LoginViewModel : ViewModel() {

    val firestoreService = FirestoreService()
    var isLogged = MutableLiveData<String>()
    var isLoading = MutableLiveData<Boolean>()
    var listUsers: MutableLiveData<List<User>> = MutableLiveData()

    var isRegisteredSuccess = MutableLiveData<String>()
    var isUpdatedSuccess = MutableLiveData<String>()

    // usuario que se obtiene despues del login y se hace persistente en preferences
    var user = MutableLiveData<String>()

    fun login(email: String, password: String) {
        firestoreService.login(email, password, object : Callback<String> {
            override fun onSuccess(result: String?) {
                // actualizamos el usuario que viene en result
                user.postValue(result)

                isLogged.postValue(Constants.RESULT_OK)
                processFinished()
            }

            override fun onFailed(exception: String) {
                isLogged.postValue(exception)
                processFinished()
            }

        })
    }

    fun register(photo: Bitmap, name: String, lastName: String, email: String, password: String) {
        firestoreService.register(
            photo,
            name,
            lastName,
            email,
            password,
            object : Callback<String> {
                override fun onSuccess(result: String?) {
                    // actualizamos el usuario que viene en result
                    user.postValue(result)

                    isRegisteredSuccess.postValue(Constants.RESULT_OK)
                    processFinished()
                }

                override fun onFailed(exception: String) {
                    isRegisteredSuccess.postValue(exception)
                    processFinished()
                }
            })
    }

    fun updateInfoUser(photo: Bitmap?, currentUser: User) {
        firestoreService.updateInfoUser(photo, currentUser, object : Callback<String> {
            override fun onSuccess(result: String?) {
                // actualizamos el usuario que viene en result
                user.postValue(result)

                isUpdatedSuccess.postValue(Constants.RESULT_OK)
                processFinished()
            }

            override fun onFailed(exception: String) {
                Log.w("ERROR UPDATE", exception)
                processFinished()
            }

        })
    }

    fun processFinished() {
        isLoading.value = true
    }

    fun logout() {
        firestoreService.logout()
    }

    // traer los usuarios registrados en la app
    fun getUsersFromFirebase() {
        firestoreService.getUsersFirebase(object : Callback<List<User>> {
            override fun onSuccess(result: List<User>?) {
                listUsers.postValue(result)
            }

            override fun onFailed(exception: String) {
                Log.w("Error Obtener Users", exception)
            }
        })
    }

}