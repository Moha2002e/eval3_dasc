package org.example.server;

import org.example.server.bd.BdManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Serveur principal de l'application de rapports médicaux sécurisés.
 * Utilise un modèle multi-threadé avec un pool de threads pour gérer les
 * connexions clientes.
 * Chaque connexion est déléguée à un {@link ClientHandler}.
 */
public class MedicalReportServer {
    private final int port;
    private final int taillePoolThreads;
    private final ExecutorService poolThreads;
    private final BdManager gestionnaireBd;
    private volatile boolean enCours;

    /**
     * Constructeur du serveur.
     * Initialise le pool de threads et le gestionnaire de base de données.
     *
     * @param port              Le port d'écoute du serveur
     * @param taillePoolThreads La taille du pool de threads pour gérer les clients
     *                          simultanés
     */
    public MedicalReportServer(int port, int taillePoolThreads) {
        this.port = port;
        this.taillePoolThreads = taillePoolThreads;
        this.poolThreads = Executors.newFixedThreadPool(taillePoolThreads);
        this.gestionnaireBd = new BdManager();
        this.enCours = false;
    }

    /**
     * Démarre le serveur et écoute les connexions entrantes.
     * Boucle indéfiniment pour accepter les clients et les soumettre au pool de
     * threads.
     */
    public void demarrer() {
        enCours = true;
        System.out.println("🚀 Serveur MRPS démarré sur le port " + port);

        try (ServerSocket socketServeur = new ServerSocket(port)) {
            System.out.println("Serveur en écoute sur le port " + port);

            while (enCours) {
                try {
                    // Accepter la connexion client
                    Socket socketClient = socketServeur.accept();
                    System.out.println("👤 Client connecté: " + socketClient.getInetAddress());

                    // Soumettre la connexion au pool de threads (modèle à la demande)
                    poolThreads.submit(new ClientHandler(socketClient, gestionnaireBd));
                } catch (IOException e) {
                    if (enCours) {
                        System.err.println("Erreur lors de l'acceptation de la connexion client: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur serveur: " + e.getMessage());
            e.printStackTrace();
        } finally {
            arreter();
        }
    }

    /**
     * Arrête proprement le serveur.
     * Ferme le pool de threads et arrête la boucle d'acceptation.
     */
    public void arreter() {
        enCours = false;
        poolThreads.shutdown();
        System.out.println("Arrêt du serveur terminé");
    }

    /**
     * Point d'entrée principal du serveur.
     * Charge la configuration, instancie le serveur et le démarre.
     * Ajoute un hook d'arrêt pour une fermeture propre lors de l'interruption du
     * processus.
     *
     * @param args Arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        // Charger la configuration
        BdManager chargeurConfig = new BdManager();
        int port = Integer.parseInt(chargeurConfig.getProperty("PORT_REPORT_SECURE"));
        int taillePoolThreads = Integer.parseInt(chargeurConfig.getProperty("THREAD_POOL_SIZE"));

        // Démarrer le serveur
        MedicalReportServer serveur = new MedicalReportServer(port, taillePoolThreads);

        // Ajouter un hook d'arrêt
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nArrêt du serveur...");
            serveur.arreter();
        }));

        serveur.demarrer();
    }
}
