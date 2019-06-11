package com.vitaxa.jasteambot.helper;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.security.*;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class SecurityHelper {
    // Algorithm constants
    public static final String RSA_ALGO = "RSA";
    public static final String RSA_SIGN_ALGO = "SHA256withRSA";
    public static final String RSA_CIPHER_ALGO = "RSA/ECB/PKCS1Padding";

    // Algorithm size constants
    public static final int TOKEN_LENGTH = 16;
    public static final int TOKEN_STRING_LENGTH = TOKEN_LENGTH << 1;
    public static final int RSA_KEY_LENGTH_BITS = 2048;
    public static final int RSA_KEY_LENGTH = RSA_KEY_LENGTH_BITS / Byte.SIZE;
    public static final int CRYPTO_MAX_LENGTH = 2048;

    // Certificate constants
    public static final String CERTIFICATE_DIGEST = "fca9659209c6b3b510d9d0e328f37ea0e8df11b6f897c70e4fce440501f43075";
    public static final String HEX = "0123456789abcdef";

    // Random generator constants
    private static final char[] VOWELS = {'e', 'u', 'i', 'o', 'a'};
    private static final char[] CONS = {'r', 't', 'p', 's', 'd', 'f', 'g', 'h', 'k', 'l', 'c', 'v', 'b', 'n', 'm'};

    private SecurityHelper() {
    }

    public static byte[] digest(DigestAlgorithm algo, String s) {
        return digest(algo, IOHelper.encode(s));
    }

    public static byte[] digest(DigestAlgorithm algo, URL url) throws IOException {
        try (InputStream input = IOHelper.newInput(url)) {
            return digest(algo, input);
        }
    }

    public static byte[] digest(DigestAlgorithm algo, Path file) throws IOException {
        try (InputStream input = IOHelper.newInput(file)) {
            return digest(algo, input);
        }
    }

    public static byte[] digest(DigestAlgorithm algo, byte[] bytes) {
        return newDigest(algo).digest(bytes);
    }

    public static byte[] digest(DigestAlgorithm algo, InputStream input) throws IOException {
        byte[] buffer = IOHelper.newBuffer();
        MessageDigest digest = newDigest(algo);
        for (int length = input.read(buffer); length != -1; length = input.read(buffer)) {
            digest.update(buffer, 0, length);
        }
        return digest.digest();
    }

    public static KeyPair genRSAKeyPair(SecureRandom random) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA_ALGO);
            generator.initialize(RSA_KEY_LENGTH_BITS, random);
            return generator.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError(e);
        }
    }

    public static KeyPair genRSAKeyPair() {
        return genRSAKeyPair(newRandom());
    }

    public static boolean isValidSign(Path path, byte[] sign, RSAPublicKey publicKey) throws IOException, SignatureException {
        try (InputStream input = IOHelper.newInput(path)) {
            return isValidSign(input, sign, publicKey);
        }
    }

    public static boolean isValidSign(byte[] bytes, byte[] sign, RSAPublicKey publicKey) throws SignatureException {
        Signature signature = newRSAVerifySignature(publicKey);
        try {
            signature.update(bytes);
        } catch (SignatureException e) {
            throw new InternalError(e);
        }
        return signature.verify(sign);
    }

    public static boolean isValidSign(InputStream input, byte[] sign, RSAPublicKey publicKey) throws IOException, SignatureException {
        Signature signature = newRSAVerifySignature(publicKey);
        updateSignature(input, signature);
        return signature.verify(sign);
    }

    public static boolean isValidSign(URL url, byte[] sign, RSAPublicKey publicKey) throws IOException, SignatureException {
        try (InputStream input = IOHelper.newInput(url)) {
            return isValidSign(input, sign, publicKey);
        }
    }

    public static boolean isValidToken(CharSequence token) {
        return token.length() == TOKEN_STRING_LENGTH && token.chars().allMatch(ch -> HEX.indexOf(ch) >= 0);
    }

    public static MessageDigest newDigest(DigestAlgorithm algo) {
        VerifyHelper.verify(algo, a -> a != DigestAlgorithm.PLAIN, "PLAIN digest");
        try {
            return MessageDigest.getInstance(algo.name);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError(e);
        }
    }

    public static Cipher newRSADecryptCipher(RSAPrivateKey key) {
        return newRSACipher(Cipher.DECRYPT_MODE, key);
    }

    public static Cipher newRSAEncryptCipher(RSAPublicKey key) {
        return newRSACipher(Cipher.ENCRYPT_MODE, key);
    }

    public static Cipher newRSAEncryptCipher(RSAPublicKey key, String algo, String algoProvider, Provider provider) {
        return newRSACipher(Cipher.ENCRYPT_MODE, key, algo, algoProvider, provider);
    }

    public static Signature newRSASignSignature(RSAPrivateKey key) {
        Signature signature = newRSASignature();
        try {
            signature.initSign(key);
        } catch (InvalidKeyException e) {
            throw new InternalError(e);
        }
        return signature;
    }

    public static Signature newRSAVerifySignature(RSAPublicKey key) {
        Signature signature = newRSASignature();
        try {
            signature.initVerify(key);
        } catch (InvalidKeyException e) {
            throw new InternalError(e);
        }
        return signature;
    }

    public static SecureRandom newRandom() {
        return new SecureRandom();
    }

    public static byte[] randomBytes(Random random, int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    public static byte[] randomBytes(int length) {
        return randomBytes(newRandom(), length);
    }

    public static String randomStringToken(Random random) {
        return toHex(randomToken(random));
    }

    public static String randomStringToken() {
        return randomStringToken(newRandom());
    }

    public static byte[] randomToken(Random random) {
        return randomBytes(random, TOKEN_LENGTH);
    }

    public static byte[] randomToken() {
        return randomToken(newRandom());
    }

    public static byte[] sign(InputStream input, RSAPrivateKey privateKey) throws IOException {
        Signature signature = newRSASignSignature(privateKey);
        updateSignature(input, signature);
        try {
            return signature.sign();
        } catch (SignatureException e) {
            throw new InternalError(e);
        }
    }

    public static byte[] sign(byte[] bytes, RSAPrivateKey privateKey) {
        Signature signature = newRSASignSignature(privateKey);
        try {
            signature.update(bytes);
            return signature.sign();
        } catch (SignatureException e) {
            throw new InternalError(e);
        }
    }

    public static byte[] sign(Path path, RSAPrivateKey privateKey) throws IOException {
        try (InputStream input = IOHelper.newInput(path)) {
            return sign(input, privateKey);
        }
    }

    public static String toHex(byte[] bytes) {
        int offset = 0;
        char[] hex = new char[bytes.length << 1];
        for (byte currentByte : bytes) {
            int ub = Byte.toUnsignedInt(currentByte);
            hex[offset] = HEX.charAt(ub >>> 4);
            offset++;
            hex[offset] = HEX.charAt(ub & 0x0F);
            offset++;
        }
        return new String(hex);
    }

    public static RSAPrivateKey toPrivateRSAKey(byte[] bytes) throws InvalidKeySpecException {
        return (RSAPrivateKey) newRSAKeyFactory().generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    public static RSAPublicKey toPublicRSAKey(byte[] bytes) throws InvalidKeySpecException {
        return (RSAPublicKey) newRSAKeyFactory().generatePublic(new X509EncodedKeySpec(bytes));
    }

    public static RSAPublicKey toPublicRSAKey(RSAPublicKeySpec key) throws InvalidKeySpecException {
        return (RSAPublicKey) newRSAKeyFactory().generatePublic(key);
    }

    public static void verifySign(byte[] bytes, byte[] sign, RSAPublicKey publicKey) throws SignatureException {
        if (!isValidSign(bytes, sign, publicKey)) {
            throw new SignatureException("Invalid sign");
        }
    }

    public static void verifySign(InputStream input, byte[] sign, RSAPublicKey publicKey) throws SignatureException, IOException {
        if (!isValidSign(input, sign, publicKey)) {
            throw new SignatureException("Invalid stream sign");
        }
    }

    public static void verifySign(Path path, byte[] sign, RSAPublicKey publicKey) throws SignatureException, IOException {
        if (!isValidSign(path, sign, publicKey)) {
            throw new SignatureException(String.format("Invalid file sign: '%s'", path));
        }
    }

    public static void verifySign(URL url, byte[] sign, RSAPublicKey publicKey) throws SignatureException, IOException {
        if (!isValidSign(url, sign, publicKey)) {
            throw new SignatureException(String.format("Invalid URL sign: '%s'", url));
        }
    }

    public static String verifyToken(String token) {
        return VerifyHelper.verify(token, SecurityHelper::isValidToken, String.format("Invalid token: '%s'", token));
    }

    private static Cipher newCipher(String algo) {
        try {
            return Cipher.getInstance(algo);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new InternalError(e);
        }
    }

    private static Cipher newCipher(String algo, String provider) {
        try {
            return Cipher.getInstance(algo, provider);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            throw new InternalError(e);
        }
    }

    private static Cipher newRSACipher(int mode, RSAKey key) {
        Cipher cipher = newCipher(RSA_CIPHER_ALGO);
        try {
            cipher.init(mode, (Key) key);
        } catch (InvalidKeyException e) {
            throw new InternalError(e);
        }
        return cipher;
    }

    private static Cipher newRSACipher(int mode, RSAKey key, String algo, String algoProvider, Provider provider) {
        Security.addProvider(provider);
        Cipher cipher = newCipher(algo, algoProvider);
        try {
            cipher.init(mode, (Key) key);
        } catch (InvalidKeyException e) {
            throw new InternalError(e);
        }
        return cipher;
    }

    private static KeyFactory newRSAKeyFactory() {
        try {
            return KeyFactory.getInstance(RSA_ALGO);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError(e);
        }
    }

    private static Signature newRSASignature() {
        try {
            return Signature.getInstance(RSA_SIGN_ALGO);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError(e);
        }
    }

    private static void updateSignature(InputStream input, Signature signature) throws IOException {
        byte[] buffer = IOHelper.newBuffer();
        for (int length = input.read(buffer); length >= 0; length = input.read(buffer)) {
            try {
                signature.update(buffer, 0, length);
            } catch (SignatureException e) {
                throw new InternalError(e);
            }
        }
    }

    public enum DigestAlgorithm {
        PLAIN("plain"), MD5("MD5"), SHA1("SHA-1"), SHA224("SHA-224"), SHA256("SHA-256"), SHA512("SHA-512");
        private static final Map<String, DigestAlgorithm> ALGORITHMS;

        static {
            DigestAlgorithm[] algorithmsValues = values();
            ALGORITHMS = new HashMap<>(algorithmsValues.length);
            for (DigestAlgorithm algorithm : algorithmsValues) {
                ALGORITHMS.put(algorithm.name, algorithm);
            }
        }

        public final String name;

        DigestAlgorithm(String name) {
            this.name = name;
        }

        public static DigestAlgorithm byName(String name) {
            return VerifyHelper.getMapValue(ALGORITHMS, name, String.format("Unknown digest algorithm: '%s'", name));
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
