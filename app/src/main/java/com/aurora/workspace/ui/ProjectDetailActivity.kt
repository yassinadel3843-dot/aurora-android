package com.aurora.workspace.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aurora.workspace.R
import com.aurora.workspace.data.Project
import com.aurora.workspace.data.Store
import com.aurora.workspace.databinding.ActivityProjectDetailBinding
import com.aurora.workspace.databinding.DialogTextInputBinding
import com.aurora.workspace.databinding.ItemTaskBinding
import com.aurora.workspace.util.DateText
import com.aurora.workspace.util.Priority
import com.aurora.workspace.util.colorAttr
import com.aurora.workspace.util.dp
import com.aurora.workspace.util.fitCenteredColumn
import com.aurora.workspace.util.withAlpha
import com.aurora.workspace.widgets.AvatarView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProjectDetailActivity : AuroraActivity() {

    companion object {
        private const val EXTRA_ID = "project_id"

        fun start(context: Context, projectId: String) {
            context.startActivity(
                Intent(context, ProjectDetailActivity::class.java)
                    .putExtra(EXTRA_ID, projectId)
            )
        }
    }

    private lateinit var binding: ActivityProjectDetailBinding
    private var projectId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        projectId = intent.getStringExtra(EXTRA_ID).orEmpty()
        if (Store.project(projectId) == null) {
            finish()
            return
        }

        binding = ActivityProjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdge()

        // Screen-size adaptation: centered content column on tablets.
        binding.detailContent.fitCenteredColumn(720)

        binding.backButton.setOnClickListener { finish() }
        binding.shareButton.setOnClickListener { shareProject() }
        binding.addTaskButton.setOnClickListener { showAddTaskDialog() }

        render()
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) render()
    }

    private fun render() {
        val project = Store.project(projectId) ?: return
        val b = binding

        b.projectEmoji.text = project.emoji
        b.projectEmoji.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(22f).toFloat()
            setColor(project.accent.withAlpha(44))
            setStroke(dp(1f), project.accent.withAlpha(90))
        }

        b.projectName.text = project.name
        b.projectDescription.text = project.description
        b.ring.setProgress(project.progress)
        b.progressLabel.text = getString(R.string.project_complete, project.progress)
        b.dueLabel.text = getString(R.string.project_due, DateText.due(this, project.dueLabel))
        b.membersLabel.text = getString(R.string.project_members, project.memberIds.size)
        b.tasksTitle.text = "${getString(R.string.detail_tasks)}  ${project.doneCount}/${project.tasks.size}"

        bindMembers(project)
        bindTasks(project)
    }

    private fun bindMembers(project: Project) {
        binding.memberAvatars.removeAllViews()
        val size = dp(38f)
        project.memberIds.take(5).forEachIndexed { index, id ->
            val member = Store.member(id)
            val avatar = AvatarView(this)
            val params = LinearLayout.LayoutParams(size, size)
            if (index > 0) params.marginStart = -dp(10f)
            avatar.layoutParams = params
            avatar.elevation = dp(2f).toFloat()
            avatar.bind(member?.name ?: "?", member?.color ?: project.accent)
            binding.memberAvatars.addView(avatar)
        }
    }

    private fun bindTasks(project: Project) {
        val container = binding.tasksContainer
        container.removeAllViews()
        binding.emptyTasks.visibility = if (project.tasks.isEmpty()) View.VISIBLE else View.GONE

        project.tasks.forEach { task ->
            val item = ItemTaskBinding.inflate(layoutInflater, container, false)
            item.taskTitle.text = task.title
            applyDoneStyle(item.taskTitle, task.done)

            if (task.priority == Store.PRIORITY_NONE) {
                item.priorityBadge.visibility = View.GONE
            } else {
                item.priorityBadge.visibility = View.VISIBLE
                item.priorityBadge.text = getString(Priority.labelRes(task.priority))
                val priorityColor = Priority.color(this, task.priority)
                item.priorityBadge.setTextColor(priorityColor)
                item.priorityBadge.background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(priorityColor.withAlpha(38))
                    setStroke(dp(1f), priorityColor.withAlpha(90))
                }
            }

            val tag = task.tag.trim()
            if (tag.isEmpty()) {
                item.taskTag.visibility = View.GONE
            } else {
                item.taskTag.visibility = View.VISIBLE
                item.taskTag.text = "#$tag"
            }

            item.checkIcon.visibility = if (task.done) View.VISIBLE else View.GONE
            item.checkCircle.background = ContextCompat.getDrawable(
                this,
                if (task.done) R.drawable.bg_check_done else R.drawable.bg_check_empty
            )

            val member = Store.member(task.assigneeId)
            if (member != null) {
                item.taskAvatar.visibility = View.VISIBLE
                item.taskAvatar.bind(member.name, member.color)
            } else {
                item.taskAvatar.visibility = View.GONE
            }

            item.root.setOnClickListener {
                if (Store.haptics) {
                    item.root.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                Store.toggleTask(this, project.id, task.id)
                render()
            }
            container.addView(item.root)
        }
    }

    private fun applyDoneStyle(textView: TextView, done: Boolean) {
        textView.paintFlags = if (done) {
            textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        textView.alpha = if (done) 0.5f else 1f
    }

    private fun showAddTaskDialog() {
        val dialogBinding = DialogTextInputBinding.inflate(layoutInflater)
        var selectedPriority = Store.PRIORITY_NONE
        val chips = listOf(
            dialogBinding.chipPriorityNone to Store.PRIORITY_NONE,
            dialogBinding.chipPriorityLow to Store.PRIORITY_LOW,
            dialogBinding.chipPriorityMedium to Store.PRIORITY_MEDIUM,
            dialogBinding.chipPriorityHigh to Store.PRIORITY_HIGH
        )

        fun styleChips() {
            chips.forEach { (chip, key) ->
                val selected = key == selectedPriority
                chip.setBackgroundResource(
                    if (selected) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected
                )
                chip.setTextColor(
                    if (selected) Color.WHITE else colorAttr(R.attr.auroraTextDim)
                )
            }
        }
        chips.forEach { (chip, key) ->
            chip.setOnClickListener {
                selectedPriority = key
                styleChips()
            }
        }
        styleChips()

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.new_task)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.add, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val title = dialogBinding.input.text.toString().trim()
                if (title.isEmpty()) {
                    dialogBinding.input.error = getString(R.string.task_name_hint)
                    return@setOnClickListener
                }
                val tag = dialogBinding.tagInput.text.toString().trim()
                Store.addTask(this, projectId, title, selectedPriority, tag)
                dialog.dismiss()
                render()
            }
        }
        dialog.show()
    }

    private fun shareProject() {
        val project = Store.project(projectId) ?: return
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.invite_message, project.name))
        }
        startActivity(Intent.createChooser(send, getString(R.string.invite)))
    }
}
