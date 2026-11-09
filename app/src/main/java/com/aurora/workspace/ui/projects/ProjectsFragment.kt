package com.aurora.workspace.ui.projects

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.aurora.workspace.R
import com.aurora.workspace.data.Project
import com.aurora.workspace.data.Store
import com.aurora.workspace.databinding.DialogNewProjectBinding
import com.aurora.workspace.databinding.FragmentProjectsBinding
import com.aurora.workspace.ui.ProjectDetailActivity
import com.aurora.workspace.ui.adapters.ProjectsAdapter
import com.aurora.workspace.util.colorAttr
import com.aurora.workspace.util.dp
import com.aurora.workspace.util.fitCenteredColumn
import com.aurora.workspace.util.gridSpan
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProjectsFragment : Fragment() {

    private var _binding: FragmentProjectsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProjectsAdapter
    private var filter = FILTER_ALL

    companion object {
        private const val FILTER_ALL = "all"
        private const val FILTER_ACTIVE = "active"
        private const val FILTER_DONE = "done"

        /** The palette the "new project" dialog offers (was random before). */
        private val PROJECT_EMOJIS = listOf(
            "\uD83C\uDF0C", "\uD83C\uDFA8", "\uD83D\uDCF1",
            "\uD83D\uDE80", "\u2728", "\uD83E\uDDED", "\uD83C\uDF3F"
        )
        private val PROJECT_ACCENTS = intArrayOf(
            0xFF4DA3FF.toInt(),
            0xFF7C5CFF.toInt(),
            0xFF35E0C8.toInt(),
            0xFFFF8A5B.toInt(),
            0xFF37D67A.toInt()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Screen-size adaptation: centered content column + grid list on tablets.
        view.fitCenteredColumn(720)

        adapter = ProjectsAdapter { project ->
            ProjectDetailActivity.start(requireContext(), project.id)
        }
        binding.projectsList.layoutManager = GridLayoutManager(requireContext(), binding.root.gridSpan())
        binding.projectsList.adapter = adapter

        binding.chipAll.setOnClickListener { setFilter(FILTER_ALL) }
        binding.chipActive.setOnClickListener { setFilter(FILTER_ACTIVE) }
        binding.chipDone.setOnClickListener { setFilter(FILTER_DONE) }
        binding.addButton.setOnClickListener { showNewProjectDialog() }

        setFilter(FILTER_ALL)
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) bind()
    }

    private fun setFilter(value: String) {
        filter = value
        styleChip(binding.chipAll, value == FILTER_ALL)
        styleChip(binding.chipActive, value == FILTER_ACTIVE)
        styleChip(binding.chipDone, value == FILTER_DONE)
        bind()
    }

    private fun styleChip(chip: TextView, selected: Boolean) {
        chip.setBackgroundResource(
            if (selected) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected
        )
        chip.setTextColor(
            if (selected) Color.WHITE else requireContext().colorAttr(R.attr.auroraTextDim)
        )
    }

    private fun bind() {
        val list: List<Project> = when (filter) {
            FILTER_ACTIVE -> Store.projects.filter { !it.isDone }
            FILTER_DONE -> Store.projects.filter { it.isDone }
            else -> Store.projects.toList()
        }
        adapter.submit(list)
        binding.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showNewProjectDialog() {
        val dialogBinding = DialogNewProjectBinding.inflate(layoutInflater)
        var emoji = PROJECT_EMOJIS.first()
        var accent = PROJECT_ACCENTS.first()

        // The dialog used to roll a random emoji and accent, so two projects
        // created back to back could look identical by chance and the user had
        // no say. Both rows now show the real choice, seeded on the first entry.
        fun renderEmojis() {
            val row = dialogBinding.emojiRow
            row.removeAllViews()
            val size = requireContext().dp(42f)
            PROJECT_EMOJIS.forEach { candidate ->
                val chip = TextView(requireContext()).apply {
                    text = candidate
                    textSize = 19f
                    gravity = Gravity.CENTER
                    setBackgroundResource(
                        if (candidate == emoji) R.drawable.bg_chip_selected
                        else R.drawable.bg_chip_unselected
                    )
                    layoutParams = LinearLayout.LayoutParams(size, size).apply {
                        marginEnd = requireContext().dp(6f)
                    }
                    isClickable = true
                    contentDescription = getString(R.string.project_icon)
                    setOnClickListener {
                        emoji = candidate
                        renderEmojis()
                    }
                }
                row.addView(chip)
            }
        }

        fun renderAccents() {
            val row = dialogBinding.accentRow
            row.removeAllViews()
            val size = requireContext().dp(36f)
            PROJECT_ACCENTS.forEach { candidate ->
                val swatch = View(requireContext()).apply {
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(candidate)
                        setStroke(
                            requireContext().dp(if (candidate == accent) 2.5f else 1f),
                            if (candidate == accent) Color.WHITE else 0x40FFFFFF
                        )
                    }
                    layoutParams = LinearLayout.LayoutParams(size, size).apply {
                        marginEnd = requireContext().dp(10f)
                    }
                    isClickable = true
                    contentDescription = getString(R.string.project_accent)
                    setOnClickListener {
                        accent = candidate
                        renderAccents()
                    }
                }
                row.addView(swatch)
            }
        }

        renderEmojis()
        renderAccents()

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.new_project)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.create, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.nameInput.text.toString().trim()
                if (name.isEmpty()) {
                    dialogBinding.nameInput.error = getString(R.string.project_name_required)
                    return@setOnClickListener
                }
                val description = dialogBinding.descriptionInput.text.toString().trim()
                    .ifBlank { getString(R.string.project_desc_empty) }

                Store.addProject(requireContext(), name, description, emoji, accent)
                dialog.dismiss()
                setFilter(FILTER_ALL)
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
