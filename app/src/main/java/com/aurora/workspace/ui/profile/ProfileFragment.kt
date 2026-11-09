package com.aurora.workspace.ui.profile

import android.Manifest
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.aurora.workspace.R
import com.aurora.workspace.data.Store
import com.aurora.workspace.databinding.FragmentProfileBinding
import com.aurora.workspace.theme.LocaleHelper
import com.aurora.workspace.theme.ThemeManager
import com.aurora.workspace.ui.AuroraActivity
import com.aurora.workspace.ui.AuthActivity
import com.aurora.workspace.util.GoogleAccountHelper
import com.aurora.workspace.util.NotificationHelper
import com.aurora.workspace.util.UpdateChecker
import com.aurora.workspace.util.colorAttr
import com.aurora.workspace.util.dp
import com.aurora.workspace.util.fitCenteredColumn
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            NotificationHelper.sendWelcomeNotification(requireContext())
        } else {
            // No permission means no reminders: keep the switch truthful.
            Store.setNotifications(requireContext(), false)
            binding.notificationsSwitch.isChecked = false
        }
    }

    private val googlePicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val account = GoogleAccountHelper.accountFrom(requireContext(), result)
        if (account != null) {
            Store.linkGoogle(requireContext(), account)
            bindGoogle()
            bind()
            showToast(getString(R.string.linked_toast, account))
        } else {
            showGoogleFallback()
        }
    }

    /**
     * Attached only while the user can interact with the switch. Setting the
     * stored value in [bind] must not fire it, otherwise opening Profile with
     * notifications saved as "on" while the OS permission was revoked would
     * pop the system permission dialog up uninvited.
     */
    private val notificationsListener = CompoundButton.OnCheckedChangeListener { _, enabled ->
        Store.setNotifications(requireContext(), enabled)
        if (!enabled) return@OnCheckedChangeListener
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationHelper.hasPermission(requireContext())
        ) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            NotificationHelper.sendWelcomeNotification(requireContext())
        }
    }

    private val hapticsListener = CompoundButton.OnCheckedChangeListener { _, enabled ->
        Store.setHaptics(requireContext(), enabled)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Screen-size adaptation: centered content column on tablets.
        binding.profileContent.fitCenteredColumn(720)

        binding.googleButton.setOnClickListener {
            if (Store.googleAccount == null) launchGooglePicker() else unlinkGoogle()
        }
        binding.notificationsSwitch.setOnCheckedChangeListener(notificationsListener)
        binding.hapticsSwitch.setOnCheckedChangeListener(hapticsListener)
        binding.aboutRow.setOnClickListener { showAbout() }
        binding.signOutButton.setOnClickListener { confirmSignOut() }

        binding.languageSystem.setOnClickListener { changeLanguage(LocaleHelper.SYSTEM) }
        binding.languageEnglish.setOnClickListener { changeLanguage(LocaleHelper.ENGLISH) }
        binding.languageArabic.setOnClickListener { changeLanguage(LocaleHelper.ARABIC) }

        buildThemeRow()
        bind()
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) bind()
    }

    private fun bind() {
        val b = binding
        b.avatar.bind(Store.userName, Store.userColor, true)
        b.name.text = Store.userName
        b.email.text = Store.userEmail

        b.statProjects.text = Store.projects.size.toString()
        b.statTasks.text = Store.doneTasks.toString()
        b.statTeam.text = (Store.members.size - 1).coerceAtLeast(0).toString()

        // Bind silently: the listeners are only for real user taps.
        b.notificationsSwitch.setOnCheckedChangeListener(null)
        b.notificationsSwitch.isChecked = Store.notifications
        b.notificationsSwitch.setOnCheckedChangeListener(notificationsListener)

        b.hapticsSwitch.setOnCheckedChangeListener(null)
        b.hapticsSwitch.isChecked = Store.haptics
        b.hapticsSwitch.setOnCheckedChangeListener(hapticsListener)
        b.version.text = getString(
            R.string.version,
            UpdateChecker.installedVersion(requireContext())
        )

        bindGoogle()
        bindLanguage()
    }

    private fun bindGoogle() {
        val b = binding
        val account = Store.googleAccount
        if (account != null) {
            b.googleTitle.text = getString(R.string.google_linked)
            b.googleSubtitle.text = account
            b.googleButton.text = getString(R.string.unlink)
            b.googleIcon.setImageResource(R.drawable.ic_check)
            b.googleIcon.imageTintList = ColorStateList.valueOf(
                requireContext().colorAttr(R.attr.auroraAccent)
            )
        } else {
            b.googleTitle.text = getString(R.string.google_not_linked)
            b.googleSubtitle.text = getString(R.string.google_desc)
            b.googleButton.text = getString(R.string.link_google)
            b.googleIcon.setImageResource(R.drawable.ic_google)
            b.googleIcon.imageTintList = null
        }
    }

    private fun bindLanguage() {
        val current = LocaleHelper.current(requireContext())
        styleLanguageChip(binding.languageSystem, current == LocaleHelper.SYSTEM)
        styleLanguageChip(binding.languageEnglish, current == LocaleHelper.ENGLISH)
        styleLanguageChip(binding.languageArabic, current == LocaleHelper.ARABIC)
    }

    private fun styleLanguageChip(chip: TextView, selected: Boolean) {
        chip.setBackgroundResource(
            if (selected) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected
        )
        chip.setTextColor(
            if (selected) Color.WHITE else requireContext().colorAttr(R.attr.auroraTextDim)
        )
    }

    private fun buildThemeRow() {
        val row = binding.themeRow
        row.removeAllViews()
        val size = requireContext().dp(52f)
        val current = ThemeManager.current(requireContext())

        ThemeManager.keys.forEach { key ->
            val item = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            val holder = FrameLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(size, size)
            }

            val swatch = View(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(size, size)
                background = ContextCompat.getDrawable(requireContext(), swatchRes(key))
                elevation = dp(3f).toFloat()
            }

            val check = ImageView(requireContext()).apply {
                val checkSize = (size * 0.38f).toInt()
                layoutParams = FrameLayout.LayoutParams(checkSize, checkSize).apply {
                    gravity = Gravity.CENTER
                }
                setImageResource(R.drawable.ic_check)
                imageTintList = ColorStateList.valueOf(Color.WHITE)
                visibility = if (current == key) View.VISIBLE else View.GONE
            }

            holder.addView(swatch)
            holder.addView(check)

            val label = TextView(requireContext()).apply {
                text = getString(ThemeManager.labelRes(key))
                textSize = 11f
                gravity = Gravity.CENTER
                setTextColor(requireContext().colorAttr(R.attr.auroraTextDim))
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(8f) }
            }

            // Ripple feedback on the theme swatch (foreground needs API 23+,
            // so a ripple background keeps API 21-22 working too).
            item.background = rippleDrawable()

            item.addView(holder)
            item.addView(label)
            item.setOnClickListener {
                if (ThemeManager.current(requireContext()) != key) {
                    ThemeManager.set(requireContext(), key)
                    requireActivity().recreate()
                }
            }
            row.addView(item)
        }
    }

    private fun rippleDrawable(): android.graphics.drawable.Drawable? {
        val value = android.util.TypedValue()
        requireContext().theme.resolveAttribute(
            android.R.attr.selectableItemBackgroundBorderless, value, true
        )
        return ContextCompat.getDrawable(requireContext(), value.resourceId)
    }

    private fun swatchRes(key: String): Int = when (key) {
        ThemeManager.GLASS -> R.drawable.bg_swatch_glass
        ThemeManager.DAY -> R.drawable.bg_swatch_day
        ThemeManager.EMERALD -> R.drawable.bg_swatch_emerald
        ThemeManager.SUNSET -> R.drawable.bg_swatch_sunset
        else -> R.drawable.bg_swatch_night
    }

    private fun launchGooglePicker() {
        try {
            googlePicker.launch(GoogleAccountHelper.intent())
        } catch (e: Exception) {
            showGoogleFallback()
        }
    }

    private fun showGoogleFallback() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.google_not_linked)
            .setMessage(R.string.google_add_first)
            .setPositiveButton(R.string.use_demo) { _, _ ->
                Store.linkGoogle(requireContext(), "layla.aurora@gmail.com")
                bindGoogle()
                showToast(getString(R.string.linked_toast, "layla.aurora@gmail.com"))
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun unlinkGoogle() {
        Store.unlinkGoogle(requireContext())
        bindGoogle()
    }

    private fun changeLanguage(language: String) {
        if (LocaleHelper.current(requireContext()) == language) return
        LocaleHelper.set(requireContext(), language)
        (requireActivity() as? AuroraActivity)?.restartApp()
    }

    private fun showAbout() {
        val context = requireContext()
        // The About sheet is also the manual entry point for updates, so the
        // version shown here is read from the installed package (never a
        // hardcoded string that could drift from the built APK).
        val version = getString(R.string.version, UpdateChecker.installedVersion(context))
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.about)
            .setMessage(getString(R.string.about_text) + "\n\n" + version)
            .setPositiveButton(R.string.updates_check) { _, _ ->
                UpdateChecker.check(requireActivity(), manual = true)
            }
            .setNegativeButton(R.string.updates_close, null)
            .show()
    }

    private fun confirmSignOut() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.sign_out)
            .setMessage(R.string.sign_out_confirm)
            .setPositiveButton(R.string.sign_out) { _, _ ->
                Store.signOut(requireContext())
                val intent = Intent(requireContext(), AuthActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showToast(text: String) {
        android.widget.Toast.makeText(requireContext(), text, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
