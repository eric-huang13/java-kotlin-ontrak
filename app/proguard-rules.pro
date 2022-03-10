# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/ejrobi/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#GreenRobot
-keepclassmembers class * extends org.greenrobot.**greendao.**AbstractDao { public static java.lang.String TABLENAME; }
-keep class **$Properties
-dontwarn org.greenrobot.greendao.database.**
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
#GooglePlayServices
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**
#RetroLambda
-dontwarn java.lang.invoke.*
#RxJava
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
#Picasso
-dontwarn com.squareup.okhttp.**
#OkHttp3
-dontwarn okio.**
#Retrofit
-dontwarn retrofit2.Platform$Java8
#Gson
-keepattributes Signature
-keep class sun.misc.Unsafe { *; }
-dontwarn sun.misc.Unsafe
-keep class com.insperity.escmobile.net.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer