package com.aurora.workspace.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.aurora.workspace.R
import com.aurora.workspace.data.Store
import com.aurora.workspace.databinding.FragmentHomeBinding
import com.aurora.workspace.ui.MainActivity
import com.aurora.workspace.ui.ProjectDetailActivity
import com.aurora.workspace.util.fitCenteredColumn
import com.aurora.workspace.ui.adapters.MiniProjectsAdapter
import com.aurora.workspace.util.dp
import com.aurora.workspace.widgets.AvatarView
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MiniProjectsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Screen-size adaptation: centered content column on tablets.
        binding.homeContent.fitCenteredColumn(720)

        adapter = MiniProjectsAdapter { project ->
            ProjectDetailActivity.start(requireContext(), project.id)
        }
        binding.projectsList.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
        binding.projectsList.adapter = adapter
        binding.projectsList.isNestedScrollingEnabled = false

        binding.seeAll.setOnClickListener { openProjects() }
        binding.focusCard.setOnClickListener { openProjects() }
        binding.quickAdd.setOnClickListener { openProjects() }
        binding.bellButton.setOnClickListener {
            val activity = activity as? MainActivity
            activity?.select(MainActivity.TAB_PROFILE)
        }
        binding.profileAvatar.setOnClickListener {
            (activity as? MainActivity)?.select(MainActivity.TAB_PROFILE)
        }

        bind()
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) bind()
    }

    private fun openProjects() {
        (activity as? MainActivity)?.select(MainActivity.TAB_PROJECTS)
    }

    private fun bind() {
        val b = binding

        b.profileAvatar.bind(Store.userName, Store.userColor)
        b.greeting.text = getString(greetingRes())
        b.userName.text = Store.userName

        b.focusRing.setProgress(Store.overallProgress)
        b.focusCount.text = getString(R.string.focus_count, Store.doneTasks, Store.totalTasks)
        b.streakChip.text = getString(R.string.day_streak, Store.streak)

        b.statProjects.text = Store.activeProjects.size.toString()
        b.statWeekly.text = getString(R.string.percent_format, Store.overallProgress)

        adapter.submit(Store.activeProjects.take(5))
        bindCircle()
    }

    private fun bindCircle() {
        val container = binding.circleContainer
        container.removeAllViews()
        val size = requireContext().dp(40f)
        Store.members
            .filter { it.id != "me" }
            .take(5)
            .forEachIndexed { index, member ->
                val avatar = AvatarView(requireContext())
                val params = LinearLayout.LayoutParams(size, size)
                if (index > 0) params.marginStart = -requireContext().dp(10f)
                avatar.layoutParams = params
                avatar.elevation = requireContext().dp(2f).toFloat()
                avatar.bind(member.name, member.color, member.online)
                container.addView(avatar)
            }
    }

    private fun greetingRes(): Int {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> R.string.greeting_morning
            hour < 18 -> R.string.greeting_afternoon
            else -> R.string.greeting_evening
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
