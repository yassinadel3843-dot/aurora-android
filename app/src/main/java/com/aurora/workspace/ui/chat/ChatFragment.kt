package com.aurora.workspace.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.aurora.workspace.R
import com.aurora.workspace.data.Store
import com.aurora.workspace.databinding.FragmentChatBinding
import com.aurora.workspace.ui.ChatActivity
import com.aurora.workspace.ui.adapters.ConversationsAdapter
import com.aurora.workspace.util.fitCenteredColumn

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ConversationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Screen-size adaptation: centered content column + grid list on tablets.
        view.fitCenteredColumn(720)

        adapter = ConversationsAdapter { conversation ->
            ChatActivity.start(requireContext(), conversation.id)
        }
        binding.conversationsList.layoutManager = LinearLayoutManager(requireContext())
        binding.conversationsList.adapter = adapter

        binding.searchButton.setOnClickListener {
            Toast.makeText(requireContext(), R.string.search, Toast.LENGTH_SHORT).show()
        }

        bind()
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) bind()
    }

    private fun bind() {
        adapter.submit(Store.conversations)
        binding.emptyView.visibility =
            if (Store.conversations.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
