package com.jefersonsalazar.testquicksas.network

import android.graphics.Bitmap
import android.os.Handler
import android.util.Log
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.jefersonsalazar.testquicksas.model.User
import com.jefersonsalazar.testquicksas.util.ActivityUtils
import com.jefersonsalazar.testquicksas.util.Constants
import java.io.ByteArrayOutputStream

const val TIME_WAIT_IMAGE: Long = 2500

class FirestoreService {

    private val firebaseFirestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()

    private val storage: FirebaseStorage = Firebase.storage
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val databaseReferenceUsers: DatabaseReference =
        database.reference.child(Constants.TAG_USER_TABLE)

    val databaseReferenceChatGroup: DatabaseReference =
        database.reference.child(Constants.GROUP_MESSAGES)

    // traemos la configuracion para hacer persistentes los datos que descarguemos
    private val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build()

    init {
        // habilitamos la persistencia de datos
        firebaseFirestore.firestoreSettings = settings
    }

    fun login(email: String, password: String, callback: Callback<String>) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // llamamos la consulta de la info del usuario logueado
                    getInfoCurrentUser(callback)
                } else {
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthWeakPasswordException) {
                        // sin implementacion - password inseguro
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        callback.onFailed(e.errorCode)
                    } catch (e: FirebaseAuthUserCollisionException) {
                        // sin implementacion - cuenta ya esixstente
                    } catch (e: FirebaseAuthInvalidUserException) {
                        callback.onFailed(e.errorCode)
                    } catch (e: FirebaseTooManyRequestsException) {
                        callback.onFailed(e.localizedMessage)
                    }
                }
            }
    }

    fun getInfoCurrentUser(callback: Callback<String>) {
        val userFirebase: FirebaseUser = firebaseAuth.currentUser!!
        val reference = firebaseFirestore.collection(Constants.TAG_USER_TABLE).document(userFirebase.uid)

        // hacemos un llamado a firebase para obetener la informacion
        reference.get().addOnSuccessListener { documentSnapshot ->
            // obtenemos cada propiedad
            val photo: String = documentSnapshot.data?.get("photo").toString()
            val name: String = documentSnapshot.data?.get("name").toString()
            val lastName: String = documentSnapshot.data?.get("lastName").toString()
            val email: String = documentSnapshot.data?.get("email").toString()
            val password: String = documentSnapshot.data?.get("password").toString()

            val user = User(userFirebase.uid, photo, name, lastName, email, password, ActivityUtils.currentTimeToLong())
            callback.onSuccess(Gson().toJson(user))
        }.addOnFailureListener {
            callback.onFailed("ERROR")
        }
    }

    fun register(
        photo: Bitmap,
        name: String,
        lastName: String,
        email: String,
        password: String,
        callback: Callback<String>
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // obtenemos la instancia del usuario registrado
                val userFirebase: FirebaseUser = firebaseAuth.currentUser!!

                sendImageToDataBase(
                    userFirebase, photo, name,
                    lastName,
                    email,
                    password, callback
                )
            } else {
                try {
                    throw task.exception!!
                } catch (e: FirebaseAuthWeakPasswordException) {
                    callback.onFailed(e.errorCode)
                } catch (e: FirebaseAuthUserCollisionException) {
                    callback.onFailed(e.errorCode)
                }
            }
        }
    }

    fun updateInfoUser(photo: Bitmap?, user: User, callback: Callback<String>) {
        if (photo != null) {
            sendImageToDataBase(
                firebaseAuth.currentUser!!,
                photo,
                user.name,
                user.lastName,
                user.email,
                user.password,
                callback
            )
        } else {
            // guardamos el usuario en firestore
            firebaseFirestore.collection(Constants.TAG_USER_TABLE).document(firebaseAuth.currentUser!!.uid)
                .set(user)

            callback.onSuccess(Gson().toJson(user))
        }
    }

    fun sendImageToDataBase(
        userFirebase: FirebaseUser, photo: Bitmap, name: String,
        lastName: String,
        email: String,
        password: String, callback: Callback<String>
    ) {
        val bytes = ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val data: ByteArray = bytes.toByteArray()

        storage.reference.child("images").child(userFirebase.uid + ".jpeg")
            .putBytes(data)
            .addOnSuccessListener { taskSnapshot ->
                val urlPhoto = taskSnapshot.storage.downloadUrl
                val currentUserDb = databaseReferenceUsers.child(userFirebase.uid)

                // ejecutamos este codigo 2 segundos despues, debido a que la urlPhoto tarda un poco en ser obtenida
                val runnable = Runnable {
                    // creamos instancia de usuario
                    val user = User(userFirebase.uid, urlPhoto.result.toString(), name, lastName, email, password, ActivityUtils.currentTimeToLong())
                    currentUserDb.setValue(user)

                    // guardamos el usuario en firestore
                    firebaseFirestore.collection(Constants.TAG_USER_TABLE).document(userFirebase.uid)
                        .set(user)

                    getInfoCurrentUser(callback)
                }
                val handler = Handler()
                handler.postDelayed(runnable, TIME_WAIT_IMAGE)
            }
    }

    fun logout() {
        firebaseAuth.signOut()
    }


    /* METODOS PARA MANEJO DE CHAT */
    fun getUsersFirebase(callback: Callback<List<User>>) {
        firebaseFirestore.collection(Constants.TAG_USER_TABLE)
            .get()
            .addOnSuccessListener { result ->
                val listUsers = ArrayList<User>()

                for (doc in result!!) {
                    val id: String = doc.data?.get("id").toString()
                    val photo: String = doc.data?.get("photo").toString()
                    val name: String = doc.data?.get("name").toString()
                    val lastName: String = doc.data?.get("lastName").toString()
                    val email: String = doc.data?.get("email").toString()
                    val password: String = doc.data?.get("password").toString()

                    val user = User(id, photo, name, lastName, email, password, ActivityUtils.currentTimeToLong())
                    listUsers.add(user)
                }

                callback.onSuccess(listUsers)
            }.addOnFailureListener {
                Log.w("ERROR_GET_USERS", it.localizedMessage)
            }
    }

    fun getUsersFirebaseWithListener(callback: Callback<List<User>>) {
        firebaseFirestore.collection(Constants.TAG_USER_TABLE)
            .addSnapshotListener { result, e ->
                val listUsers = ArrayList<User>()

                if (firebaseAuth.currentUser != null) {
                    val currentUser = firebaseAuth.currentUser

                    for (doc in result!!) {
                        if (currentUser?.uid != doc.data?.get("id").toString()) {
                            val id: String = doc.data?.get("id").toString()
                            val photo: String = doc.data?.get("photo").toString()
                            val name: String = doc.data?.get("name").toString()
                            val lastName: String = doc.data?.get("lastName").toString()
                            val email: String = doc.data?.get("email").toString()
                            val password: String = doc.data?.get("password").toString()

                            val user = User(id, photo, name, lastName, email, password, ActivityUtils.currentTimeToLong())
                            listUsers.add(user)
                        }
                    }
                    callback.onSuccess(listUsers)
                }
            }
    }

    // obtiene los chats que tiene cada usuario
    fun getChatsCurrentUserFirebase(callback: Callback<List<User>>) {
        val currentUser = firebaseAuth.currentUser!!

        firebaseFirestore.collection(Constants.USERS_CHATS).document(currentUser.uid)
            .collection(Constants.MY_CHATS)
            .addSnapshotListener { result, e ->
                val listChatsUser = ArrayList<User>()

                if (firebaseAuth.currentUser != null) {
                    for (doc in result!!) {
                        val id: String = doc.data?.get("id").toString()
                        val photo: String = doc.data?.get("photo").toString()
                        val name: String = doc.data?.get("name").toString()
                        val lastName: String = doc.data?.get("lastName").toString()
                        val email: String = doc.data?.get("email").toString()
                        val password: String = doc.data?.get("password").toString()
                        val time: String = doc.data?.get("time").toString()

                        val user = User(id, photo, name, lastName, email, password, time.toLong())
                        listChatsUser.add(user)
                    }
                    callback.onSuccess(listChatsUser)
                }
            }
    }
}