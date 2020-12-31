package com.jefersonsalazar.testquicksas.view.adapter

import com.jefersonsalazar.testquicksas.model.User

interface UserListener {
    fun onUserClicked(user: User, position: Int)
}