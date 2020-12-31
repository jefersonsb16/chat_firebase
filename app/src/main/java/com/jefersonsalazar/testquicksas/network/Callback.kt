package com.jefersonsalazar.testquicksas.network

interface Callback<T> {
    fun onSuccess(result: T?)

    fun onFailed(exception: String)
}