package com.jefersonsalazar.testquicksas.view.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.jefersonsalazar.testquicksas.R
import com.jefersonsalazar.testquicksas.model.User
import com.jefersonsalazar.testquicksas.util.Constants
import com.jefersonsalazar.testquicksas.util.Preferences
import com.jefersonsalazar.testquicksas.view.adapter.UserListener
import com.jefersonsalazar.testquicksas.view.adapter.UserRecyclerAdapter
import com.jefersonsalazar.testquicksas.view.ui.activities.PrivateChatActivity
import com.jefersonsalazar.testquicksas.viewmodel.ListChatsCurrentUserViewModel
import kotlinx.android.synthetic.main.fragment_list_personal_chat.*

class ListPersonalChatFragment : Fragment(), UserListener {

    private val preferencesUserInfo: Preferences? = Preferences.getInstance()
    private lateinit var listChatViewModel: ListChatsCurrentUserViewModel
    private lateinit var chatsRecyclerAdapter: UserRecyclerAdapter

    private var user: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_list_personal_chat, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // inicializamos las preferencias de la informacion del usuario
        preferencesUserInfo?.sharedPreferences =
            activity?.getSharedPreferences(Constants.TAG_USER_INFO, Context.MODE_PRIVATE)
        preferencesUserInfo?.editor = preferencesUserInfo?.sharedPreferences?.edit()

        // instanciamos nuestro view model
        listChatViewModel = ViewModelProviders.of(this).get(ListChatsCurrentUserViewModel::class.java)

        // consultamos los usuarios registrados
        chatsRecyclerAdapter = UserRecyclerAdapter(this, tvNotChats, Constants.TAG_FRAGMENT_CHATS)
        listChatViewModel.getChatsCurrentUserFromFirebase()

        // obtenemos la info de nuestro usuario logueado
        user = preferencesUserInfo?.getUserInfo()

        // observamos los cambios de users del view model
        rvChats.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = chatsRecyclerAdapter
        }
        observerViewModel()
    }

    private fun observerViewModel() {
        listChatViewModel.listChats.observe(viewLifecycleOwner, { users ->
            chatsRecyclerAdapter.updateData(users)
        })

        listChatViewModel.isLoading.observe(viewLifecycleOwner, {
            if (it != null) {
                progressBarListChats.visibility = View.GONE
            }
        })
    }

    override fun onUserClicked(user: User, position: Int) {
        val intent = Intent(activity, PrivateChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER_INTENT, user)
        startActivity(intent)
    }
}