package com.aurora.workspace.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aurora.workspace.R
import com.aurora.workspace.ui.MainActivity

object NotificationHelper {

    const val CHANNEL_ID = "aurora_tasks_channel"
    private const val NOTIFICATION_ID = 1001

    const val UPDATE_CHANNEL_ID = "aurora_updates_channel"
    private const val UPDATE_AVAILABLE_ID = 2001
    private const val UPDATE_READY_ID = 2002

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notif_channel_name)
            val descriptionText = context.getString(R.string.notif_channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /** Update notices get their own channel so they can be muted separately. */
    fun createUpdateChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                UPDATE_CHANNEL_ID,
                context.getString(R.string.updates_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.updates_channel_desc) }
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun hasPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendWelcomeNotification(context: Context) {
        if (!hasPermission(context)) return

        createChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(context.getString(R.string.notif_test_title))
            .setContentText(context.getString(R.string.notif_test_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        } catch (_: SecurityException) {
            // Permission rejected by user
        }
    }

    /** "Aurora 1.1.0 is available" - tapping opens the release page. */
    fun postUpdateAvailable(context: Context, update: UpdateChecker.Update) {
        if (!hasPermission(context)) return
        createUpdateChannel(context)

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(update.pageUrl))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pending = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(context.getString(R.string.updates_title, update.version))
            .setContentText(context.getString(R.string.updates_notif_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(UPDATE_AVAILABLE_ID, builder.build())
        } catch (_: SecurityException) {
            // Permission rejected by user
        }
    }

    /** "Update downloaded" - tapping opens the system package installer. */
    fun postUpdateReady(context: Context, fileName: String) {
        if (!hasPermission(context)) return
        createUpdateChannel(context)

        val intent = Intent(context, UpdateInstallReceiver::class.java).apply {
            action = UpdateInstallReceiver.ACTION_INSTALL
            putExtra(UpdateInstallReceiver.EXTRA_FILE_NAME, fileName)
        }
        val pending = PendingIntent.getBroadcast(
            context, 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle(context.getString(R.string.updates_ready_title))
            .setContentText(context.getString(R.string.updates_ready_body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(UPDATE_READY_ID, builder.build())
        } catch (_: SecurityException) {
            // Permission rejected by user
        }
    }
}
