package com.topmortar.topmortarsales.adapter

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.model.UserModel

class UsersRecyclerViewAdapter(private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<UsersRecyclerViewAdapter.ViewHolder>() {
    private var listItem: ArrayList<UserModel> = ArrayList()
    private var context: Context? = null

    interface ItemClickListener {
        fun onItemClick(data: UserModel? = null)
    }

    fun setListItem(listItem: ArrayList<UserModel>) {
        this.listItem = listItem
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val ivProfile: ImageView = itemView.findViewById(R.id.iv_contact_profile)
        private val tvContactName: TextView = itemView.findViewById(R.id.tv_contact_name)
        private val tvPhoneNumber: TextView = itemView.findViewById(R.id.tv_phone_number)

        fun bind(item: UserModel) {

            ivProfile.setImageResource(R.drawable.person_red)
            tvContactName.text = item.full_name
            tvPhoneNumber.text = "${ item.level_user.toLowerCase() } - ${ item.kode_city.toLowerCase() }"

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_room, parent, false)
        context = parent.context
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = listItem[position]

        holder.bind(item)
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.rv_item_fade_slide_up))

        holder.itemView.setOnClickListener {

            if (position != RecyclerView.NO_POSITION) {
                val animateDuration = 200L

                val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                fadeIn.duration = animateDuration

                val overlayView = holder.itemView.findViewById<LinearLayout>(R.id.overlay_view)

                overlayView.alpha = 0.7f
                overlayView.visibility = View.VISIBLE
                overlayView.startAnimation(fadeIn)

                Handler().postDelayed({
                    overlayView.alpha = 0f
                    overlayView.visibility = View.GONE
                }, animateDuration)

                val data = listItem[position]
                itemClickListener.onItemClick(data)

            }

        }

    }

    override fun getItemCount(): Int = listItem.size
}