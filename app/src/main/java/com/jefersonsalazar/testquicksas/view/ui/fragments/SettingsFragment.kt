package com.jefersonsalazar.testquicksas.view.ui.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.jefersonsalazar.testquicksas.R
import com.jefersonsalazar.testquicksas.model.User
import com.jefersonsalazar.testquicksas.util.ActivityUtils
import com.jefersonsalazar.testquicksas.util.Constants
import com.jefersonsalazar.testquicksas.util.Preferences
import com.jefersonsalazar.testquicksas.view.ui.activities.LoginActivity
import com.jefersonsalazar.testquicksas.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.fragment_settings.*
import java.io.IOException


class SettingsFragment : Fragment() {

    private val preferencesUserInfo: Preferences? = Preferences.getInstance()
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var user: User

    // image profile
    private val pickImage = 100
    private var imageUri: Uri? = null
    private var photo: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // inicializamos las preferencias de la informacion del usuario
        preferencesUserInfo?.sharedPreferences =
            activity?.getSharedPreferences(Constants.TAG_USER_INFO, Context.MODE_PRIVATE)
        preferencesUserInfo?.editor = preferencesUserInfo?.sharedPreferences?.edit()

        // instanciamos nuestro view model
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)


        btnLogout.setOnClickListener {
            loginViewModel.logout()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            startActivity(intent)
            activity?.finish()
        }

        ivProfileSettings.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        btnUpdateProfile.setOnClickListener {
            if (ActivityUtils.validateNetwork(activity!!) == true) {
                when {
                    !TextUtils.isEmpty(edtName.text.toString()) && !TextUtils.isEmpty(edtLastName.text.toString()) -> {
                        progressBarUpdate.visibility = View.VISIBLE
                        btnUpdateProfile.isEnabled = false
                        btnLogout.isEnabled = false

                        user.name = edtName.text.toString().trim()
                        user.lastName = edtLastName.text.toString().trim()

                        loginViewModel.updateInfoUser(photo, user)
                    }
                    else -> {
                        Toast.makeText(
                            activity!!,
                            resources.getString(R.string.text_empty_fields),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                ActivityUtils.showDialog(
                    activity!!,
                    false,
                    getString(R.string.no_connected_internet),
                    getString(R.string.text_ok),
                    null
                )
            }
        }

        // metodo para cargar info del usuario logueado
        loadInfoUser()

        observerViewModel()
    }

    fun loadInfoUser() {
        user = preferencesUserInfo?.getUserInfo()!!

        edtName.setText(user.name)
        edtLastName.setText(user.lastName)

        Glide.with(this)
            .load(user.photo)
            .placeholder(R.drawable.ic_download)
            .centerCrop()
            .into(ivProfileSettings);

        photo = null
    }

    private fun observerViewModel() {
        // observamos si el usuario cambio para almacenarlo en las preferencias
        loginViewModel.user.observe(this, { userInput ->
            preferencesUserInfo?.setUserInfo(userInput)
            user = preferencesUserInfo?.getUserInfo()!!

            Toast.makeText(activity, getString(R.string.text_update_success), Toast.LENGTH_SHORT)
                .show()
            loadInfoUser()
        })

        loginViewModel.isLoading.observe(this, {
            if (it != null) {
                progressBarUpdate.visibility = View.GONE
                btnUpdateProfile.isEnabled = true
                btnLogout.isEnabled = true
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data

            // convertimos la imagen a bitmap
            try {
                photo = MediaStore.Images.Media.getBitmap(activity?.contentResolver, imageUri)
                ivProfileSettings.setImageBitmap(photo)
            } catch (e: IOException) {
                println("Error al convertir imagen a bitmap")
            }
        }
    }

}