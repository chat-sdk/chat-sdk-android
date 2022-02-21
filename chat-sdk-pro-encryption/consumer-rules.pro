# Virgil Security

# ------------
# --- Gson ---
# ------------
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes Annotation

# Gson specific classes
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }
-keep class com.google.**
-dontwarn com.google.**

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
#-keep class * implements com.google.gson.TypeAdapterFactory
#-keep class * implements com.google.gson.JsonSerializer
#-keep class * implements com.google.gson.JsonDeserializer

# -------------------------
# --- DatatypeConverter ---
# -------------------------
-dontwarn javax.xml.bind.**

# -----------------------
# --- Virgil Security ---
# -----------------------
-keep class com.virgilsecurity.crypto.** { *; }