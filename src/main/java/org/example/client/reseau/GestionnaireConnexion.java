package org.example.client.reseau;

import org.example.shared.Protocol;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Gestionnaire de connexion réseau avec le serveur.
 * Gère l'établissement de la connexion socket, l'envoi de requêtes
 * et la réception de réponses.
 */
public class GestionnaireConnexion {

    private Socket socket;
    private BufferedReader entree;
    private PrintWriter sortie;
    private boolean connecte;

    /**
     * Établit une connexion TCP avec le serveur spécifié.
     * Initialise les flux d'entrée et de sortie pour la communication.
     *
     * @param hote L'adresse IP ou le nom d'hôte du serveur
     * @param port Le port d'écoute du serveur
     * @return true si la connexion est réussie, false sinon
     */
    public boolean connecter(String hote, int port) {
        try {
            socket = new Socket(hote, port);
            entree = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            sortie = new PrintWriter(socket.getOutputStream(), true);
            connecte = true;
            // Connecté au serveur
            return true;
        } catch (Exception e) {
            System.err.println("✗ Erreur de connexion: " + e.getMessage());
            connecte = false;
            return false;
        }
    }

    /**
     * Envoie une requête textuelle au serveur.
     * La requête est envoyée telle quelle, suivie d'un saut de ligne.
     *
     * @param requete La chaîne de caractères à envoyer
     */
    public void envoyerRequete(String requete) {
        if (connecte && sortie != null) {
            sortie.println(requete);
            // Requête envoyée
        }
    }

    /**
     * Attend et lit une réponse du serveur.
     * Cette méthode est bloquante jusqu'à ce qu'une ligne soit reçue.
     *
     * @return La réponse du serveur, ou null si la connexion est fermée
     * @throws Exception En cas d'erreur de lecture
     */
    public String recevoirReponse() throws Exception {
        if (connecte && entree != null) {
            String reponse = entree.readLine();
            // Réponse reçue
            return reponse;
        }
        return null;
    }

    /**
     * Ferme proprement la connexion avec le serveur.
     * Envoie d'abord une commande de déconnexion (LOGOUT) puis ferme les flux et le
     * socket.
     */
    public void deconnecter() {
        try {
            if (connecte) {
                envoyerRequete(Protocol.CMD_LOGOUT);
                recevoirReponse();
            }
            if (entree != null)
                entree.close();
            if (sortie != null)
                sortie.close();
            if (socket != null)
                socket.close();
            connecte = false;
            // Déconnecté
        } catch (Exception e) {
            System.err.println("Erreur lors de la déconnexion: " + e.getMessage());
        }
    }

    /**
     * Vérifie si la connexion est actuellement active.
     *
     * @return true si connecté, false sinon
     */
    public boolean estConnecte() {
        return connecte;
    }
}
