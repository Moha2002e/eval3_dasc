package org.example.server.crypto;

import java.io.*;
import java.security.*;

/**
 * Utilitaire pour générer et sauvegarder les paires de clés RSA.
 * Cette classe doit être exécutée une fois pour créer les fichiers de clés
 * nécessaires au fonctionnement sécurisé de l'application (serveur et client).
 * Les clés sont sauvegardées dans le dossier {@code src/main/resources/keys/}.
 */
public class GenerateurCles {

    private static final String DOSSIER_CLES = "src/main/resources/keys/";

    public static void main(String[] args) {
        try {
            new File(DOSSIER_CLES).mkdirs();

            // Générer paires de clés
            KeyPair paireServeur = GestionnaireCrypto.genererPaireClesRSA();
            KeyPair paireClient = GestionnaireCrypto.genererPaireClesRSA();

            // Sauvegarder
            sauvegarderCle(paireServeur.getPublic(), DOSSIER_CLES + "serveur_public.key");
            sauvegarderCle(paireServeur.getPrivate(), DOSSIER_CLES + "serveur_prive.key");
            sauvegarderCle(paireClient.getPublic(), DOSSIER_CLES + "client_public.key");
            sauvegarderCle(paireClient.getPrivate(), DOSSIER_CLES + "client_prive.key");

            System.out.println("✓ Clés RSA générées dans: " + DOSSIER_CLES);

        } catch (Exception e) {
            System.err.println("✗ Erreur génération clés: " + e.getMessage());
        }
    }

    /**
     * Sauvegarde une clé cryptographique dans un fichier au format Base64.
     *
     * @param cle           La clé à sauvegarder
     * @param cheminFichier Le chemin du fichier de destination
     * @throws IOException En cas d'erreur d'écriture dans le fichier
     */
    private static void sauvegarderCle(Key cle, String cheminFichier) throws IOException {
        String base64 = GestionnaireCrypto.cleVersBase64(cle);
        try (FileWriter writer = new FileWriter(cheminFichier)) {
            writer.write(base64);
        }
    }

    /**
     * Charge une clé publique RSA depuis un fichier contenant sa représentation en
     * Base64.
     *
     * @param cheminFichier Le chemin du fichier contenant la clé publique
     * @return La clé publique chargée
     * @throws Exception En cas d'erreur de lecture ou de format de clé
     */
    public static PublicKey chargerClePublique(String cheminFichier) throws Exception {
        String base64 = lireFichier(cheminFichier);
        return GestionnaireCrypto.base64VersClePublique(base64);
    }

    /**
     * Charge une clé privée RSA depuis un fichier contenant sa représentation en
     * Base64.
     *
     * @param cheminFichier Le chemin du fichier contenant la clé privée
     * @return La clé privée chargée
     * @throws Exception En cas d'erreur de lecture ou de format de clé
     */
    public static PrivateKey chargerClePrivee(String cheminFichier) throws Exception {
        String base64 = lireFichier(cheminFichier);
        return GestionnaireCrypto.base64VersClePrivee(base64);
    }

    /**
     * Lit le contenu textuel complet d'un fichier.
     *
     * @param cheminFichier Le chemin du fichier à lire
     * @return Le contenu du fichier sous forme de chaîne de caractères
     * @throws IOException En cas d'erreur de lecture
     */
    private static String lireFichier(String cheminFichier) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(cheminFichier))) {
            StringBuilder contenu = new StringBuilder();
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                contenu.append(ligne);
            }
            return contenu.toString();
        }
    }
}
