package com.jefersonsalazar.testquicksas.util

class Constants {
    companion object {

        // OTHERS
        const val TAG_FRAGMENT_MAIN = "TAG_FRAGMENT_MAIN"
        const val TAG_FRAGMENT_CHATS = "TAG_FRAGMENT_CHATS"

        // INTENT KEYS
        const val KEY_USER_INTENT = "USER"

        // STATUS CODES
        const val RESULT_OK = "OK"
        const val ERROR_USER_NOT_FOUND = "ERROR_USER_NOT_FOUND"
        const val ERROR_USER_DISABLED = "ERROR_USER_DISABLED"
        const val ERROR_WRONG_PASSWORD = "ERROR_WRONG_PASSWORD"
        const val ERROR_WEAK_PASSWORD = "ERROR_WEAK_PASSWORD"
        const val ERROR_EMAIL_ALREADY_IN_USE = "ERROR_EMAIL_ALREADY_IN_USE"

        // Preferences
        const val IS_LOGGED: String = "LOGGED"
        const val TAG_USER_INFO: String = "TAG_USER_INFO"

        // BD
        const val TAG_USER_TABLE: String = "Users"

        // Chat
        const val GROUP_MESSAGES: String = "group_messages"
        const val PRIVATE_MESSAGES: String = "users_messages"
        const val USERS_CHATS: String = "users_chats"
        const val MY_CHATS: String = "my_chats"
        const val LOADING_IMAGE_URL: String = "https://www.google.com/images/spin-32.gif"

        // Image
        const val REQUEST_IMAGE = 2
    }
}