# =========================
# BASIC
# =========================

-keepattributes Signature
-keepattributes *Annotation*

# =========================
# GSON
# =========================

-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# =========================
# RETROFIT
# =========================

-keep interface * {
    @retrofit2.http.* <methods>;
}

# =========================
# FIREBASE
# =========================

-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# =========================
# WORKMANAGER
# =========================

-keep class androidx.work.** { *; }
-keep class * extends androidx.work.ListenableWorker

# =========================
# SERVICE
# =========================

-keep class * extends android.app.Service

# =========================
# GLIDE
# =========================

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule

# =========================
# EVENTBUS
# =========================

-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}

# =========================
# MEDIA3
# =========================

-keep class androidx.media3.** { *; }

# =========================
# GOOGLE MAPS
# =========================

-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.maps.** { *; }

# =========================
# ITEXT PDF
# =========================

-keep class com.itextpdf.** { *; }

# =========================
# LOG REMOVE
# =========================

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}