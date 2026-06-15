# AnDesk Launcher ProGuard Rules

# 保留行号（调试用）
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.andesk.launcher.data.remote.** { *; }
-keep class com.andesk.launcher.data.local.** { *; }

# 保留TypeToken的泛型签名
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# 保留泛型签名
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# 今日诗词 SDK
-keep class com.jinrishici.** { *; }
-dontwarn com.jinrishici.**

# Coil
-keep class coil.** { *; }
-keep class coil.decode.** { *; }
-keep class coil.fetch.** { *; }
-keep class coil.request.** { *; }
-keep class coil.target.** { *; }
-keep class coil.transition.** { *; }
-keep class coil.util.** { *; }

# Epoxy
-keep class com.airbnb.epoxy.** { *; }
-keep class * extends com.airbnb.epoxy.EpoxyController { *; }
-keep class * extends com.airbnb.epoxy.EpoxyModel { *; }
-keep class * extends com.airbnb.epoxy.EpoxyModelWithHolder { *; }
-keepclassmembers class * extends com.airbnb.epoxy.EpoxyHolder {
    public <init>(android.view.View);
}
-keepattributes *Annotation*

# 数据模型
-keep class com.andesk.launcher.data.model.** { *; }

# Android组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver

# 枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# R文件
-keepclassmembers class **.R$* {
    public static <fields>;
}
