#            | Classes | Members |
# Shrink:    |         |         |
# Obfuscate: |         |         |
-keep public class sdk.chat.ui.**, sdk.chat.core.dao.**, **.R$*, org.bouncycastle.jcajce.provider.**, org.jivesoftware.**, app.xmpp.**  {
    public protected *;
    <fields>;
}

#            | Classes | Members |
# Shrink:    |    x    |    x    |
# Obfuscate: |         |         |
-keepnames class org.kxml2.io.**, org.xmlpull.** {
    public protected *;
}