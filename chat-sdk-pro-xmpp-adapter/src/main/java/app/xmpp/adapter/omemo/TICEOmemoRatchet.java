package app.xmpp.adapter.omemo;

import org.jivesoftware.smackx.omemo.OmemoManager;
import org.jivesoftware.smackx.omemo.OmemoRatchet;
import org.jivesoftware.smackx.omemo.OmemoStore;
import org.jivesoftware.smackx.omemo.exceptions.CorruptedOmemoKeyException;
import org.jivesoftware.smackx.omemo.exceptions.CryptoFailedException;
import org.jivesoftware.smackx.omemo.exceptions.NoRawSessionException;
import org.jivesoftware.smackx.omemo.exceptions.UntrustedOmemoIdentityException;
import org.jivesoftware.smackx.omemo.internal.CiphertextTuple;
import org.jivesoftware.smackx.omemo.internal.OmemoDevice;

import java.io.IOException;

public class TICEOmemoRatchet extends OmemoRatchet {

    /**
     * Constructor.
     *
     * @param omemoManager omemoManager
     * @param store        omemoStore
     */
    public TICEOmemoRatchet(OmemoManager omemoManager, OmemoStore store) {
        super(omemoManager, store);
    }

    @Override
    public byte[] doubleRatchetDecrypt(OmemoDevice sender, byte[] encryptedKey) throws CorruptedOmemoKeyException, NoRawSessionException, CryptoFailedException, UntrustedOmemoIdentityException, IOException {
        return new byte[0];
    }

    @Override
    public CiphertextTuple doubleRatchetEncrypt(OmemoDevice recipient, byte[] messageKey) {
        return null;
    }

}
