#            | Classes | Members |
# Shrink:    |         |         |
# Obfuscate: |         |         |
-keep public class org.bouncycastle.jcajce.provider.**, org.jivesoftware.**, app.xmpp.**, sdk.chat.**, firestream.chat.**, sdk.guru.**, co.chatsdk.** {
    public protected *;
}

-keep public class smartadapter.**, materialsearchview.**, org.ocpsoft.prettytime.** {
    public protected *;
}

-keep class **.R$* {
    <fields>;
}

# GreenDao
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties

# If you do not use SQLCipher:
-dontwarn org.greenrobot.greendao.database.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# androidDatabaseSQPCipher
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

