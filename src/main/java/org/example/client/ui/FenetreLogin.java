package org.example.client.ui;

import org.example.client.crypto.GestionnaireCryptoClient;
import org.example.client.reseau.GestionnaireConnexion;
import org.example.shared.Protocol;

import javax.swing.*;
import java.awt.*;
import java.util.Base64;

/**
 * Fenêtre de connexion (Login) de l'application cliente.
 * Permet à un médecin de s'authentifier auprès du serveur sécurisé.
 * Gère l'initialisation de la connexion réseau et des outils cryptographiques.
 */
public class FenetreLogin extends JFrame {

    private JTextField champLogin;
    private JPasswordField champMotDePasse;
    private JTextField champHote;
    private JTextField champPort;
    private JButton boutonConnecter;

    private GestionnaireConnexion gestionnaireConnexion;
    private GestionnaireCryptoClient gestionnaireCrypto;

    /**
     * Constructeur de la fenêtre de connexion.
     * Initialise les gestionnaires de connexion et de cryptographie,
     * ainsi que l'interface graphique.
     */
    public FenetreLogin() {
        gestionnaireConnexion = new GestionnaireConnexion();
        gestionnaireCrypto = new GestionnaireCryptoClient();

        initialiserInterface();
    }

    /**
     * Initialise les composants graphiques de la fenêtre.
     * Configure le layout, les champs de saisie (hôte, port, login, mot de passe)
     * et le bouton de connexion.
     */
    private void initialiserInterface() {
        setTitle("Connexion - Serveur Rapport Médical");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);

