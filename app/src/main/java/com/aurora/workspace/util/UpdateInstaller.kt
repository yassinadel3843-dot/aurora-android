package com.aurora.workspace.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import com.aurora.workspace.R
import java.io.File

/**
 * Downloads the release APK with the system Download Manager and hands it to
 * the package installer.
 *
 * Android does not allow an app to install another APK silently: the system
 * always shows its own confirmation screen (and on Android 8+ the user must
 * allow "install unknown apps" once). Everything up to that point - finding,
 * downloading and opening the installer - is automatic, which is why the
 * feature is described as one tap rather than as an invisible upgrade.
 */
object UpdateInstaller {

    private const val PREF_LAST_ID = "download_id"
    private const val PREF_LAST_NAME = "download_name"

    fun startDownload(context: Context, update: UpdateChecker.Update) {
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val fileName = "aurora-${update.version}.apk"

        // Replace a previously downloaded build so the folder never grows.
        downloadsDir(context).listFiles()?.forEach { it.delete() }

        val request = DownloadManager.Request(Uri.parse(update.apkUrl))
            .setTitle(context.getString(R.string.updates_title, update.version))
            .setDescription(context.getString(R.string.updates_downloading, update.version))
            .setMimeType("application/vnd.android.package-archive")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(
                context, Environment.DIRECTORY_DOWNLOADS, fileName
            )

        val id = manager.enqueue(request)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong(PREF_LAST_ID, id)
            .putString(PREF_LAST_NAME, fileName)
            .apply()

        Toast.makeText(context, R.string.updates_downloading_toast, Toast.LENGTH_SHORT).show()
    }

    /** Called by [UpdateDownloadReceiver] when the system finishes the file. */
    fun onDownloadFinished(context: Context, id: Long) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getLong(PREF_LAST_ID, -1L) != id) return
        val fileName = prefs.getString(PREF_LAST_NAME, null) ?: return
        if (!File(downloadsDir(context), fileName).exists()) return
        NotificationHelper.postUpdateReady(context, fileName)
    }

    /** Opens the installer for an already downloaded APK. */
    fun install(context: Context, fileName: String) {
        val file = File(downloadsDir(context), fileName)
        if (!file.exists()) {
            Toast.makeText(context, R.string.updates_file_missing, Toast.LENGTH_SHORT).show()
            return
        }

        // Android 8+ requires the per-app "install unknown apps" grant.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            Toast.makeText(context, R.string.updates_permission, Toast.LENGTH_LONG).show()
            val settings = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(settings)
            return
        }

        val uri = FileProvider.getUriForFile(context, authority(context), file)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun fileFor(context: Context, fileName: String): File = File(downloadsDir(context), fileName)

    fun authority(context: Context): String = "${context.packageName}.updates"

    private fun downloadsDir(context: Context): File =
        File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "").apply { mkdirs() }

    const val PREFS = "aurora_updates"
}

/** Receives the system's DOWNLOAD_COMPLETE broadcast. */
class UpdateDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (id != -1L) UpdateInstaller.onDownloadFinished(context, id)
    }
}

/** Action behind the "tap to install" notification. */
class UpdateInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: return
        UpdateInstaller.install(context, fileName)
    }

    companion object {
        const val ACTION_INSTALL = "com.aurora.workspace.action.INSTALL_UPDATE"
        const val EXTRA_FILE_NAME = "file_name"
    }
}
