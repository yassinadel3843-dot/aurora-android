package com.aurora.workspace.ui.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.aurora.workspace.R
import com.aurora.workspace.data.Message
import com.aurora.workspace.data.Store
import com.aurora.workspace.databinding.ItemMessageBinding
import com.aurora.workspace.util.colorAttr

class MessagesAdapter : RecyclerView.Adapter<MessagesAdapter.VH>() {

    private val items = mutableListOf<Message>()

    @SuppressLint("NotifyDataSetChanged")
    fun submit(list: List<Message>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemMessageBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val message = items[position]
        val b = holder.b
        val context = b.root.context
        val mine = message.mine

        val params = b.messageRow.layoutParams as FrameLayout.LayoutParams
        params.gravity = if (mine) Gravity.END else Gravity.START
        b.messageRow.layoutParams = params

        b.avatar.visibility = if (mine) View.GONE else View.VISIBLE
        b.author.visibility = if (mine) View.GONE else View.VISIBLE
        b.bubble.setBackgroundResource(
            if (mine) R.drawable.bg_bubble_out else R.drawable.bg_bubble_in
        )
        b.bubble.setTextColor(
            if (mine) Color.WHITE else context.colorAttr(R.attr.auroraText)
        )

        if (!mine) {
            val member = Store.member(message.authorId)
            b.avatar.bind(member?.name ?: "?", member?.color ?: 0xFF7C5CFF.toInt())
            b.author.text = member?.name ?: ""
        }

        b.bubble.text = message.text
        b.time.text = message.time
    }
}
