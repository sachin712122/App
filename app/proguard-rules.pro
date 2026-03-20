# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK's default proguard-android.txt file.

# Keep Room entities
-keep class com.attendance.app.data.** { *; }

# Keep KioskManager
-keep class com.attendance.app.kiosk.** { *; }
