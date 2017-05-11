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

## start cling
-dontwarn org.fourthline.cling.**
-dontwarn org.seamless.**
-dontwarn org.eclipse.jetty.**

-keep class javax.** { *; }
-keep class org.** { *; }
-keep class org.fourthline.cling.** { *;}
-keep class org.seamless.** { *;}
-keep class org.eclipse.jetty.** { *;}
-keepattributes Annotation, InnerClasses, Signature
# end cling
