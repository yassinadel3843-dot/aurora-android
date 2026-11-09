package com.aurora.workspace

import android.app.Application
import android.content.Context
import com.aurora.workspace.data.Store
import com.aurora.workspace.theme.LocaleHelper
import com.aurora.workspace.util.NotificationHelper

class AuroraApp : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()
        Store.init(this)
        // Register the notifications channel up front so the first reminder can
        // be posted without waiting for the user to open preferences.
        NotificationHelper.createChannel(this)
    }
}
