# Consumer ProGuard rules for host apps depending on nfc-locator-core.

# kotlinx.serialization models used for seed/remote catalog parsing.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class com.nfclocator.core.**$$serializer {
    *** INSTANCE;
}
-keepclassmembers class com.nfclocator.core.** {
    *** Companion;
}
-keep,includedescriptorclasses class com.nfclocator.core.**$$serializer { *; }

# Room entities generated at compile time.
-keep class com.nfclocator.core.data.local.** { *; }
