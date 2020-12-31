package com.jefersonsalazar.testquicksas.view.ui.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProviders
import com.jefersonsalazar.testquicksas.R
import com.jefersonsalazar.testquicksas.network.Callback
import com.jefersonsalazar.testquicksas.util.ActivityUtils
import com.jefersonsalazar.testquicksas.util.Constants
import com.jefersonsalazar.testquicksas.util.Preferences
import com.jefersonsalazar.testquicksas.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.activity_create_account.*
import java.io.IOException

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private val preferences: Preferences? = Preferences.getInstance()
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    // image profile
    private val pickImage = 100
    private var imageUri: Uri? = null
    private lateinit var photo: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        // inicializamos las preferencias
        preferences?.sharedPreferences =
            getSharedPreferences(Constants.TAG_USER_INFO, Context.MODE_PRIVATE)
        preferences?.editor = preferences?.sharedPreferences?.edit()

        // agregamos soporte a nuestro toolbar
        toolbarCreateAccount.title = resources.getString(R.string.text_create_account)
        setSupportActionBar(toolbarCreateAccount)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarCreateAccount.setNavigationOnClickListener { finish() }

        // incializamos nuestro viewmodel
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        btnRegisterUser.setOnClickListener {
            if (ActivityUtils.validateNetwork(this) == true) {
                registerUser()
            } else {
                ActivityUtils.showDialog(
                    this!!,
                    false,
                    getString(R.string.no_connected_internet),
                    getString(R.string.text_ok),
                    null
                )
            }
        }

        // opcion para cargar imagen de la galeria
        ivProfileRegister.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        observerViewModel()
    }

    private fun validateEmail(): Boolean {
        return if (!TextUtils.isEmpty(edtEmailRegister.text.toString())) {
            if (edtEmailRegister.text.toString().matches(emailPattern.toRegex())) {
                textinputedtEmailRegister.error = null
                true
            } else {
                textinputedtEmailRegister.error = getString(R.string.error_email)
                false
            }
        } else {
            textinputedtEmailRegister.error = getString(R.string.error_email)
            false
        }
    }

    private fun registerUser() {
        var isEmailValid = validateEmail()
        textinputedtEmailRegister?.editText?.doOnTextChanged { _, _, _, _ ->
            isEmailValid = validateEmail()
        }

        when {
            !TextUtils.isEmpty(edtEmailRegister.text.toString()) && !TextUtils.isEmpty(
                edtNameRegister.text.toString()
            ) && !TextUtils.isEmpty(
                edtLastNameRegister.text.toString()
            ) && !TextUtils.isEmpty(
                edtPasswordRegister.text.toString()
            ) -> {
                if (isEmailValid) {
                    if (imageUri != null) {
                        progressRegister.visibility = View.VISIBLE
                        btnRegisterUser.isEnabled = false

                        // llamamos nuestro metodo register del viewmodel
                        loginViewModel.register(
                            photo,
                            edtNameRegister.text.toString().trim(),
                            edtLastNameRegister.text.toString().trim(),
                            edtEmailRegister.text.toString().trim(),
                            edtPasswordRegister.text.toString().trim()
                        )
                    } else {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.text_empty_photo),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            else -> {
                Toast.makeText(
                    this,
                    resources.getString(R.string.text_empty_fields),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // observamos los cambios de nuestro view model
    // y en base a su estado ejecutamos una accion
    private fun observerViewModel() {
        loginViewModel.isRegisteredSuccess.observe(this, {
            when (it) {
                Constants.RESULT_OK -> {
                    // notificamos el resultado al usuario
                    ActivityUtils.showDialog(
                        this,
                        false,
                        resources.getString(R.string.create_account_success),
                        resources.getString(R.string.text_ok),
                        object :
                            Callback<Boolean> {
                            override fun onSuccess(result: Boolean?) {
                                finish()
                            }

                            override fun onFailed(exception: String) {
                                // sin implementacion
                            }

                        })
                }
                Constants.ERROR_WEAK_PASSWORD -> {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.error_password_invalid),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Constants.ERROR_EMAIL_ALREADY_IN_USE -> {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.error_user_exist),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.error_general_register),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

        loginViewModel.isLoading.observe(this, {
            if (it != null) {
                progressRegister.visibility = View.GONE
                btnRegisterUser.isEnabled = true
            }
        })

        // observamos si el usuario cambio para almacenarlo en las preferencias
        loginViewModel.user.observe(this, { user ->
            preferences?.setUserInfo(user)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data

            // convertimos la imagen a bitmap
            try {
                photo = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                ivProfileRegister.setImageBitmap(photo)
            } catch (e: IOException) {
                println("Error al convertir imagen a bitmap")
            }
        }
    }
}