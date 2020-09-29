# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\tools\android-sdk/tools/proguard/proguard-android.txt
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


## start joda-time-android 2.8.0
-dontwarn org.joda.convert.FromString
-dontwarn org.joda.convert.ToString
## end joda-time-android 2.8.0


## start retrofit

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn okio.**
-dontwarn okhttp3.**
-keep class de.christinecoenen.code.zapp.app.mediathek.model.** { *; }
-keep class de.christinecoenen.code.zapp.app.mediathek.api.request.** { *; }
-keep class de.christinecoenen.code.zapp.app.mediathek.api.result.** { *; }
-keep class de.christinecoenen.code.zapp.app.livestream.api.model.** { *; }

## end retrofit

## start about libraries
-keep class .R
-keep class **.R$* {
    <fields>;
}
-dontwarn javax.annotation.**
## end about libraries

## start exoplayer
-dontwarn com.google.android.exoplayer2.upstream.DataSource
## end exopayer

## start fetch
-keep class com.tonyodev.fetch2.** { *; }
-keep class com.tonyodev.fetch2core.** { *; }
-keep interface com.tonyodev.fetch2.** { *; }
-keep interface com.tonyodev.fetch2core.** { *; }
## end fetch
