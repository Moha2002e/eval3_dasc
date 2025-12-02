package org.example.server;

import org.example.server.bd.BdManager;
import org.example.server.crypto.GenerateurCles;
import org.example.server.crypto.GestionnaireCrypto;
import org.example.server.dao.ReportDAO;
import org.example.server.entity.Patient;
import org.example.server.searchvm.PatientSearchVM;
import org.example.shared.Protocol;
import com.google.gson.Gson;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;

/**
 * Gère les connexions individuelles des clients dans un thread séparé.
 * Implémente le protocole MRPS (Medical Report Protocol Secure) avec
 * cryptographie complète.
 * Cette classe est responsable de :
 * <ul>
 * <li>L'authentification sécurisée des médecins</li>
 * <li>La gestion des requêtes (ajout, modification, listage de rapports)</li>
 * <li>Le chiffrement et déchiffrement des échanges</li>
 * <li>La vérification des signatures numériques</li>
 * </ul>
 */
public class ClientHandler implements Runnable {
    private final Socket socketClient;
    private final BdManager gestionnaireBd;
    private BufferedReader entree;
    private PrintWriter sortie;

    // État de session
    private Integer idMedecinCourant;
    private String loginCourant;
    private SecretKey cleSession;
    private byte[] selCourant;
    private boolean authentifie;

    // Clés RSA
    private PrivateKey clePriveeServeur;
    private PublicKey clePubliqueClient;

    /**
     * Constructeur du gestionnaire de client.
     * Charge les clés RSA nécessaires au démarrage.
     *
     * @param socket         La socket de connexion avec le client
     * @param gestionnaireBd Le gestionnaire d'accès à la base de données
     */
    public ClientHandler(Socket socket, BdManager gestionnaireBd) {
        this.socketClient = socket;
        this.gestionnaireBd = gestionnaireBd;
        this.authentifie = false;

        // Charger les clés RSA
        try {
            this.clePriveeServeur = GenerateurCles.chargerClePrivee("src/main/resources/keys/serveur_prive.key");
            this.clePubliqueClient = GenerateurCles.chargerClePublique("src/main/resources/keys/client_public.key");
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des clés: " + e.getMessage());
        }
    }

