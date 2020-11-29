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
