package com.jefersonsalazar.testquicksas.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import com.jefersonsalazar.testquicksas.R
import com.jefersonsalazar.testquicksas.network.Callback
import java.text.SimpleDateFormat
import java.util.*

class ActivityUtils {
    companion object {
        fun validateNetwork(context: Context): Boolean? {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            return activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting
        }

        fun showDialog(
            context: Activity,
            withNegativeBtn: Boolean,
            message: String,
            textButton: String,
            callback: Callback<Boolean>?
        ) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(message)
            builder.setPositiveButton(textButton) { dialog, id ->
                dialog.dismiss()
                callback?.onSuccess(true)
            }

            if (withNegativeBtn) {
                builder.setNegativeButton(context.resources.getString(R.string.text_cancel)) { dialog, id ->
                    dialog.dismiss()
                }
            }

            val dialog = builder.create()
            dialog.setCancelable(false)
            dialog.show()
        }

        fun convertLongToTime(time: Long): String {
            val date = Date(time)
            val format = SimpleDateFormat("MMM dd, yyyy HH:mm")

            return format.format(date)
        }

        fun currentTimeToLong(): Long {
            return System.currentTimeMillis()
        }
    }
}