    /**
     * Point d'entrée du thread.
     * Initialise les flux, connecte à la BD et boucle sur la réception des
     * requêtes.
     */
    @Override
    public void run() {
        try {
            // Initialiser les flux I/O
            entree = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
            sortie = new PrintWriter(socketClient.getOutputStream(), true);

            // Connecter à la BD
            gestionnaireBd.connecter();

            // Boucle principale du protocole
            String requete;
            while ((requete = entree.readLine()) != null) {
                traiterRequete(requete);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur client: " + e.getMessage());
        } finally {
            nettoyer();
        }
    }

    /**
     * Analyse et dispatch la requête reçue vers la méthode appropriée.
     * Vérifie l'authentification pour les commandes sensibles.
     *
     * @param requete La chaîne de caractères brute reçue du client
     */
    private void traiterRequete(String requete) {
        String[] parties = requete.split("\\|", -1);
        if (parties.length == 0) {
            envoyerErreur("Format de requête invalide");
            return;
        }

        String commande = parties[0];

        try {
            switch (commande) {
                case Protocol.CMD_LOGIN:
                    traiterLogin(parties);
                    break;
                case Protocol.CMD_ADD_REPORT:
                    if (verifierAuthentification()) {
                        traiterAjoutRapport(parties);
                    }
                    break;
                case Protocol.CMD_EDIT_REPORT:
                    if (verifierAuthentification()) {
                        traiterModificationRapport(parties);
                    }
                    break;
                case Protocol.CMD_LIST_REPORTS:
                    if (verifierAuthentification()) {
                        traiterListeRapports(parties);
                    }
                    break;
                case Protocol.CMD_LIST_PATIENTS:
                    traiterListePatients();
                    break;
                case Protocol.CMD_LOGOUT:
                    traiterLogout();
                    break;
                default:
                    envoyerErreur("Commande inconnue: " + commande);
            }
        } catch (Exception e) {
            envoyerErreur("Erreur lors du traitement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * LOGIN Protocol:
     * 1. Client -> Server: LOGIN|<login>
     * 2. Server -> Client: SALT|<sel_base64>
     * 3. Client -> Server: DIGEST|<digest_base64>|<cle_session_chiffree_RSA>
     * 4. Server -> Client: OK|<session_id> ou ERROR|<message>
     */
    /**
     * Gère le processus d'authentification en deux étapes (Challenge-Response).
     * <ol>
     * <li>Réception du login -> Envoi d'un sel aléatoire</li>
     * <li>Réception du digest et de la clé de session chiffrée -> Validation et
     * établissement de la session</li>
     * </ol>
     *
     * @param parties Les parties de la commande LOGIN
     * @throws Exception En cas d'erreur cryptographique ou d'accès BD
     */
    private void traiterLogin(String[] parties) throws Exception {
        if (parties.length < 2) {
            envoyerErreur("Format LOGIN invalide");
            return;
        }

        String login = parties[1];

        // Étape 1: Requête initiale - envoyer le sel
        if (parties.length == 2) {
            if (!gestionnaireBd.medecinExiste(login)) {
                envoyerErreur("Médecin inexistant");
                return;
            }

            loginCourant = login;
            selCourant = GestionnaireCrypto.genererSel(Protocol.SALT_SIZE);
            String selBase64 = Base64.getEncoder().encodeToString(selCourant);
            sortie.println(Protocol.RESP_SALT + "|" + selBase64);
            // Envoyer le sel au client
            return;
        }

        // Étape 2: Vérification du digest et échange de clé de session
        if (parties.length >= 3) {
            String digestRecuBase64 = parties[1];
            String cleSessionChiffreeBase64 = parties[2];

            // Récupérer le mot de passe hashé depuis la BD
            String motDePasseHash = gestionnaireBd.getMotDePasseMedecin(loginCourant);
            if (motDePasseHash == null) {
                envoyerErreur("Authentification échouée");
                return;
            }

            // Calculer le digest attendu
            byte[] digestAttendu = GestionnaireCrypto.calculerDigestSale(
                    loginCourant, motDePasseHash, selCourant);

            // Vérifier le digest reçu
            byte[] digestRecu = Base64.getDecoder().decode(digestRecuBase64);
            if (!java.security.MessageDigest.isEqual(digestAttendu, digestRecu)) {
                envoyerErreur("Authentification échouée");
                return;
            }

            // Déchiffrer la clé de session avec RSA
            byte[] cleSessionChiffree = Base64.getDecoder().decode(cleSessionChiffreeBase64);
            byte[] cleSessionBytes = GestionnaireCrypto.dechiffrerRSA(cleSessionChiffree, clePriveeServeur);
            cleSession = GestionnaireCrypto.base64VersCleSecrete(
                    Base64.getEncoder().encodeToString(cleSessionBytes));

            // Récupérer l'ID du médecin
            idMedecinCourant = gestionnaireBd.getIdMedecin(loginCourant);
            authentifie = true;

            String sessionId = "session_" + System.currentTimeMillis();
            sortie.println(Protocol.RESP_OK + "|" + sessionId);
            System.out.println("✓ Médecin authentifié: " + loginCourant + " (ID: " + idMedecinCourant + ")");
        }
    }

    /**
     * ADD_REPORT Protocol:
     * Client -> Server:
     * ADD_REPORT|<date_chiffree>|<patient_id_chiffre>|<texte_chiffre>|<signature>
     * Server -> Client: OK|<report_id> ou ERROR|<message>
     */
    /**
     * Traite l'ajout d'un rapport médical.
     * Déchiffre les données, vérifie la signature numérique et l'autorisation,
     * puis ajoute le rapport en base de données.
     *
     * @param parties Les données chiffrées de la commande ADD_REPORT
     * @throws Exception En cas d'erreur de déchiffrement ou d'accès BD
     */
    private void traiterAjoutRapport(String[] parties) throws Exception {
        if (parties.length < 5) {
            envoyerErreur("Format ADD_REPORT invalide");
            return;
        }

        // Déchiffrer les données avec la clé de session
        byte[] dateChiffree = Base64.getDecoder().decode(parties[1]);
        byte[] patientIdChiffre = Base64.getDecoder().decode(parties[2]);
        byte[] texteChiffre = Base64.getDecoder().decode(parties[3]);
        byte[] signature = Base64.getDecoder().decode(parties[4]);

        String dateStr = new String(GestionnaireCrypto.dechiffrerAES(dateChiffree, cleSession));
        String patientIdStr = new String(GestionnaireCrypto.dechiffrerAES(patientIdChiffre, cleSession));
        String texteRapport = new String(GestionnaireCrypto.dechiffrerAES(texteChiffre, cleSession));

        // Vérifier la signature
        byte[] donneesAVerifier = (dateStr + patientIdStr + texteRapport).getBytes();
        if (!GestionnaireCrypto.verifierSignature(donneesAVerifier, signature, clePubliqueClient)) {
            envoyerErreur("Signature invalide");
            return;
        }

        int patientId = Integer.parseInt(patientIdStr);

        // Vérifier que le médecin a une consultation avec ce patient
        if (!gestionnaireBd.medecinAConsultationAvecPatient(idMedecinCourant, patientId)) {
            envoyerErreur("Aucune consultation trouvée avec ce patient");
            return;
        }

        // Ajouter le rapport en BD
        int rapportId = gestionnaireBd.ajouterRapport(idMedecinCourant, patientId, dateStr, texteRapport);

        sortie.println(Protocol.RESP_OK + "|" + rapportId);
        System.out.println("✓ Rapport ajouté (ID: " + rapportId + ")");
    }

    /**
     * EDIT_REPORT Protocol:
     * Client -> Server: EDIT_REPORT|<report_id_chiffre>|<texte_chiffre>
     * Server -> Client: OK ou ERROR|<message>
     */
    /**
     * Traite la modification d'un rapport existant.
     * Déchiffre les nouvelles données et met à jour la base de données.
     *
     * @param parties Les données chiffrées de la commande EDIT_REPORT
     * @throws Exception En cas d'erreur de déchiffrement ou d'accès BD
     */
    private void traiterModificationRapport(String[] parties) throws Exception {
        if (parties.length < 3) {
            envoyerErreur("Format EDIT_REPORT invalide");
            return;
        }

        // Déchiffrer les données
        byte[] rapportIdChiffre = Base64.getDecoder().decode(parties[1]);
        byte[] texteChiffre = Base64.getDecoder().decode(parties[2]);

        String rapportIdStr = new String(GestionnaireCrypto.dechiffrerAES(rapportIdChiffre, cleSession));
        String nouveauTexte = new String(GestionnaireCrypto.dechiffrerAES(texteChiffre, cleSession));

        int rapportId = Integer.parseInt(rapportIdStr);

        // Modifier le rapport
        boolean succes = gestionnaireBd.modifierRapport(rapportId, nouveauTexte, idMedecinCourant);

        if (succes) {
            sortie.println(Protocol.RESP_OK);
            // Rapport modifié
        } else {
            envoyerErreur("Rapport non trouvé ou non autorisé");
        }
    }

    /**
     * LIST_REPORTS Protocol:
     * Client -> Server: LIST_REPORTS|<patient_id_chiffre_optionnel>
     * Server -> Client: OK|<count>|<rapports_json_chiffres>|<hmac>
     */
    /**
     * Liste les rapports médicaux pour le médecin connecté.
     * Récupère les rapports, les convertit en JSON, chiffre le tout et ajoute un
     * HMAC
     * pour garantir l'intégrité.
     *
     * @param parties Paramètres optionnels (filtre patient)
     * @throws Exception En cas d'erreur de chiffrement ou d'accès BD
     */
    private void traiterListeRapports(String[] parties) throws Exception {
        List<ReportDAO.Rapport> rapports;

        // Vérifier si un patient_id est spécifié
        if (parties.length >= 2 && !parties[1].isEmpty()) {
            byte[] patientIdChiffre = Base64.getDecoder().decode(parties[1]);
            String patientIdStr = new String(GestionnaireCrypto.dechiffrerAES(patientIdChiffre, cleSession));
            int patientId = Integer.parseInt(patientIdStr);
            rapports = gestionnaireBd.listerRapportsMedecinPatient(idMedecinCourant, patientId);
        } else {
            rapports = gestionnaireBd.listerRapportsMedecin(idMedecinCourant);
        }

        // Convertir en JSON
        Gson gson = new Gson();
        String json = gson.toJson(rapports);

        // Chiffrer le JSON
        byte[] jsonChiffre = GestionnaireCrypto.chiffrerAES(json.getBytes(), cleSession);
        String jsonChiffreBase64 = Base64.getEncoder().encodeToString(jsonChiffre);

        // Calculer le HMAC
        byte[] hmac = GestionnaireCrypto.calculerHMAC(jsonChiffre, cleSession);
        String hmacBase64 = Base64.getEncoder().encodeToString(hmac);

        sortie.println(Protocol.RESP_OK + "|" + rapports.size() + "|" + jsonChiffreBase64 + "|" + hmacBase64);
        // Liste envoyée
    }

    /**
     * LOGOUT Protocol:
     * Client -> Server: LOGOUT
     * Server -> Client: OK
     */
    /**
     * LIST_PATIENTS Protocol:
     * Client → Serveur: LIST_PATIENTS
     * Serveur → Client: OK|id1,prenom1,nom1,date1|id2,prenom2,nom2,date2|...
     */
    /**
     * Liste les patients associés au médecin connecté.
     * Envoie la liste en clair (seuls les ID et noms sont transmis, pas de données
     * médicales sensibles ici).
     *
     * @throws Exception En cas d'erreur d'accès BD
     */
    private void traiterListePatients() throws Exception {
        // Récupérer tous les patients du médecin connecté
        PatientSearchVM searchVM = new PatientSearchVM();
        searchVM.setDoctorId(idMedecinCourant);
        List<Patient> patients = gestionnaireBd.load(searchVM);

        // Construire la réponse
        StringBuilder reponse = new StringBuilder(Protocol.RESP_OK);
        for (Patient patient : patients) {
            reponse.append("|");
            reponse.append(patient.getId()).append(",");
            reponse.append(patient.getFirst_name()).append(",");
            reponse.append(patient.getLast_name()).append(",");
            reponse.append(patient.getBirth_date() != null ? patient.getBirth_date() : "");
        }

        sortie.println(reponse.toString());
        // Liste envoyée
    }

    /**
     * LOGOUT Protocol:
     * Client → Serveur: LOGOUT
     * Serveur → Client: OK
     */
    /**
     * Gère la déconnexion explicite du client.
     * Réinitialise l'état de la session.
     */
    private void traiterLogout() {
        authentifie = false;
        idMedecinCourant = null;
        loginCourant = null;
        cleSession = null;
        selCourant = null;
        sortie.println(Protocol.RESP_OK);
        // Médecin déconnecté
    }

    /**
     * Vérifie si le client est authentifié.
     *
     * @return true si authentifié, false sinon
     */
    private boolean verifierAuthentification() {
        if (!authentifie) {
            envoyerErreur("Non authentifié");
            return false;
        }
        return true;
    }

    /**
     * Envoie un message d'erreur au client selon le protocole.
     *
     * @param message Le message d'erreur
     */
    private void envoyerErreur(String message) {
        sortie.println(Protocol.RESP_ERROR + "|" + message);
    }

    /**
     * Nettoie les ressources (sockets, flux, connexion BD) à la fin de la session.
     */
    private void nettoyer() {
        try {
            if (entree != null)
                entree.close();
            if (sortie != null)
                sortie.close();
            if (socketClient != null)
                socketClient.close();
            if (gestionnaireBd != null)
                gestionnaireBd.deconnecter();
            System.out.println("Connexion client fermée: " + socketClient.getInetAddress());
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture: " + e.getMessage());
        }
    }
}
