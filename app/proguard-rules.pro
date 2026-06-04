# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Hilt
-keepclasseswithmembers class * {
    @dagger.hilt.* <methods>;
}

# Coil
-dontwarn coil.**
