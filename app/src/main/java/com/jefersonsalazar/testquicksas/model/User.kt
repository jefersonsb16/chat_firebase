package com.jefersonsalazar.testquicksas.model

import java.io.Serializable

data class User(
    val id: String,
    val photo: String,
    var name: String,
    var lastName: String,
    val email: String,
    val password: String,
    var time: Long
) : Serializable