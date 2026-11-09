package com.aurora.workspace.ui.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.aurora.workspace.R
import com.aurora.workspace.data.Conversation
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

        // The header icon used to show a "Search" toast with nothing behind it.
        // It now opens an inline field that filters the list as you type.
        binding.searchButton.setOnClickListener { toggleSearch() }
        binding.searchClose.setOnClickListener { closeSearch() }
        binding.searchInput.doAfterTextChanged { bind() }
        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            } else {
                false
            }
        }

        bind()
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) bind()
    }

    private fun toggleSearch() {
        if (binding.searchBar.visibility == View.VISIBLE) closeSearch() else openSearch()
    }

    private fun openSearch() {
        binding.searchBar.visibility = View.VISIBLE
        binding.searchInput.requestFocus()
        binding.searchInput.post {
            keyboard().showSoftInput(binding.searchInput, InputMethodManager.SHOW_IMPLICIT)
        }
        bind()
    }

    private fun closeSearch() {
        binding.searchInput.setText("")
        binding.searchInput.clearFocus()
        hideKeyboard()
        binding.searchBar.visibility = View.GONE
        bind()
    }

    private fun hideKeyboard() {
        keyboard().hideSoftInputFromWindow(binding.searchInput.windowToken, 0)
    }

    private fun keyboard(): InputMethodManager =
        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    private fun currentQuery(): String =
        if (binding.searchBar.visibility == View.VISIBLE) {
            binding.searchInput.text.toString().trim()
        } else {
            ""
        }

    private fun bind() {
        val query = currentQuery()
        val list = if (query.isEmpty()) {
            Store.conversations.toList()
        } else {
            Store.conversations.filter { matches(it, query) }
        }

        adapter.submit(list)
        val empty = list.isEmpty()
        binding.emptyView.visibility = if (empty) View.VISIBLE else View.GONE
        if (empty) {
            binding.emptyView.setText(
                if (query.isEmpty()) R.string.no_messages else R.string.no_search_results
            )
        }
    }

    /** Title, preview, the other member's name and every message body count. */
    private fun matches(conversation: Conversation, query: String): Boolean {
        val haystack = buildString {
            append(conversation.title).append(' ').append(conversation.preview)
            Store.member(conversation.memberId)?.name?.let { append(' ').append(it) }
            conversation.messages.forEach { append(' ').append(it.text) }
        }
        return haystack.contains(query, ignoreCase = true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
