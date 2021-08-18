package app.xmpp.adapter.omemo;

import org.jivesoftware.smackx.omemo.OmemoStore;
import org.jivesoftware.smackx.omemo.exceptions.CorruptedOmemoKeyException;
import org.jivesoftware.smackx.omemo.internal.OmemoCachedDeviceList;
import org.jivesoftware.smackx.omemo.internal.OmemoDevice;
import org.jivesoftware.smackx.omemo.util.OmemoKeyUtil;
import org.jxmpp.jid.BareJid;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeMap;

public class TICEOmemoStore extends OmemoStore {

    @Override
    public SortedSet<Integer> localDeviceIdsOf(BareJid localUser) {
        return null;
    }

    @Override
    public Object loadOmemoIdentityKeyPair(OmemoDevice userDevice) throws CorruptedOmemoKeyException, IOException {
        return null;
    }

    @Override
    public void storeOmemoIdentityKeyPair(OmemoDevice userDevice, Object identityKeyPair) throws IOException {

    }

    @Override
    public void removeOmemoIdentityKeyPair(OmemoDevice userDevice) {

    }

    @Override
    public Object loadOmemoIdentityKey(OmemoDevice userDevice, OmemoDevice contactsDevice) throws CorruptedOmemoKeyException, IOException {
        return null;
    }

    @Override
    public void storeOmemoIdentityKey(OmemoDevice userDevice, OmemoDevice contactsDevice, Object contactsKey) throws IOException {

    }

    @Override
    public void removeOmemoIdentityKey(OmemoDevice userDevice, OmemoDevice contactsDevice) {

    }

    @Override
    public void storeOmemoMessageCounter(OmemoDevice userDevice, OmemoDevice contactsDevice, int counter) throws IOException {

    }

    @Override
    public int loadOmemoMessageCounter(OmemoDevice userDevice, OmemoDevice contactsDevice) throws IOException {
        return 0;
    }

    @Override
    public void setDateOfLastReceivedMessage(OmemoDevice userDevice, OmemoDevice contactsDevice, Date date) throws IOException {

    }

    @Override
    public Date getDateOfLastReceivedMessage(OmemoDevice userDevice, OmemoDevice contactsDevice) throws IOException {
        return null;
    }

    @Override
    public void setDateOfLastDeviceIdPublication(OmemoDevice userDevice, OmemoDevice contactsDevice, Date date) throws IOException {

    }

    @Override
    public Date getDateOfLastDeviceIdPublication(OmemoDevice userDevice, OmemoDevice contactsDevice) throws IOException {
        return null;
    }

    @Override
    public void setDateOfLastSignedPreKeyRenewal(OmemoDevice userDevice, Date date) throws IOException {

    }

    @Override
    public Date getDateOfLastSignedPreKeyRenewal(OmemoDevice userDevice) throws IOException {
        return null;
    }

    @Override
    public Object loadOmemoPreKey(OmemoDevice userDevice, int preKeyId) throws IOException {
        return null;
    }

    @Override
    public void storeOmemoPreKey(OmemoDevice userDevice, int preKeyId, Object o) throws IOException {

    }

    @Override
    public void removeOmemoPreKey(OmemoDevice userDevice, int preKeyId) {

    }

    @Override
    public TreeMap loadOmemoPreKeys(OmemoDevice userDevice) throws IOException {
        return null;
    }

    @Override
    public Object loadOmemoSignedPreKey(OmemoDevice userDevice, int signedPreKeyId) throws IOException {
        return null;
    }

    @Override
    public TreeMap loadOmemoSignedPreKeys(OmemoDevice userDevice) throws IOException {
        return null;
    }

    @Override
    public void storeOmemoSignedPreKey(OmemoDevice userDevice, int signedPreKeyId, Object signedPreKey) throws IOException {

    }

    @Override
    public void removeOmemoSignedPreKey(OmemoDevice userDevice, int signedPreKeyId) {

    }

    @Override
    public Object loadRawSession(OmemoDevice userDevice, OmemoDevice contactsDevice) throws IOException {
        return null;
    }

    @Override
    public HashMap loadAllRawSessionsOf(OmemoDevice userDevice, BareJid contact) throws IOException {
        return null;
    }

    @Override
    public void storeRawSession(OmemoDevice userDevice, OmemoDevice contactsDevice, Object session) throws IOException {

    }

    @Override
    public void removeRawSession(OmemoDevice userDevice, OmemoDevice contactsDevice) {

    }

    @Override
    public void removeAllRawSessionsOf(OmemoDevice userDevice, BareJid contact) {

    }

    @Override
    public boolean containsRawSession(OmemoDevice userDevice, OmemoDevice contactsDevice) {
        return false;
    }

    @Override
    public OmemoCachedDeviceList loadCachedDeviceList(OmemoDevice userDevice, BareJid contact) throws IOException {
        return null;
    }

    @Override
    public void storeCachedDeviceList(OmemoDevice userDevice, BareJid contact, OmemoCachedDeviceList contactsDeviceList) throws IOException {

    }

    @Override
    public void purgeOwnDeviceKeys(OmemoDevice userDevice) {

    }

    @Override
    public OmemoKeyUtil keyUtil() {
        return null;
    }
}
