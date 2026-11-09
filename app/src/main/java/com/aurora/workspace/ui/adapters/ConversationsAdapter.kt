package com.aurora.workspace.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aurora.workspace.R
import com.aurora.workspace.data.Conversation
import com.aurora.workspace.data.Store
import com.aurora.workspace.databinding.ItemConversationBinding

class ConversationsAdapter(
    private val onClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationsAdapter.VH>() {

    private val items = mutableListOf<Conversation>()

    @SuppressLint("NotifyDataSetChanged")
    fun submit(list: List<Conversation>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemConversationBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val conversation = items[position]
        val context: Context = holder.b.root.context
        val b = holder.b

        val member = Store.member(conversation.memberId)
        if (conversation.isGroup) {
            b.avatar.bind(initialsFor(conversation.title), conversationColor(conversation.id), false)
        } else {
            b.avatar.bind(member?.name ?: conversation.title, member?.color ?: 0xFF4DA3FF.toInt(), member?.online == true)
        }

        b.title.text = conversation.title
        b.preview.text = conversation.preview
        b.time.text = conversation.time
        b.onlineDot.visibility =
            if (!conversation.isGroup && member?.online == true) View.VISIBLE else View.GONE

        if (conversation.unread > 0) {
            b.unread.visibility = View.VISIBLE
            b.unread.text = if (conversation.unread > 9) "9+" else conversation.unread.toString()
        } else {
            b.unread.visibility = View.GONE
        }

        b.root.setOnClickListener { onClick(conversation) }
    }

    private fun initialsFor(title: String): String =
        title.trim().take(2).uppercase()

    /**
     * Colour is derived from the conversation id, not from the row position:
     * a filter (chat search) or a new message must not repaint a group's avatar.
     */
    private fun conversationColor(id: String): Int {
        val palette = intArrayOf(
            0xFF4DA3FF.toInt(),
            0xFF7C5CFF.toInt(),
            0xFF35E0C8.toInt(),
            0xFFFF8A5B.toInt()
        )
        return palette[Math.floorMod(id.hashCode(), palette.size)]
    }
}
