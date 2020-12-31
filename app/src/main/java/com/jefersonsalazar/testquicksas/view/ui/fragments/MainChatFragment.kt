package com.jefersonsalazar.testquicksas.view.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jefersonsalazar.testquicksas.R
import com.jefersonsalazar.testquicksas.model.MessageChat
import com.jefersonsalazar.testquicksas.model.User
import com.jefersonsalazar.testquicksas.network.Callback
import com.jefersonsalazar.testquicksas.util.ActivityUtils
import com.jefersonsalazar.testquicksas.util.Constants
import com.jefersonsalazar.testquicksas.util.Preferences
import com.jefersonsalazar.testquicksas.view.adapter.ChatMessageViewHolder
import com.jefersonsalazar.testquicksas.view.adapter.UserListener
import com.jefersonsalazar.testquicksas.view.adapter.UserRecyclerAdapter
import com.jefersonsalazar.testquicksas.view.ui.activities.PrivateChatActivity
import com.jefersonsalazar.testquicksas.viewmodel.MainChatViewModel
import kotlinx.android.synthetic.main.fragment_main_chat.*

class MainChatFragment : Fragment(), UserListener {

    private val preferencesUserInfo: Preferences? = Preferences.getInstance()
    private lateinit var mainChatViewModel: MainChatViewModel
    private lateinit var userRecyclerAdapter: UserRecyclerAdapter

    private var user: User? = null
    private var userFirebase: FirebaseUser? = null

    // propiedades para usar en recyclerview
    private var mLinearLayoutManager: LinearLayoutManager = LinearLayoutManager(activity)
    private var mFirebaseAdapter: FirebaseRecyclerAdapter<MessageChat, ChatMessageViewHolder>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_main_chat, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // inicializamos las preferencias de la informacion del usuario
        preferencesUserInfo?.sharedPreferences =
            activity?.getSharedPreferences(Constants.TAG_USER_INFO, Context.MODE_PRIVATE)
        preferencesUserInfo?.editor = preferencesUserInfo?.sharedPreferences?.edit()

        // instanciamos nuestro view model
        mainChatViewModel = ViewModelProviders.of(this).get(MainChatViewModel::class.java)
        userFirebase = mainChatViewModel?.fireStoreService.firebaseAuth.currentUser!!

        // consultamos los usuarios registrados
        userRecyclerAdapter = UserRecyclerAdapter(this, tvNotResults, Constants.TAG_FRAGMENT_MAIN)
        mainChatViewModel.getUsersFromFirebaseWithListener()

        // obtenemos la info de nuestro usuario logueado
        user = preferencesUserInfo?.getUserInfo()

        // asignamos un layout manager a nuestro recycler
        mLinearLayoutManager = LinearLayoutManager(activity)
        mLinearLayoutManager.stackFromEnd = true
        rvGroupChat.layoutManager = mLinearLayoutManager

        callMethodsForChat()

        // agregamos metodos onclick
        fabSend.setOnClickListener {
            // creamos el objeto message a enviar
            val messageChat = MessageChat()
            messageChat.setName(user?.name + " " + user?.lastName)
            messageChat.setText(textinputSendMessageGroup.editText?.text.toString())
            messageChat.setPhotoUrlUser(user?.photo!!)
            messageChat.setHour(ActivityUtils.currentTimeToLong())
            messageChat.setIdUser(userFirebase?.uid ?: "CY7ivhfCKnT0FcrzKVe25T7yDx12")

            mainChatViewModel.fireStoreService.databaseReferenceChatGroup
                .push().setValue(messageChat)
            textinputSendMessageGroup.editText?.setText("")
        }

