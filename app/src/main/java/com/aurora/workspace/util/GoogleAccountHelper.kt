package com.aurora.workspace.util

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.annotation.RequiresApi

/**
 * Google account linking via the system account chooser.
 *
 * This works without an OAuth client ID because it reuses the Google account
 * already signed in on the phone. The account name is what we persist and show
 * in the profile screen.
 *
 * Android 6.0+ gets the real "choose account" sheet. Android 5.x does not expose
 * that API, so it opens the system "add account" screen filtered to Google and
 * falls back to reading the Google accounts already on the device.
 *
 * To upgrade to a real OAuth flow later, replace [intent] with
 * GoogleSignInClient.signInIntent and read the email from the result data.
 */
object GoogleAccountHelper {

    fun intent(): Intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            chooseAccountIntent()
        } else {
            Intent(Settings.ACTION_ADD_ACCOUNT)
                .putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf(GOOGLE))
        }

    fun accountFrom(context: Context, result: ActivityResult): String? {
        if (result.resultCode != Activity.RESULT_OK) return null

        val picked = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        if (!picked.isNullOrBlank()) return picked

        // Some OEM dialogs return RESULT_OK without the extra, so read the
        // Google accounts that are already present on the device.
        return runCatching {
            AccountManager.get(context).getAccountsByType(GOOGLE)
                .firstOrNull()?.name
        }.getOrNull()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun chooseAccountIntent(): Intent {
        val allowable: ArrayList<Account>? = null
        return AccountManager.newChooseAccountIntent(
            null,
            allowable,
            arrayOf(GOOGLE),
            false,
            null,
            null,
            null,
            null
        )
    }

    private const val GOOGLE = "com.google"
}
