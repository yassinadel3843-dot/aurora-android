package com.aurora.workspace.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aurora.workspace.R
import com.aurora.workspace.data.Project
import com.aurora.workspace.databinding.ItemProjectMiniBinding

class MiniProjectsAdapter(
    private val onClick: (Project) -> Unit
) : RecyclerView.Adapter<MiniProjectsAdapter.VH>() {

    private val items = mutableListOf<Project>()

    @SuppressLint("NotifyDataSetChanged")
    fun submit(list: List<Project>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemProjectMiniBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemProjectMiniBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val project = items[position]
        val context = holder.b.root.context
        holder.b.emoji.text = project.emoji
        holder.b.name.text = project.name
        holder.b.description.text = project.description
        holder.b.progress.setProgressCompat(project.progress, false)
        holder.b.progressLabel.text = context.getString(R.string.percent_format, project.progress)
        holder.b.tasks.text = context.getString(R.string.project_tasks, project.tasks.size)
        holder.b.root.setOnClickListener { onClick(project) }
    }
}
