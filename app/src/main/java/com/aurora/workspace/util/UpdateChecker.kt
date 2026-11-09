package com.aurora.workspace.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import com.aurora.workspace.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Reads the latest published GitHub release and compares it with the version
 * installed on this device.
 *
 * Aurora is otherwise fully offline: this is the only network request the app
 * ever makes, it carries no account or device identifiers, and it only runs
 * once a day in the background (or when the user taps "Check for updates").
 *
 * Releases are published from [REPO] by the workflow in `.github/workflows`.
 */
object UpdateChecker {

    /**
     * Owner/name of the public repository that hosts the releases. When the
     * project is forked or renamed this is the only line that has to change.
     */
    const val REPO = "yassinadel3843-dot/aurora-android"

    private const val API = "https://api.github.com/repos/$REPO/releases/latest"
    private const val RELEASES_PAGE = "https://github.com/$REPO/releases"

    private const val PREFS = "aurora_updates"
    private const val KEY_LAST_CHECK = "last_check_ms"
    private const val DAY_MS = 24L * 60L * 60L * 1000L

    // Lazy so pure helpers (compareVersions) stay usable in JVM unit tests.
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    /** A published release that is newer than the installed build. */
    data class Update(
        val version: String,
        val notes: String,
        val apkUrl: String,
        val pageUrl: String,
        val sizeBytes: Long
    )

    fun releasesPage(): String = RELEASES_PAGE

    /** Version name of the running app, e.g. "1.0.0". */
    fun installedVersion(context: Context): String = try {
        context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName ?: "0"
    } catch (e: PackageManager.NameNotFoundException) {
        "0"
    }

    /**
     * Compares two dotted version names ("1.10.0" vs "1.9.2"). Returns 1 when
     * [a] is newer, -1 when older and 0 when equal. A leading "v" and any
     * pre-release suffix ("-beta.1") are ignored, so a tag may be written
     * either way. Pure function: covered by unit tests.
     */
    fun compareVersions(a: String, b: String): Int {
        val left = numbers(a)
        val right = numbers(b)
        val size = maxOf(left.size, right.size)
        for (i in 0 until size) {
            val l = left.getOrElse(i) { 0 }
            val r = right.getOrElse(i) { 0 }
            if (l != r) return if (l > r) 1 else -1
        }
        return 0
    }

    private fun numbers(version: String): List<Int> =
        version.trim()
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore('-')
            .substringBefore('+')
            .split('.')
            .map { it.toIntOrNull() ?: 0 }

    /**
     * Background check. Called with `manual = false` once a day from the main
     * screen, and with `manual = true` from the About dialog.
     */
    fun check(activity: Activity, manual: Boolean) {
        val context = activity.applicationContext
        if (!manual && !isDue(context)) return
        if (manual) toast(activity, R.string.updates_checking)

        threadCheck(context) { result ->
            if (activity.isFinishing) return@threadCheck
            result.onSuccess { update ->
                if (update == null) {
                    markChecked(context)
                    if (manual) toast(activity, R.string.updates_latest)
                } else {
                    markChecked(context)
                    // Automatic checks stay quiet: one notification, no dialog
                    // the user did not ask for. A manual check always answers.
                    if (manual) showDialog(activity, update)
                    else NotificationHelper.postUpdateAvailable(context, update)
                }
            }.onFailure {
                if (manual) toast(activity, R.string.updates_failed)
            }
        }
    }

    private fun showDialog(activity: Activity, update: Update) {
        val notes = update.notes.trim().ifBlank { activity.getString(R.string.updates_no_notes) }
        MaterialAlertDialogBuilder(activity)
            .setTitle(activity.getString(R.string.updates_title, update.version))
            .setMessage(notes)
            .setPositiveButton(R.string.updates_download) { _, _ ->
                UpdateInstaller.startDownload(activity, update)
            }
            .setNegativeButton(R.string.updates_later, null)
            .show()
    }

    // ------------------------------------------------------------------ net

    private fun threadCheck(context: Context, onResult: (Result<Update?>) -> Unit) {
        val installed = installedVersion(context)
        Thread {
            val result = try {
                val body = httpGet(API) ?: error("empty response")
                Result.success(parse(JSONObject(body), installed))
            } catch (e: Exception) {
                Result.failure(e)
            }
            mainHandler.post { onResult(result) }
        }.apply { isDaemon = true }.start()
    }

    private fun httpGet(url: String): String? {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            // GitHub rejects requests without a User-Agent.
            setRequestProperty("User-Agent", "Aurora-Android")
            setRequestProperty("Accept", "application/vnd.github+json")
        }
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    /** Turns a release JSON payload into an [Update], or null when up to date. */
    fun parse(release: JSONObject, installed: String): Update? {
        val tag = release.optString("tag_name").trim()
        if (tag.isEmpty()) return null
        val version = tag.removePrefix("v").removePrefix("V")
        if (compareVersions(version, installed) <= 0) return null

        val assets = release.optJSONArray("assets")
        var apkUrl = ""
        var size = 0L
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val asset = assets.optJSONObject(i) ?: continue
                val name = asset.optString("name")
                if (name.endsWith(".apk", ignoreCase = true)) {
                    apkUrl = asset.optString("browser_download_url")
                    size = asset.optLong("size")
                    break
                }
            }
        }
        if (apkUrl.isEmpty()) return null

        return Update(
            version = version,
            notes = release.optString("body").trim(),
            apkUrl = apkUrl,
            pageUrl = release.optString("html_url").ifBlank { RELEASES_PAGE },
            sizeBytes = size
        )
    }

    // ------------------------------------------------------------- schedule

    private fun prefs(context: Context) = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun isDue(context: Context): Boolean =
        System.currentTimeMillis() - prefs(context).getLong(KEY_LAST_CHECK, 0L) >= DAY_MS

    private fun markChecked(context: Context) {
        prefs(context).edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply()
    }

    private fun toast(activity: Activity, res: Int) {
        android.widget.Toast.makeText(activity, res, android.widget.Toast.LENGTH_SHORT).show()
    }
}
