# Rules for the release build (R8). The libraries ship their own; these cover what they cannot know.

# webp-android calls into native code by method name.
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}
-keep class com.aureusapps.android.webpandroid.** { *; }

# Stack traces in the diagnostic log stay readable.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
