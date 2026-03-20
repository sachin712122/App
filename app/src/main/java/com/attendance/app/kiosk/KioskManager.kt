package com.attendance.app.kiosk

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import com.attendance.app.receiver.KioskDeviceAdminReceiver

/**
 * Manages kiosk mode activation and deactivation.
 *
 * Kiosk mode pins the app to the screen (lock task mode) and hides
 * system UI so students cannot exit the attendance system.
 * Only an admin with the correct PIN can deactivate kiosk mode.
 */
class KioskManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val adminComponent = ComponentName(context, KioskDeviceAdminReceiver::class.java)

    // ─── Public API ─────────────────────────────────────────────────────────────

    val isKioskModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_KIOSK_ENABLED, false)

    /**
     * Activate kiosk mode: start lock task and hide system UI.
     */
    fun activateKioskMode(activity: Activity) {
        prefs.edit().putBoolean(KEY_KIOSK_ENABLED, true).apply()
        startLockTaskIfAllowed(activity)
        hideSystemUI(activity)
    }

    /**
     * Deactivate kiosk mode: stop lock task and restore system UI.
     */
    fun deactivateKioskMode(activity: Activity) {
        prefs.edit().putBoolean(KEY_KIOSK_ENABLED, false).apply()
        stopLockTaskIfRunning(activity)
        showSystemUI(activity)
    }

    /**
     * Re-apply kiosk restrictions when the activity resumes (e.g. after
     * returning from the lock screen or a permission dialog).
     */
    fun enforceKioskOnResume(activity: Activity) {
        if (isKioskModeEnabled) {
            startLockTaskIfAllowed(activity)
            hideSystemUI(activity)
        }
    }

    // ─── Admin PIN ───────────────────────────────────────────────────────────────

    /** Returns true if an admin PIN has been set. */
    fun hasAdminPin(): Boolean = prefs.contains(KEY_ADMIN_PIN)

    /**
     * Set or change the admin PIN (stored as a BCrypt-style hash via a simple
     * salted SHA-256 for demonstration; production apps should use Argon2/BCrypt).
     */
    fun setAdminPin(pin: String) {
        val hash = hashPin(pin)
        prefs.edit().putString(KEY_ADMIN_PIN, hash).apply()
    }

    /** Verify a supplied PIN against the stored hash. */
    fun verifyAdminPin(pin: String): Boolean {
        val stored = prefs.getString(KEY_ADMIN_PIN, null) ?: return false
        return stored == hashPin(pin)
    }

    // ─── Device-owner lock-task helpers ─────────────────────────────────────────

    fun isDeviceOwner(): Boolean = devicePolicyManager.isDeviceOwnerApp(context.packageName)

    /**
     * Allow the app to use lock-task mode when acting as device owner.
     */
    fun allowLockTask() {
        if (isDeviceOwner()) {
            devicePolicyManager.setLockTaskPackages(
                adminComponent,
                arrayOf(context.packageName)
            )
        }
    }

    // ─── Private helpers ─────────────────────────────────────────────────────────

    private fun startLockTaskIfAllowed(activity: Activity) {
        try {
            val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            // Only call startLockTask if not already locked
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
                    activity.startLockTask()
                }
            } else {
                activity.startLockTask()
            }
        } catch (e: Exception) {
            // startLockTask may throw if not whitelisted; kiosk still works via UI hiding
        }
    }

    private fun stopLockTaskIfRunning(activity: Activity) {
        try {
            activity.stopLockTask()
        } catch (e: Exception) {
            // Safe to ignore if not in lock-task mode
        }
    }

    private fun hideSystemUI(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = activity.window.insetsController
            controller?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
        }
    }

    private fun showSystemUI(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.show(
                WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
            )
        } else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    private fun hashPin(pin: String): String {
        val salt = "attendance_kiosk_salt_v1"
        val bytes = java.security.MessageDigest
            .getInstance("SHA-256")
            .digest((salt + pin).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // ─── Constants ───────────────────────────────────────────────────────────────

    companion object {
        private const val PREFS_NAME = "kiosk_prefs"
        private const val KEY_KIOSK_ENABLED = "kiosk_enabled"
        private const val KEY_ADMIN_PIN = "admin_pin"

        /** Default admin PIN used before the admin changes it. */
        const val DEFAULT_ADMIN_PIN = "1234"
    }
}
