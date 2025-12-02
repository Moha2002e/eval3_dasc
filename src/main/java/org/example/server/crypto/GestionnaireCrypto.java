package org.example.server.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Gestionnaire centralisé des opérations cryptographiques utilisant la
 * bibliothèque Bouncy Castle.
 * Cette classe fournit des méthodes statiques pour :
 * <ul>
 * <li>Le chiffrement symétrique (AES)</li>
 * <li>Le chiffrement asymétrique (RSA)</li>
 * <li>Le hachage (SHA-256)</li>
 * <li>Les signatures numériques (SHA256withRSA)</li>
 * <li>Les codes d'authentification de message (HMAC)</li>
 * </ul>
 */
public class GestionnaireCrypto {

    private static final String ALGORITHME_SYMETRIQUE = "AES";
    private static final String ALGORITHME_ASYMETRIQUE = "RSA";
    private static final String ALGORITHME_HASH = "SHA-256";
    private static final String ALGORITHME_HMAC = "HmacSHA256";
    private static final String ALGORITHME_SIGNATURE = "SHA256withRSA";

    private static final int TAILLE_CLE_AES = 256;
    private static final int TAILLE_CLE_RSA = 2048;

    static {
        // Enregistrer le provider Bouncy Castle
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Génère une clé secrète pour l'algorithme AES (256 bits).
     * Cette clé est utilisée pour le chiffrement symétrique de la session.
     *
     * @return Une nouvelle clé secrète AES
     * @throws NoSuchAlgorithmException Si l'algorithme AES n'est pas disponible
     */
    public static SecretKey genererCleSession() throws NoSuchAlgorithmException {
        KeyGenerator generateur = KeyGenerator.getInstance(ALGORITHME_SYMETRIQUE);
        generateur.init(TAILLE_CLE_AES);
        return generateur.generateKey();
    }

    /**
     * Génère une paire de clés RSA (publique et privée) de 2048 bits.
     * Utilisé pour l'échange sécurisé de clés et la signature numérique.
     *
     * @return Une paire de clés RSA
     * @throws NoSuchAlgorithmException Si l'algorithme RSA n'est pas disponible
     */
    public static KeyPair genererPaireClesRSA() throws NoSuchAlgorithmException {
        KeyPairGenerator generateur = KeyPairGenerator.getInstance(ALGORITHME_ASYMETRIQUE);
        generateur.initialize(TAILLE_CLE_RSA);
        return generateur.generateKeyPair();
    }

    /**
     * Chiffre un tableau d'octets en utilisant l'algorithme AES.
     *
     * @param donnees Les données à chiffrer
     * @param cle     La clé secrète AES
     * @return Les données chiffrées
     * @throws Exception En cas d'erreur de chiffrement
     */
    public static byte[] chiffrerAES(byte[] donnees, SecretKey cle) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHME_SYMETRIQUE);
        cipher.init(Cipher.ENCRYPT_MODE, cle);
        return cipher.doFinal(donnees);
    }

    /**
     * Déchiffre un tableau d'octets chiffré avec AES.
     *
     * @param donnees Les données chiffrées
     * @param cle     La clé secrète AES utilisée pour le chiffrement
     * @return Les données déchiffrées
     * @throws Exception En cas d'erreur de déchiffrement
     */
    public static byte[] dechiffrerAES(byte[] donnees, SecretKey cle) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHME_SYMETRIQUE);
        cipher.init(Cipher.DECRYPT_MODE, cle);
        return cipher.doFinal(donnees);
    }

    /**
     * Chiffre des données avec l'algorithme RSA en utilisant une clé publique.
     * Principalement utilisé pour chiffrer la clé de session AES lors de
     * l'établissement de la connexion.
     *
     * @param donnees     Les données à chiffrer
     * @param clePublique La clé publique du destinataire
     * @return Les données chiffrées
     * @throws Exception En cas d'erreur de chiffrement
     */
    public static byte[] chiffrerRSA(byte[] donnees, PublicKey clePublique) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHME_ASYMETRIQUE);
        cipher.init(Cipher.ENCRYPT_MODE, clePublique);
        return cipher.doFinal(donnees);
    }

    /**
     * Déchiffre des données chiffrées avec RSA en utilisant la clé privée
     * correspondante.
     *
     * @param donnees   Les données chiffrées
     * @param clePrivee La clé privée du destinataire
     * @return Les données déchiffrées
     * @throws Exception En cas d'erreur de déchiffrement
     */
    public static byte[] dechiffrerRSA(byte[] donnees, PrivateKey clePrivee) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHME_ASYMETRIQUE);
        cipher.init(Cipher.DECRYPT_MODE, clePrivee);
        return cipher.doFinal(donnees);
    }

    /**
     * Calcule l'empreinte numérique (hash) SHA-256 de données.
     *
     * @param donnees Les données à hacher
     * @return Le hash SHA-256
     * @throws NoSuchAlgorithmException Si l'algorithme SHA-256 n'est pas disponible
     */
    public static byte[] calculerHash(byte[] donnees) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(ALGORITHME_HASH);
        return digest.digest(donnees);
    }

    /**
     * Calcule un digest salé pour l'authentification sécurisée.
     * Combine le login, le mot de passe et un sel aléatoire avant de hacher le
     * tout.
     * Formule : SHA-256(login + password + sel)
     *
     * @param login      Le nom d'utilisateur
     * @param motDePasse Le mot de passe (déjà haché ou en clair selon le contexte)
     * @param sel        Le sel cryptographique
     * @return Le digest résultant
     * @throws NoSuchAlgorithmException Si l'algorithme de hachage n'est pas
     *                                  disponible
     */
    public static byte[] calculerDigestSale(String login, String motDePasse, byte[] sel)
            throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(ALGORITHME_HASH);
        digest.update(login.getBytes());
        digest.update(motDePasse.getBytes());
        digest.update(sel);
        return digest.digest();
    }

    /**
     * Génère une signature numérique pour garantir l'authenticité et l'intégrité
     * des données.
     * Utilise l'algorithme SHA256withRSA et la clé privée de l'émetteur.
     *
     * @param donnees   Les données à signer
     * @param clePrivee La clé privée de l'émetteur
     * @return La signature numérique
     * @throws Exception En cas d'erreur de signature
     */
    public static byte[] signer(byte[] donnees, PrivateKey clePrivee) throws Exception {
        Signature signature = Signature.getInstance(ALGORITHME_SIGNATURE);
        signature.initSign(clePrivee);
        signature.update(donnees);
        return signature.sign();
    }

    /**
     * Vérifie une signature numérique.
     * Utilise la clé publique de l'émetteur présumé pour valider la signature.
     *
     * @param donnees        Les données originales
     * @param signatureBytes La signature à vérifier
     * @param clePublique    La clé publique de l'émetteur
     * @return true si la signature est valide, false sinon
     * @throws Exception En cas d'erreur de vérification
     */
    public static boolean verifierSignature(byte[] donnees, byte[] signatureBytes, PublicKey clePublique)
            throws Exception {
        Signature signature = Signature.getInstance(ALGORITHME_SIGNATURE);
        signature.initVerify(clePublique);
        signature.update(donnees);
        return signature.verify(signatureBytes);
    }

    /**
     * Calcule un code d'authentification de message (HMAC) utilisant SHA-256.
     * Permet de vérifier l'intégrité et l'authenticité d'un message avec une clé
     * secrète partagée.
     *
     * @param donnees Les données à protéger
     * @param cle     La clé secrète partagée
     * @return Le HMAC calculé
     * @throws Exception En cas d'erreur de calcul
     */
    public static byte[] calculerHMAC(byte[] donnees, SecretKey cle) throws Exception {
        Mac mac = Mac.getInstance(ALGORITHME_HMAC);
        mac.init(cle);
        return mac.doFinal(donnees);
    }

    /**
     * Vérifie la validité d'un HMAC reçu.
     * Recalcule le HMAC des données et le compare avec celui reçu.
     *
     * @param donnees  Les données reçues
     * @param hmacRecu Le HMAC reçu avec les données
     * @param cle      La clé secrète partagée
     * @return true si le HMAC est valide, false sinon
     * @throws Exception En cas d'erreur de vérification
     */
    public static boolean verifierHMAC(byte[] donnees, byte[] hmacRecu, SecretKey cle) throws Exception {
        byte[] hmacCalcule = calculerHMAC(donnees, cle);
        return MessageDigest.isEqual(hmacCalcule, hmacRecu);
    }

    /**
     * Convertit une clé en Base64 pour stockage/transmission
     */
    public static String cleVersBase64(Key cle) {
        return Base64.getEncoder().encodeToString(cle.getEncoded());
    }

    /**
     * Convertit une clé publique depuis Base64
     */
    public static PublicKey base64VersClePublique(String base64) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(base64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance(ALGORITHME_ASYMETRIQUE);
        return factory.generatePublic(spec);
    }

    /**
     * Convertit une clé privée depuis Base64
     */
    public static PrivateKey base64VersClePrivee(String base64) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(base64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance(ALGORITHME_ASYMETRIQUE);
        return factory.generatePrivate(spec);
    }

    /**
     * Convertit une clé secrète AES depuis Base64
     */
    public static SecretKey base64VersCleSecrete(String base64) {
        byte[] bytes = Base64.getDecoder().decode(base64);
        return new SecretKeySpec(bytes, ALGORITHME_SYMETRIQUE);
    }

    /**
     * Génère un sel aléatoire pour le digest
     */
    public static byte[] genererSel(int taille) {
        SecureRandom random = new SecureRandom();
        byte[] sel = new byte[taille];
        random.nextBytes(sel);
        return sel;
    }
}
