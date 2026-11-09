package com.aurora.workspace.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.aurora.workspace.R
import com.aurora.workspace.data.Store
import com.aurora.workspace.theme.LocaleHelper
import com.aurora.workspace.theme.ThemeManager

/**
 * Base for every screen: applies the saved visual theme, wraps the context with
 * the selected language (RTL aware), and handles translucent system bars.
 */
abstract class AuroraActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeManager.styleRes(this))
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    /** Pads the layout marked with @id/safeArea so content clears the bars. */
    protected fun applyEdgeToEdge() {
        val safe = findViewById<View>(R.id.safeArea) ?: return

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        val light = ThemeManager.isLight(this)
        controller.isAppearanceLightStatusBars = light
        controller.isAppearanceLightNavigationBars = light

        ViewCompat.setOnApplyWindowInsetsListener(safe) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(
                view.paddingLeft,
                bars.top,
                view.paddingRight,
                maxOf(bars.bottom, ime.bottom)
            )
            insets
        }
        ViewCompat.requestApplyInsets(safe)
    }

    fun restartApp() {
        val target = if (Store.signedIn) MainActivity::class.java else AuthActivity::class.java
        val intent = Intent(this, target)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    protected fun toast(text: CharSequence) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}
