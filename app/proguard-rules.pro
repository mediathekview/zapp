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

## start about libraries
-keep class *.R
-keep class **.R$* {
    <fields>;
}
-dontwarn javax.annotation.**
## end about libraries

## start exoplayer
-dontwarn com.google.android.exoplayer2.upstream.DataSource
## end exopayer

## start fetch
#noinspection ShrinkerUnresolvedReference
-keep class com.tonyodev.fetch2.** { *; }
#noinspection ShrinkerUnresolvedReference
-keep class com.tonyodev.fetch2core.** { *; }
#noinspection ShrinkerUnresolvedReference
-keep interface com.tonyodev.fetch2.** { *; }
#noinspection ShrinkerUnresolvedReference
-keep interface com.tonyodev.fetch2core.** { *; }
## end fetch
