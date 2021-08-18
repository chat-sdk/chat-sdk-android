package app.xmpp.adapter.omemo;

import org.jivesoftware.smackx.omemo.OmemoManager;
import org.jivesoftware.smackx.omemo.OmemoRatchet;
import org.jivesoftware.smackx.omemo.OmemoService;
import org.jivesoftware.smackx.omemo.OmemoStore;
import org.jivesoftware.smackx.omemo.exceptions.CorruptedOmemoKeyException;
import org.jivesoftware.smackx.omemo.internal.OmemoDevice;
//
// https://blog.jabberhead.tk/2017/06/14/homemo/
//
public class TICEOmemoService extends OmemoService {

    @Override
    protected OmemoStore createDefaultOmemoStoreBackend() {
        return null;
    }

    @Override
    protected OmemoRatchet instantiateOmemoRatchet(OmemoManager manager, OmemoStore store) {
        return new TICEOmemoRatchet(manager, getOmemoStoreBackend());
    }

    @Override
    protected void processBundle(OmemoManager omemoManager, Object contactsBundle, OmemoDevice contactsDevice) throws CorruptedOmemoKeyException {

    }
}
