package com.aurora.workspace.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.aurora.workspace.R
import com.aurora.workspace.data.Store
import com.aurora.workspace.databinding.ActivityMainBinding
import com.aurora.workspace.ui.chat.ChatFragment
import com.aurora.workspace.ui.home.HomeFragment
import com.aurora.workspace.ui.profile.ProfileFragment
import com.aurora.workspace.ui.projects.ProjectsFragment
import com.aurora.workspace.util.colorAttr
import com.aurora.workspace.util.fitCenteredColumn

class MainActivity : AuroraActivity() {

    companion object {
        private const val KEY_TAB = "selected_tab"
        const val TAB_HOME = "home"
        const val TAB_PROJECTS = "projects"
        const val TAB_CHAT = "chat"
        const val TAB_PROFILE = "profile"
    }

    private lateinit var binding: ActivityMainBinding
    private var tab = TAB_HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Store.signedIn) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdge()

        // Screen-size adaptation: on tablets the content becomes a centered
        // column and the glass nav bar keeps a phone-like width.
        binding.safeArea.fitCenteredColumn(720)
        binding.navBar.fitCenteredColumn(480)

        binding.navHome.setOnClickListener { select(TAB_HOME) }
        binding.navProjects.setOnClickListener { select(TAB_PROJECTS) }
        binding.navChat.setOnClickListener { select(TAB_CHAT) }
        binding.navProfile.setOnClickListener { select(TAB_PROFILE) }

        val restored = savedInstanceState?.getString(KEY_TAB)
        if (restored != null) tab = restored
        select(tab, animate = false)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (tab != TAB_HOME) {
                    select(TAB_HOME)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_TAB, tab)
        super.onSaveInstanceState(outState)
    }

    fun select(target: String, animate: Boolean = true) {
        val alreadyShowing = supportFragmentManager.findFragmentById(R.id.fragmentContainer) != null
        if (target == tab && alreadyShowing) {
            updateNav()
            return
        }
        tab = target

        val fragment: Fragment = when (target) {
            TAB_PROJECTS -> ProjectsFragment()
            TAB_CHAT -> ChatFragment()
            TAB_PROFILE -> ProfileFragment()
            else -> HomeFragment()
        }

        val transaction = supportFragmentManager.beginTransaction()
        // Cold start and state restore swap instantly so the app opens on a
        // settled screen; only real tab taps get the settling motion.
        if (animate) {
            transaction.setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
        }
        transaction.replace(R.id.fragmentContainer, fragment).commit()

        updateNav()
        if (animate) animateIcon(iconFor(target))
    }

    private fun iconFor(target: String): ImageView = when (target) {
        TAB_PROJECTS -> binding.navProjectsIcon
        TAB_CHAT -> binding.navChatIcon
        TAB_PROFILE -> binding.navProfileIcon
        else -> binding.navHomeIcon
    }

    private fun updateNav() {
        val accent = colorAttr(R.attr.auroraAccent)
        val dim = colorAttr(R.attr.auroraTextDim)

        val entries = listOf(
            Triple(TAB_HOME, binding.navHomeIcon, binding.navHomeLabel),
            Triple(TAB_PROJECTS, binding.navProjectsIcon, binding.navProjectsLabel),
            Triple(TAB_CHAT, binding.navChatIcon, binding.navChatLabel),
            Triple(TAB_PROFILE, binding.navProfileIcon, binding.navProfileLabel)
        )

        entries.forEach { (key, icon, label) ->
            val selected = key == tab
            val color = if (selected) accent else dim
            icon.imageTintList = ColorStateList.valueOf(color)
            label.setTextColor(color)
            label.alpha = if (selected) 1f else 0.78f
            // Selected tab sits slightly forward: a persistent gentle scale
            // (the bounce animation on tap keeps the playful feel).
            val targetScale = if (selected) 1.08f else 1f
            if (icon.scaleX != targetScale || icon.scaleY != targetScale) {
                icon.animate().scaleX(targetScale).scaleY(targetScale).setDuration(160).start()
            }
        }
    }

    private fun animateIcon(icon: ImageView) {
        icon.animate()
            .scaleX(1.16f).scaleY(1.16f)
            .setDuration(120)
            .withEndAction {
                icon.animate().scaleX(1f).scaleY(1f).setDuration(170).start()
            }
            .start()
    }
}
