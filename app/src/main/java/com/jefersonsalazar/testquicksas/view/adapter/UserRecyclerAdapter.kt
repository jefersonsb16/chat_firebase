package com.jefersonsalazar.testquicksas.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jefersonsalazar.testquicksas.R
import com.jefersonsalazar.testquicksas.model.User
import com.jefersonsalazar.testquicksas.util.ActivityUtils
import com.jefersonsalazar.testquicksas.util.Constants
import de.hdodenhof.circleimageview.CircleImageView

class UserRecyclerAdapter(val userListener: UserListener, tvNotResults: TextView?, TAG: String) :
    RecyclerView.Adapter<UserRecyclerAdapter.ViewHolder>(), Filterable {

    var listUsers : ArrayList<User>? = null
    var filteredListUsers = ArrayList<User>()

    var tvNotResults: TextView? = tvNotResults
    val tag = TAG

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileUserChatImageView =
            itemView.findViewById<CircleImageView>(R.id.profileUserChatImageView)
        val tvNameUser = itemView.findViewById<TextView>(R.id.tvNameUser)
        val tvHourChat = itemView.findViewById<TextView>(R.id.tvHourChat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_private_chat, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = filteredListUsers[position]

        Glide.with(holder.itemView.context)
            .load(user.photo)
            .placeholder(R.drawable.ic_download)
            .into(holder.profileUserChatImageView)

        holder.tvNameUser.text = user.name + " " + user.lastName

        if (tag == Constants.TAG_FRAGMENT_CHATS) {
            holder.tvHourChat.text = user.time?.let { ActivityUtils.convertLongToTime(it) }
        } else {
            holder.tvHourChat.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            userListener.onUserClicked(user, position)
        }
    }

    fun updateData(users: List<User>) {
        if (listUsers == null) {
            listUsers = ArrayList()
            listUsers!!.addAll(users)
        }

        filteredListUsers.clear();
        filteredListUsers.addAll(users)
        notifyDataSetChanged()

        tvNotResults?.visibility = if (filteredListUsers.size == 0) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = filteredListUsers.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                var filteredList: MutableList<User> = ArrayList()

                filteredList = if (charString.isEmpty()) {
                    listUsers ?: ArrayList()
                } else {
                    listUsers!!.filter { user ->
                        user.name.contains(charString, ignoreCase = true) ||
                                user.lastName.contains(charString, ignoreCase = true)
                    } as MutableList<User>
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList

                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                if (filterResults.values != null) {
                    updateData(filterResults.values as ArrayList<User>)
                }
            }
        }
    }

}