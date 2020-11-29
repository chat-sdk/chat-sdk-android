# ChatSDK
-keep public class sdk.chat.**, sdk.guru.**, firestream.chat.**, app.xmpp.**, co.chatsdk.** {
    public protected *;
}

-keepparameternames
-keeppackagenames
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

-keepclasseswithmembernames,includedescriptorclasses class sdk.chat.**, sdk.guru.**, firestream.chat.**, app.xmpp.**, co.chatsdk.** {
    native <methods>;
}

-keepclassmembers,allowoptimization enum sdk.chat.**, sdk.guru.**, firestream.chat.**, app.xmpp.**, co.chatsdk.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class sdk.chat.**, sdk.guru.**, firestream.chat.**, app.xmpp.**, co.chatsdk.** implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
# End

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

-keep,includedescriptorclasses class net.sqlcipher.** { *; }
-keep,includedescriptorclasses interface net.sqlcipher.** { *; }

