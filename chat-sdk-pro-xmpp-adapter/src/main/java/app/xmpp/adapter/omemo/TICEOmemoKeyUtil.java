package app.xmpp.adapter.omemo;

import org.jivesoftware.smackx.omemo.element.OmemoBundleElement;
import org.jivesoftware.smackx.omemo.exceptions.CorruptedOmemoKeyException;
import org.jivesoftware.smackx.omemo.internal.OmemoDevice;
import org.jivesoftware.smackx.omemo.trust.OmemoFingerprint;
import org.jivesoftware.smackx.omemo.util.OmemoKeyUtil;

import java.io.IOException;
import java.util.TreeMap;

public class TICEOmemoKeyUtil extends OmemoKeyUtil {

    @Override
    public Object identityKeyPairFromBytes(byte[] data) throws CorruptedOmemoKeyException {
        return null;
    }

    @Override
    public Object identityKeyFromBytes(byte[] data) throws CorruptedOmemoKeyException {
        return null;
    }

    @Override
    public byte[] identityKeyToBytes(Object identityKey) {
        return new byte[0];
    }

    @Override
    public Object ellipticCurvePublicKeyFromBytes(byte[] data) throws CorruptedOmemoKeyException {
        return null;
    }

    @Override
    public byte[] preKeyToBytes(Object o) {
        return new byte[0];
    }

    @Override
    public Object preKeyFromBytes(byte[] bytes) throws IOException {
        return null;
    }

    @Override
    public TreeMap generateOmemoPreKeys(int startId, int count) {
        return null;
    }

    @Override
    public Object generateOmemoSignedPreKey(Object identityKeyPair, int signedPreKeyId) throws CorruptedOmemoKeyException {
        return null;
    }

    @Override
    public Object signedPreKeyFromBytes(byte[] data) throws IOException {
        return null;
    }

    @Override
    public byte[] signedPreKeyToBytes(Object o) {
        return new byte[0];
    }

    @Override
    public Object bundleFromOmemoBundle(OmemoBundleElement bundle, OmemoDevice contact, int keyId) throws CorruptedOmemoKeyException {
        return null;
    }

    @Override
    public byte[] signedPreKeySignatureFromKey(Object signedPreKey) {
        return new byte[0];
    }

    @Override
    public Object generateOmemoIdentityKeyPair() {
        return null;
    }

    @Override
    public int signedPreKeyIdFromKey(Object signedPreKey) {
        return 0;
    }

    @Override
    public byte[] identityKeyPairToBytes(Object identityKeyPair) {
        return new byte[0];
    }

    @Override
    public Object identityKeyFromPair(Object o) {
        return null;
    }

    @Override
    public byte[] identityKeyForBundle(Object identityKey) {
        return new byte[0];
    }

    @Override
    public byte[] preKeyPublicKeyForBundle(Object preKey) {
        return new byte[0];
    }

    @Override
    public byte[] preKeyForBundle(Object o) {
        return new byte[0];
    }

    @Override
    public byte[] signedPreKeyPublicForBundle(Object signedPreKey) {
        return new byte[0];
    }

    @Override
    public OmemoFingerprint getFingerprintOfIdentityKey(Object identityKey) {
        return null;
    }

    @Override
    public OmemoFingerprint getFingerprintOfIdentityKeyPair(Object identityKeyPair) {
        return null;
    }

    @Override
    public Object rawSessionFromBytes(byte[] data) throws IOException {
        return null;
    }

    @Override
    public byte[] rawSessionToBytes(Object session) {
        return new byte[0];
    }
}
