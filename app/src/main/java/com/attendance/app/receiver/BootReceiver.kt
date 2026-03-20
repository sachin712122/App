package com.attendance.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.attendance.app.kiosk.KioskManager
import com.attendance.app.ui.MainActivity

/**
 * Restarts the app in kiosk mode after the device reboots (if kiosk was active).
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != "android.intent.action.LOCKED_BOOT_COMPLETED"
        ) return

        val kioskManager = KioskManager(context)
        if (kioskManager.isKioskModeEnabled) {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(MainActivity.EXTRA_KIOSK_BOOT, true)
            }
            context.startActivity(launchIntent)
        }
    }
}