        textinputSendMessageGroup.setEndIconOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, Constants.REQUEST_IMAGE)
        }

        // escuchamos los cambios del input para habilitar el boton de enviar
        textinputSendMessageGroup.editText?.doOnTextChanged { inputText, _, _, _ ->
            fabSend.isEnabled = inputText.toString().trim().isNotEmpty()
        }

        // observamos los cambios de users del view model
        rvUsers.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = userRecyclerAdapter
        }
        observerViewModel()

        // escuchamos los cambios del buscador para filtrar
        textinputSearchUserChat.editText?.doOnTextChanged { text, _, _, _ ->
            relativeLayoutUsers.visibility =
                if (text.toString().trim().isNotEmpty()) View.VISIBLE else View.GONE
            relativeLayoutGroupChat.visibility =
                if (text.toString().trim().isEmpty()) View.VISIBLE else View.GONE

            userRecyclerAdapter.filter.filter(text?.toString()?.trim())
        }
    }

    private fun observerViewModel() {
        mainChatViewModel.listUsers.observe(viewLifecycleOwner, { users ->
            userRecyclerAdapter.updateData(users)
        })
    }

    private fun callMethodsForChat() {

        val options: FirebaseRecyclerOptions<MessageChat> =
            FirebaseRecyclerOptions.Builder<MessageChat>()
                .setQuery(
                    mainChatViewModel.fireStoreService.databaseReferenceChatGroup,
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
                progressBarChatGroup.visibility = ProgressBar.INVISIBLE

                if (messageChat.getIdUser() == userFirebase?.uid) {
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
                        activity?.let { context ->
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_account_circle_black_36dp
                            )
                        }
                    )
                } else {
                    activity?.let { context ->
                        Glide.with(context)
                            .load(messageChat.getPhotoUrlUser())
                            .placeholder(R.drawable.ic_download)
                            .into(viewHolder.profileUserImageView)
                    }
                }
            }
        }

        mFirebaseAdapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
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
                        rvGroupChat.scrollToPosition(positionStart)
                    }
                }
            }
        })

        // asignamos el adapter a nuestro recycler
        rvGroupChat.adapter = mFirebaseAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // validamos que se cargo bien la imagen de la galeria y enviamos el mensaje de chat
        if (requestCode == Constants.REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    val uri = data.data

                    val tempMessage = MessageChat()
                    tempMessage.setName(user?.name + " " + user?.lastName)
                    tempMessage.setPhotoUrlUser(user?.photo!!)
                    tempMessage.setHour(ActivityUtils.currentTimeToLong())
                    tempMessage.setImageUrl(Constants.LOADING_IMAGE_URL)
                    tempMessage.setIdUser(userFirebase?.uid ?: "CY7ivhfCKnT0FcrzKVe25T7yDx12")

                    mainChatViewModel?.fireStoreService.databaseReferenceChatGroup.push()
                        .setValue(
                            tempMessage
                        ) { databaseError, databaseReference ->
                            if (databaseError == null) {
                                val key = databaseReference.key
                                val storageReference = FirebaseStorage.getInstance()
                                    .getReference(
                                        userFirebase?.uid ?: "CY7ivhfCKnT0FcrzKVe25T7yDx12"
                                    )
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
        storageReference.putFile(uri).addOnCompleteListener(
            activity!!
        ) { task ->
            if (task.isSuccessful) {
                task.result!!.metadata!!.reference!!.downloadUrl
                    .addOnCompleteListener(
                        activity!!
                    ) { task ->
                        if (task.isSuccessful) {
                            val tempMessage = MessageChat()
                            tempMessage.setName(user?.name + " " + user?.lastName)
                            tempMessage.setPhotoUrlUser(user?.photo!!)
                            tempMessage.setHour(ActivityUtils.currentTimeToLong())
                            tempMessage.setImageUrl(task.result.toString())
                            tempMessage.setIdUser(
                                userFirebase?.uid ?: "CY7ivhfCKnT0FcrzKVe25T7yDx12"
                            )

                            mainChatViewModel.fireStoreService.databaseReferenceChatGroup
                                .child(key)
                                .setValue(tempMessage)
                        }
                    }
            }
        }
    }

    override fun onUserClicked(user: User, position: Int) {
        ActivityUtils.showDialog(
            activity!!,
            true,
            getString(R.string.text_send_private_chat, user?.name + " " + user?.lastName),
            getString(R.string.text_yes),
            object :
                Callback<Boolean> {
                override fun onSuccess(result: Boolean?) {
                    val intent = Intent(activity, PrivateChatActivity::class.java)
                    intent.putExtra(Constants.KEY_USER_INTENT, user)
                    startActivity(intent)
                    textinputSearchUserChat.editText?.setText("")
                }

                override fun onFailed(exception: String) {
                    // sin implementacion
                }
            }
        )
    }

}