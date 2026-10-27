-keep class rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions

-keep class com.apex.overdrive.core.Tweak { *; }
-keep class com.apex.overdrive.core.GameProfile { *; }
-keep class com.apex.overdrive.core.AppSettings { *; }
-keep class com.apex.overdrive.core.Progress { *; }

-keep class com.apex.overdrive.MainActivity { *; }
-keep class com.apex.overdrive.service.** { *; }
-keep class com.apex.overdrive.receiver.** { *; }
-keep class com.apex.overdrive.core.BootReceiver { *; }

-keep class com.apex.overdrive.core.NativeBridge { public static *; }
-keep class com.apex.overdrive.core.IntegrityCheck { public *; }
-keepclasseswithmembernames,includedescriptorclasses class * { native <methods>; }

-repackageclasses 'a'
-allowaccessmodification
-overloadaggressively
-useuniqueclassmembernames
-optimizationpasses 5
-mergeinterfacesaggressively

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

-dontwarn kotlinx.coroutines.**
-dontwarn kotlin.**
-dontwarn org.jetbrains.annotations.**
