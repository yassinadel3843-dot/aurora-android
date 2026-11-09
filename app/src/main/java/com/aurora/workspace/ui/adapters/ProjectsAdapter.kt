package com.aurora.workspace.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aurora.workspace.R
import com.aurora.workspace.data.Project
import com.aurora.workspace.data.Store
import com.aurora.workspace.databinding.ItemProjectBinding
import com.aurora.workspace.util.DateText
import com.aurora.workspace.util.dp
import com.aurora.workspace.util.withAlpha
import com.aurora.workspace.widgets.AvatarView

class ProjectsAdapter(
    private val onClick: (Project) -> Unit
) : RecyclerView.Adapter<ProjectsAdapter.VH>() {

    private val items = mutableListOf<Project>()

    @SuppressLint("NotifyDataSetChanged")
    fun submit(list: List<Project>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemProjectBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemProjectBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val project = items[position]
        val context = holder.b.root.context
        val b = holder.b

        b.emoji.text = project.emoji
        b.emoji.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = context.dp(17f).toFloat()
            setColor(project.accent.withAlpha(42))
            setStroke(context.dp(1f), project.accent.withAlpha(80))
        }

        b.name.text = project.name
        b.description.text = project.description
        b.taskCount.text = context.getString(R.string.project_tasks, project.tasks.size)
        b.percent.text = context.getString(R.string.project_complete, project.progress)
        b.due.text = context.getString(R.string.project_due, DateText.due(context, project.dueLabel))
        b.progress.setProgressCompat(project.progress, false)

        bindAvatars(context, b.avatars, project)
        b.root.setOnClickListener { onClick(project) }
    }

    private fun bindAvatars(context: Context, container: LinearLayout, project: Project) {
        container.removeAllViews()
        val size = context.dp(26f)
        project.memberIds.take(3).forEachIndexed { index, id ->
            val member = Store.member(id)
            val avatar = AvatarView(context)
            val params = LinearLayout.LayoutParams(size, size)
            if (index > 0) params.marginStart = -context.dp(8f)
            avatar.layoutParams = params
            avatar.bind(member?.name ?: "?", member?.color ?: project.accent)
            avatar.elevation = context.dp(2f).toFloat()
            container.addView(avatar)
        }
        if (project.memberIds.size > 3) {
            val more = TextView(context).apply {
                text = "+${project.memberIds.size - 3}"
                textSize = 9f
                gravity = android.view.Gravity.CENTER
                setTextColor(project.accent)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(project.accent.withAlpha(40))
                }
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginStart = -context.dp(8f)
                }
            }
            container.addView(more)
        }
    }
}
