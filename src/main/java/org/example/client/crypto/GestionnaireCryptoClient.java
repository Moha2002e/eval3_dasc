package org.example.client.crypto;

import org.example.server.crypto.GestionnaireCrypto;
import org.example.server.crypto.GenerateurCles;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Gestionnaire de cryptographie côté client.
 * Gère le stockage des clés (publique serveur, privée client, session) et
 * délègue
 * les opérations cryptographiques au {@link GestionnaireCrypto}.
 */
public class GestionnaireCryptoClient {

    private PrivateKey clePriveeClient;
    private PublicKey clePubliqueServeur;
    private SecretKey cleSession;

    public GestionnaireCryptoClient() {
        chargerCles();
    }

    /**
     * Charge les clés RSA nécessaires depuis les fichiers de ressources.
     * Charge la clé privée du client pour signer et déchiffrer.
     * Charge la clé publique du serveur pour chiffrer la clé de session.
     */
    private void chargerCles() {
        try {
            clePriveeClient = GenerateurCles.chargerClePrivee("src/main/resources/keys/client_prive.key");
            clePubliqueServeur = GenerateurCles.chargerClePublique("src/main/resources/keys/serveur_public.key");
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des clés: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Génère une nouvelle clé de session AES aléatoire.
     * Cette clé sera utilisée pour sécuriser les échanges avec le serveur.
     *
     * @throws Exception En cas d'erreur de génération
     */
    public void genererCleSession() throws Exception {
        cleSession = GestionnaireCrypto.genererCleSession();
    }

    /**
     * Chiffre la clé de session AES avec la clé publique du serveur.
     * Cela permet d'envoyer la clé de session de manière sécurisée au serveur.
     *
     * @return La clé de session chiffrée
     * @throws Exception En cas d'erreur de chiffrement
     */
    public byte[] chiffrerCleSession() throws Exception {
        return GestionnaireCrypto.chiffrerRSA(cleSession.getEncoded(), clePubliqueServeur);
    }

    /**
     * Calcule le digest salé pour l'authentification.
     *
     * @param login      Le login de l'utilisateur
     * @param motDePasse Le mot de passe de l'utilisateur
     * @param sel        Le sel fourni par le serveur
     * @return Le digest calculé
     * @throws Exception En cas d'erreur de hachage
     */
    public byte[] calculerDigestSale(String login, String motDePasse, byte[] sel) throws Exception {
        return GestionnaireCrypto.calculerDigestSale(login, motDePasse, sel);
    }

    /**
     * Chiffre une chaîne de caractères avec la clé de session AES.
     *
     * @param donnees La chaîne à chiffrer
     * @return Les données chiffrées
     * @throws Exception En cas d'erreur de chiffrement
     */
    public byte[] chiffrer(String donnees) throws Exception {
        return GestionnaireCrypto.chiffrerAES(donnees.getBytes(), cleSession);
    }

    /**
     * Déchiffre des données avec la clé de session AES.
     *
     * @param donnees Les données chiffrées
     * @return La chaîne déchiffrée
     * @throws Exception En cas d'erreur de déchiffrement
     */
    public String dechiffrer(byte[] donnees) throws Exception {
        return new String(GestionnaireCrypto.dechiffrerAES(donnees, cleSession));
    }

    /**
     * Signe des données avec la clé privée du client.
     *
     * @param donnees Les données à signer
     * @return La signature numérique
     * @throws Exception En cas d'erreur de signature
     */
    public byte[] signer(byte[] donnees) throws Exception {
        return GestionnaireCrypto.signer(donnees, clePriveeClient);
    }

    /**
     * Vérifie un HMAC reçu du serveur pour garantir l'intégrité des données.
     *
     * @param donnees Les données reçues
     * @param hmac    Le HMAC reçu
     * @return true si le HMAC est valide, false sinon
     * @throws Exception En cas d'erreur de vérification
     */
    public boolean verifierHMAC(byte[] donnees, byte[] hmac) throws Exception {
        return GestionnaireCrypto.verifierHMAC(donnees, hmac, cleSession);
    }

    public SecretKey getCleSession() {
        return cleSession;
    }
}
