package com.jefersonsalazar.testquicksas.view.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.jefersonsalazar.testquicksas.R
import com.jefersonsalazar.testquicksas.util.ActivityUtils
import com.jefersonsalazar.testquicksas.util.Constants
import com.jefersonsalazar.testquicksas.util.Preferences
import com.jefersonsalazar.testquicksas.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private val preferences: Preferences? = Preferences.getInstance()

    // views
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // inicializamos las preferencias
        preferences?.sharedPreferences = getSharedPreferences(Constants.TAG_USER_INFO, Context.MODE_PRIVATE)
        preferences?.editor = preferences?.sharedPreferences?.edit()

        // inicializamos los views
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)

        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        btnLogin.setOnClickListener {
            if (ActivityUtils.validateNetwork(this) == true) {
                login()
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

        irARegistro.setOnClickListener {
            irARegistrarse()
        }

        observerViewModel()
    }

    override fun onResume() {
        super.onResume()

        if (loginViewModel.firestoreService.firebaseAuth.currentUser != null) {
            goMainActivity()
            return
        }

        loginViewModel.getUsersFromFirebase()
    }

    private fun login() {
        when {
            edtEmail.text.toString().trim() != "" && edtPassword.text.toString().trim() != "" -> {
                progressLogin.visibility = View.VISIBLE
                btnLogin.isEnabled = false

                loginViewModel.login(edtEmail.text.toString(), edtPassword.text.toString())
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
        loginViewModel.isLogged.observe(this, {
            when (it) {
                Constants.RESULT_OK -> {
                    goMainActivity()
                }
                Constants.ERROR_USER_NOT_FOUND -> {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.error_user_not_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Constants.ERROR_USER_DISABLED -> {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.error_user_disabled),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Constants.ERROR_WRONG_PASSWORD -> {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.error_password),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.error_general_login),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

        loginViewModel.isLoading.observe(this, {
            if (it != null) {
                progressLogin.visibility = View.GONE
                btnLogin.isEnabled = true
            }
        })

        // observamos si el usuario cambio para almacenarlo en las preferencias
        loginViewModel.user.observe(this, { user ->
            preferences?.setUserInfo(user)
        })

        // observamos si se consultan los usuarios con exito
        loginViewModel.listUsers.observe(this, { users ->
            Log.d("SIZE USERS", "" + users.size)
        })
    }

    private fun goMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun irARegistrarse() {
        val intent = Intent(this, CreateAccountActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

}