        // Panel principal
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Titre
        JLabel titre = new JLabel("Serveur Rapport Médical Sécurisé");
        titre.setFont(new Font("Arial", Font.BOLD, 18));
        titre.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelPrincipal.add(titre);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 20)));

        // Configuration serveur
        JPanel panelServeur = new JPanel(new GridLayout(2, 2, 10, 10));
        panelServeur.setBorder(BorderFactory.createTitledBorder("Configuration Serveur"));
        panelServeur.add(new JLabel("Hôte:"));
        champHote = new JTextField("localhost");
        panelServeur.add(champHote);
        panelServeur.add(new JLabel("Port:"));
        champPort = new JTextField("5000");
        panelServeur.add(champPort);
        panelPrincipal.add(panelServeur);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Identifiants
        JPanel panelIdentifiants = new JPanel(new GridLayout(2, 2, 10, 10));
        panelIdentifiants.setBorder(BorderFactory.createTitledBorder("Identifiants Médecin"));
        panelIdentifiants.add(new JLabel("Login:"));
        champLogin = new JTextField();
        panelIdentifiants.add(champLogin);
        panelIdentifiants.add(new JLabel("Mot de passe:"));
        champMotDePasse = new JPasswordField();
        panelIdentifiants.add(champMotDePasse);
        panelPrincipal.add(panelIdentifiants);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 20)));

        // Bouton connexion
        boutonConnecter = new JButton("Se Connecter");
        boutonConnecter.setAlignmentX(Component.CENTER_ALIGNMENT);
        boutonConnecter.setFont(new Font("Arial", Font.BOLD, 14));
        boutonConnecter.addActionListener(e -> seConnecter());
        panelPrincipal.add(boutonConnecter);

        add(panelPrincipal);
    }

    /**
     * Gère le processus de connexion au serveur.
     * Cette méthode est appelée lors du clic sur le bouton "Se Connecter".
     * Elle effectue les étapes suivantes :
     * 1. Validation des champs de saisie.
     * 2. Connexion TCP au serveur.
     * 3. Envoi du login.
     * 4. Réception du sel cryptographique.
     * 5. Calcul du digest du mot de passe.
     * 6. Génération et chiffrement de la clé de session.
     * 7. Envoi des identifiants sécurisés.
     * 8. Traitement de la réponse finale (Succès/Échec).
     */
    private void seConnecter() {
        String login = champLogin.getText().trim();
        String motDePasse = new String(champMotDePasse.getPassword());
        String hote = champHote.getText().trim();
        int port = Integer.parseInt(champPort.getText().trim());

        if (login.isEmpty() || motDePasse.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boutonConnecter.setEnabled(false);
        boutonConnecter.setText("Connexion en cours...");

        new Thread(() -> {
            try {
                // Étape 1: Connexion au serveur
                if (!gestionnaireConnexion.connecter(hote, port)) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Impossible de se connecter au serveur",
                                "Erreur", JOptionPane.ERROR_MESSAGE);
                        boutonConnecter.setEnabled(true);
                        boutonConnecter.setText("Se Connecter");
                    });
                    return;
                }

                // Étape 2: Envoyer LOGIN avec login
                gestionnaireConnexion.envoyerRequete(Protocol.CMD_LOGIN + "|" + login);
                String reponse = gestionnaireConnexion.recevoirReponse();

                if (reponse == null) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Aucune réponse du serveur",
                                "Erreur", JOptionPane.ERROR_MESSAGE);
                        boutonConnecter.setEnabled(true);
                        boutonConnecter.setText("Se Connecter");
                    });
                    return;
                }

                if (reponse.startsWith(Protocol.RESP_ERROR)) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Médecin inexistant",
                                "Erreur", JOptionPane.ERROR_MESSAGE);
                        boutonConnecter.setEnabled(true);
                        boutonConnecter.setText("Se Connecter");
                    });
                    return;
                }

                // Étape 3: Recevoir le sel
                String[] parties = reponse.split("\\|");
                if (!parties[0].equals(Protocol.RESP_SALT)) {
                    throw new Exception("Réponse inattendue du serveur");
                }
                byte[] sel = Base64.getDecoder().decode(parties[1]);

                // Étape 4: Calculer le digest
                byte[] digest = gestionnaireCrypto.calculerDigestSale(login, motDePasse, sel);
                String digestBase64 = Base64.getEncoder().encodeToString(digest);

                // Étape 5: Générer et chiffrer la clé de session
                gestionnaireCrypto.genererCleSession();
                byte[] cleSessionChiffree = gestionnaireCrypto.chiffrerCleSession();
                String cleSessionBase64 = Base64.getEncoder().encodeToString(cleSessionChiffree);

                // Étape 6: Envoyer DIGEST + clé de session
                gestionnaireConnexion.envoyerRequete(Protocol.CMD_LOGIN + "|" + digestBase64 + "|" + cleSessionBase64);
                reponse = gestionnaireConnexion.recevoirReponse();

                if (reponse == null) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Aucune réponse du serveur pour l'authentification",
                                "Erreur", JOptionPane.ERROR_MESSAGE);
                        boutonConnecter.setEnabled(true);
                        boutonConnecter.setText("Se Connecter");
                    });
                    return;
                }

                if (reponse.startsWith(Protocol.RESP_OK)) {
                    // Authentification réussie
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Connexion réussie!",
                                "Succès", JOptionPane.INFORMATION_MESSAGE);
                        ouvrirFenetrePrincipale(login);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Authentification échouée",
                                "Erreur", JOptionPane.ERROR_MESSAGE);
                        boutonConnecter.setEnabled(true);
                        boutonConnecter.setText("Se Connecter");
                    });
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    boutonConnecter.setEnabled(true);
                    boutonConnecter.setText("Se Connecter");
                });
            }
        }).start();
    }

    /**
     * Ouvre la fenêtre principale de l'application après une connexion réussie.
     * Ferme la fenêtre de login actuelle.
     *
     * @param login Le login du médecin connecté
     */
    private void ouvrirFenetrePrincipale(String login) {
        FenetrePrincipale fenetrePrincipale = new FenetrePrincipale(login, gestionnaireConnexion, gestionnaireCrypto);
        fenetrePrincipale.setVisible(true);
        this.dispose();
    }

    /**
     * Point d'entrée de l'application cliente.
     * Configure le thème graphique (FlatLaf) et lance la fenêtre de login.
     *
     * @param args Arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        // Appliquer le thème FlatLaf macOS
        try {
            com.formdev.flatlaf.themes.FlatMacLightLaf.setup();
        } catch (Exception e) {
            System.err.println("Impossible de charger le thème FlatLaf");
        }

        SwingUtilities.invokeLater(() -> {
            FenetreLogin fenetre = new FenetreLogin();
            fenetre.setVisible(true);
        });
    }
}
