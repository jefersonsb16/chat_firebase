package com.jefersonsalazar.testquicksas.view.adapter

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jefersonsalazar.testquicksas.R
import de.hdodenhof.circleimageview.CircleImageView

class ChatMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var profileUserImageView: CircleImageView = itemView.findViewById(R.id.profileUserImageView)
    var messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
    var messageImageView: ImageView = itemView.findViewById(R.id.messageImageView)
    var tvNameUserSendMessage: TextView = itemView.findViewById(R.id.tvNameUserSendMessage)
    var tvHourMessage: TextView = itemView.findViewById(R.id.tvHourMessage)
    var cardChat: LinearLayout = itemView.findViewById(R.id.cardChat)

}