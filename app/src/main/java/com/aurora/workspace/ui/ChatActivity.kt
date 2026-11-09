package com.aurora.workspace.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import com.aurora.workspace.R
import com.aurora.workspace.data.Store
import com.aurora.workspace.databinding.ActivityChatBinding
import com.aurora.workspace.ui.adapters.MessagesAdapter
import com.aurora.workspace.util.fitCenteredColumn

class ChatActivity : AuroraActivity() {

    companion object {
        private const val EXTRA_ID = "conversation_id"

        fun start(context: Context, conversationId: String) {
            context.startActivity(
                Intent(context, ChatActivity::class.java)
                    .putExtra(EXTRA_ID, conversationId)
            )
        }
    }

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: MessagesAdapter
    private var conversationId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        conversationId = intent.getStringExtra(EXTRA_ID).orEmpty()
        val conversation = Store.conversation(conversationId)
        if (conversation == null) {
            finish()
            return
        }

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdge()

        // Screen-size adaptation: centered conversation column on tablets.
        binding.safeArea.fitCenteredColumn(720)

        adapter = MessagesAdapter()
        binding.messages.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        binding.messages.adapter = adapter

        binding.backButton.setOnClickListener { finish() }
        binding.sendButton.setOnClickListener { send() }
        binding.input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                send()
                true
            } else {
                false
            }
        }

        binding.chatName.text = conversation.title
        val member = Store.member(conversation.memberId)
        if (conversation.isGroup) {
            binding.chatAvatar.bind(
                conversation.title.take(2).uppercase(),
                0xFF7C5CFF.toInt()
            )
            val people = conversation.messages.map { it.authorId }.distinct().size
            binding.chatStatus.text = getString(R.string.project_members, people)
        } else {
            binding.chatAvatar.bind(
                member?.name ?: conversation.title,
                member?.color ?: 0xFF4DA3FF.toInt(),
                member?.online == true
            )
            binding.chatStatus.text = if (member?.online == true) {
                getString(R.string.online)
            } else {
                getString(R.string.last_seen)
            }
        }

        Store.markRead(this, conversationId)
        render()
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) render()
    }

    private fun render() {
        val conversation = Store.conversation(conversationId) ?: return
        adapter.submit(conversation.messages)
        if (conversation.messages.isNotEmpty()) {
            binding.messages.post {
                binding.messages.scrollToPosition(conversation.messages.size - 1)
            }
        }
    }

    private fun send() {
        val text = binding.input.text.toString().trim()
        if (text.isEmpty()) return
        if (Store.haptics) {
            binding.sendButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        Store.sendMessage(this, conversationId, text)
        binding.input.setText("")
        render()
    }
}
