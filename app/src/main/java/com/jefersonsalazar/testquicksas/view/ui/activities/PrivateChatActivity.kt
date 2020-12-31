package com.jefersonsalazar.testquicksas.view.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jefersonsalazar.testquicksas.R
import com.jefersonsalazar.testquicksas.model.MessageChat
import com.jefersonsalazar.testquicksas.model.User
import com.jefersonsalazar.testquicksas.util.ActivityUtils
import com.jefersonsalazar.testquicksas.util.Constants
import com.jefersonsalazar.testquicksas.util.Preferences
import com.jefersonsalazar.testquicksas.view.adapter.ChatMessageViewHolder
import kotlinx.android.synthetic.main.activity_private_chat.*

class PrivateChatActivity : AppCompatActivity() {

    var userInput: User? = null
    var currentUser: User? = null

    // firebase
    private val firebaseFireStore = FirebaseFirestore.getInstance()
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var chatReference: DatabaseReference? = null

    // propiedades para usar en recyclerview
    private var mLinearLayoutManager: LinearLayoutManager = LinearLayoutManager(this)
    private var mFirebaseAdapter: FirebaseRecyclerAdapter<MessageChat, ChatMessageViewHolder>? = null

    private val preferencesUserInfo: Preferences? = Preferences.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_private_chat)

        // agregamos soporte a nuestro toolbar
        setSupportActionBar(toolbarPrivateChat)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarPrivateChat.setNavigationOnClickListener { finish() }

        // inicializamos las preferencias de la informacion del usuario
        preferencesUserInfo?.sharedPreferences =
            getSharedPreferences(Constants.TAG_USER_INFO, Context.MODE_PRIVATE)
        preferencesUserInfo?.editor = preferencesUserInfo?.sharedPreferences?.edit()

        // capturamos el usuario que viene del intent
        userInput = intent.getSerializableExtra(Constants.KEY_USER_INTENT) as User
        tvNameUserPrivateChat.text = userInput?.name + " " + userInput?.lastName

        Glide.with(this)
            .load(userInput?.photo)
            .placeholder(R.drawable.ic_download)
            .into(ivUserPrivateChat)

        // obtenemos la info de nuestro usuario logueado
        currentUser = preferencesUserInfo?.getUserInfo()

        // instanciamos la referencia a la base de datos de firebase
        chatReference = database.reference.child(Constants.PRIVATE_MESSAGES)

        // chat
        mLinearLayoutManager = LinearLayoutManager(this)
        mLinearLayoutManager.stackFromEnd = true
        rvPrivateChat.layoutManager = mLinearLayoutManager
        callMethodsForChat()

        // agregamos metodos onclick
        fabSendChat.setOnClickListener {
            // creamos el objeto message a enviar
            val messageChat = MessageChat()
            messageChat.setName(currentUser?.name + " " + currentUser?.lastName)
            messageChat.setText(textinputSendMessage.editText?.text.toString())
            messageChat.setPhotoUrlUser(currentUser?.photo!!)
            messageChat.setHour(ActivityUtils.currentTimeToLong())
            messageChat.setIdUser(currentUser?.id ?: "CY7ivhfCKnT0FcrzKVe25T7yDx12")

            // enviamos el mensjae a la coleccion de firebase del emisor
            chatReference!!.child(currentUser!!.id).child(userInput!!.id).push().setValue(messageChat)

            // enviamos el mensjae a la coleccion de firebase del receptor
            chatReference!!.child(userInput!!.id).child(currentUser!!.id).push().setValue(messageChat)

            textinputSendMessage.editText?.setText("")

            // registramos el chat con el usuario seleccionado
            registerChatInCollectionFireStore()
        }

        textinputSendMessage.setEndIconOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, Constants.REQUEST_IMAGE)
        }

        // escuchamos los cambios del input para habilitar el boton de enviar
        textinputSendMessage.editText?.doOnTextChanged { inputText, _, _, _ ->
            fabSendChat.isEnabled = inputText.toString().trim().isNotEmpty()
        }
    }

    // envia el chat a registrarse para luego obetnerlos y mostrar los chats del usuario en una lista
    fun registerChatInCollectionFireStore() {
        currentUser?.time = ActivityUtils.currentTimeToLong()
        userInput?.time = ActivityUtils.currentTimeToLong()

        // registro para usuario actual
        firebaseFireStore.collection(Constants.USERS_CHATS).document(currentUser!!.id)
            .collection(Constants.MY_CHATS).document(userInput!!.id).set(userInput!!)

        // registro para usuario seleccionado
        firebaseFireStore.collection(Constants.USERS_CHATS).document(userInput!!.id)
            .collection(Constants.MY_CHATS).document(currentUser!!.id).set(currentUser!!)
    }

    fun callMethodsForChat() {

        val options: FirebaseRecyclerOptions<MessageChat> =
            FirebaseRecyclerOptions.Builder<MessageChat>()
                .setQuery(
                    chatReference!!.child(currentUser!!.id).child(userInput!!.id),
                    MessageChat::class.java
                )
                .setLifecycleOwner(this)
                .build()

        // creamos nuestro adaptador usando un adapter de UI
        // que nos provee 'com.firebaseui:firebase-ui-database:7.1.1'
        mFirebaseAdapter = object :
            FirebaseRecyclerAdapter<MessageChat, ChatMessageViewHolder>(
                options
            ) {
            override fun onCreateViewHolder(
                viewGroup: ViewGroup,
                i: Int
            ): ChatMessageViewHolder {
                val inflater = LayoutInflater.from(viewGroup.context)

                return ChatMessageViewHolder(
                    inflater.inflate(R.layout.item_message_chat, viewGroup, false)
                )
            }

            override fun onBindViewHolder(
                viewHolder: ChatMessageViewHolder,
                position: Int,
                messageChat: MessageChat
            ) {

                if (messageChat.getIdUser() == currentUser?.id) {
                    viewHolder.cardChat.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
                } else {
                    viewHolder.cardChat.setBackgroundColor(resources.getColor(R.color.white))
                }

                if (messageChat.getText() != null) {
                    viewHolder.messageTextView.text = messageChat.getText()
                    viewHolder.messageTextView.visibility = TextView.VISIBLE
                    viewHolder.messageImageView.visibility = ImageView.GONE
                } else if (messageChat.getImageUrl() != null) {
                    val imageUrl: String? = messageChat.getImageUrl()

                    if (imageUrl?.startsWith("gs://") == true) {
                        val storageReference = FirebaseStorage.getInstance()
                            .getReferenceFromUrl(imageUrl)

                        storageReference.downloadUrl.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val downloadUrl = task.result.toString()

                                Glide.with(viewHolder.messageImageView.context)
                                    .load(downloadUrl)
                                    .placeholder(R.drawable.ic_download)
                                    .into(viewHolder.messageImageView)
                            } else {
                                Log.w(
                                    resources.getString(R.string.error_download_urlimage),
                                    task.exception
                                )
                            }
                        }
                    } else {
                        Glide.with(viewHolder.messageImageView.context)
                            .load(messageChat.getImageUrl())
                            .placeholder(R.drawable.ic_download)
                            .into(viewHolder.messageImageView)
                    }

                    viewHolder.messageImageView.visibility = ImageView.VISIBLE
                    viewHolder.messageTextView.visibility = TextView.GONE
                }

                viewHolder.tvNameUserSendMessage.text = messageChat.getName()
                viewHolder.tvHourMessage.text = messageChat.getHour()?.let {
                    ActivityUtils.convertLongToTime(
                        it
                    )
                }

                if (messageChat.getPhotoUrlUser() == null) {
                    viewHolder.profileUserImageView.setImageDrawable(
                        ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.ic_account_circle_black_36dp
                        )

                    )
                } else {
                    Glide.with(applicationContext)
                        .load(messageChat.getPhotoUrlUser())
                        .placeholder(R.drawable.ic_download)
                        .into(viewHolder.profileUserImageView)

                }
            }
        }

        mFirebaseAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                val messageChatCount = mFirebaseAdapter?.itemCount
                val lastVisiblePosition: Int =
                    mLinearLayoutManager.findLastCompletelyVisibleItemPosition()

                // mostramos el ultimo mensaje agregado haciendo scroll
                if (messageChatCount != null) {
                    if (lastVisiblePosition == -1 ||
                        positionStart >= messageChatCount - 1 &&
                        lastVisiblePosition == positionStart - 1
                    ) {
                        rvPrivateChat.scrollToPosition(positionStart)
                    }
                }
            }
        })

        // asignamos el adapter a nuestro recycler
        rvPrivateChat.adapter = mFirebaseAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // validamos que se cargo bien la imagen de la galeria y enviamos el mensaje de chat
        if (requestCode == Constants.REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    val uri = data.data

                    val tempMessage = MessageChat()
                    tempMessage.setName(currentUser?.name + " " + currentUser?.lastName)
                    tempMessage.setPhotoUrlUser(currentUser?.photo!!)
                    tempMessage.setHour(ActivityUtils.currentTimeToLong())
                    tempMessage.setImageUrl(Constants.LOADING_IMAGE_URL)
                    tempMessage.setIdUser(currentUser?.id ?: "CY7ivhfCKnT0FcrzKVe25T7yDx12")

                    // enviamos el mensaje a la coleccion de firebase del emisor
                    chatReference!!.child(currentUser!!.id).child(userInput!!.id).push()
                        .setValue(
                            tempMessage
                        ) { databaseError, databaseReference ->
                            if (databaseError == null) {
                                // registramos el chat con el usuario seleccionado y la hora del ultimo mensaje
                                registerChatInCollectionFireStore()

                                val key = databaseReference.key
                                val storageReference = FirebaseStorage.getInstance()
                                    .getReference(currentUser!!.id)
                                    .child(key!!)
                                    .child(uri!!.lastPathSegment!!)
                                putImageInStorage(storageReference, uri, key)
                            }
                        }
                }
            }
        }
    }

    // enviamos la imagen a firestore y obtenemos la url de descarga
    private fun putImageInStorage(storageReference: StorageReference, uri: Uri, key: String) {
        storageReference.putFile(uri).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                task.result!!.metadata!!.reference!!.downloadUrl
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val tempMessage = MessageChat()
                            tempMessage.setName(currentUser?.name + " " + currentUser?.lastName)
                            tempMessage.setPhotoUrlUser(currentUser?.photo!!)
                            tempMessage.setHour(ActivityUtils.currentTimeToLong())
                            tempMessage.setImageUrl(task.result.toString())
                            tempMessage.setIdUser(currentUser!!.id)

                            // enviamos el mensaje a la coleccion de firebase del emisor
                            chatReference!!.child(currentUser!!.id).child(userInput!!.id)
                                .child(key)
                                .setValue(tempMessage)

                            // enviamos el mensaje a la coleccion de firebase del receptor
                            chatReference!!.child(userInput!!.id).child(currentUser!!.id)
                                .child(key)
                                .setValue(tempMessage)
                        }
                    }
            }
        }
    }
}