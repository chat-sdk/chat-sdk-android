
#            | Classes | Members |
# Shrink:    |         |         |
# Obfuscate: |         |         |
-keep public class sdk.chat.ui.**, sdk.chat.core.dao.**, **.R$*, org.bouncycastle.jcajce.provider.**, org.jivesoftware.**  {
    public protected *;
    <fields>;
}

#            | Classes | Members |
# Shrink:    |    x    |         |
# Obfuscate: |    x    |         |
-keepclassmembers public class app.xmpp.** {
    public protected *;
}

#            | Classes | Members |
# Shrink:    |    x    |    x    |
# Obfuscate: |         |         |
-keepnames class org.kxml2.io.**, org.xmlpull.** {
    public protected *;
}

#            | Classes | Members |
# Shrink:    |    x    |    x    |
# Obfuscate: |    x    |         |
-keepclassmembernames public class sdk.chat.core.**, firestream.chat.**, sdk.guru.**, co.chatsdk.**, app.xmpp.** {
    public protected *;
}
