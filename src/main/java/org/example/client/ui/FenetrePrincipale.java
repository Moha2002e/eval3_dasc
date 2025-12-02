package org.example.client.ui;

import org.example.client.crypto.GestionnaireCryptoClient;
import org.example.client.reseau.GestionnaireConnexion;

import javax.swing.*;
import java.awt.*;

/**
 * Fenêtre principale de l'application, affichée après une authentification
 * réussie.
 * Elle contient un système d'onglets permettant d'accéder aux différentes
 * fonctionnalités :
 * <ul>
 * <li>Ajout de rapports</li>
 * <li>Modification de rapports</li>
 * <li>Consultation de la liste des rapports</li>
 * <li>Consultation de la liste des patients</li>
 * </ul>
 */
public class FenetrePrincipale extends JFrame {

    private String loginMedecin;
    private GestionnaireConnexion gestionnaireConnexion;
    private GestionnaireCryptoClient gestionnaireCrypto;

    private JTabbedPane onglets;
    private PanelAjoutRapport panelAjout;
    private PanelModificationRapport panelModification;
    private PanelListeRapports panelListe;
    private PanelListePatients panelPatients;

    /**
     * Constructeur de la fenêtre principale.
     *
     * @param loginMedecin Le login du médecin connecté
     * @param connexion    Le gestionnaire de connexion réseau actif
     * @param crypto       Le gestionnaire de cryptographie initialisé
     */
    public FenetrePrincipale(String loginMedecin, GestionnaireConnexion connexion, GestionnaireCryptoClient crypto) {
        this.loginMedecin = loginMedecin;
        this.gestionnaireConnexion = connexion;
        this.gestionnaireCrypto = crypto;

        initialiserInterface();
    }

    /**
     * Initialise l'interface graphique de la fenêtre principale.
     * Configure le titre, les dimensions, la barre de titre personnalisée,
     * les onglets et le bouton de déconnexion.
     */
    private void initialiserInterface() {
        setTitle("Serveur Rapport Médical - " + loginMedecin);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // Panel principal
        JPanel panelPrincipal = new JPanel(new BorderLayout());

        // Barre de titre
        JPanel panelTitre = new JPanel(new BorderLayout());
        panelTitre.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelTitre.setBackground(new Color(41, 128, 185));

        JLabel titre = new JLabel("Gestion des Rapports Médicaux");
        titre.setFont(new Font("Arial", Font.BOLD, 20));
        titre.setForeground(Color.WHITE);
        panelTitre.add(titre, BorderLayout.WEST);

        JLabel labelMedecin = new JLabel("Médecin: " + loginMedecin);
        labelMedecin.setFont(new Font("Arial", Font.PLAIN, 14));
        labelMedecin.setForeground(Color.WHITE);
        panelTitre.add(labelMedecin, BorderLayout.EAST);

        panelPrincipal.add(panelTitre, BorderLayout.NORTH);

        // Onglets
        onglets = new JTabbedPane();
        onglets.setFont(new Font("Arial", Font.PLAIN, 14));

        panelAjout = new PanelAjoutRapport(gestionnaireConnexion, gestionnaireCrypto);
        panelModification = new PanelModificationRapport(gestionnaireConnexion, gestionnaireCrypto);
        panelListe = new PanelListeRapports(gestionnaireConnexion, gestionnaireCrypto);
        panelPatients = new PanelListePatients(gestionnaireConnexion, gestionnaireCrypto);

        onglets.addTab("📝 Ajouter un Rapport", panelAjout);
        onglets.addTab("✏️ Modifier un Rapport", panelModification);
        onglets.addTab("📋 Liste des Rapports", panelListe);
        onglets.addTab("👥 Liste des Patients", panelPatients);

        panelPrincipal.add(onglets, BorderLayout.CENTER);

        // Bouton déconnexion
        JPanel panelBas = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton boutonDeconnexion = new JButton("Déconnexion");
        boutonDeconnexion.addActionListener(e -> deconnecter());
        panelBas.add(boutonDeconnexion);
        panelPrincipal.add(panelBas, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    /**
     * Gère la déconnexion de l'utilisateur.
     * Demande confirmation, ferme la session réseau, ferme la fenêtre actuelle
     * et retourne à la fenêtre de login.
     */
    private void deconnecter() {
        int choix = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment vous déconnecter?",
                "Déconnexion",
                JOptionPane.YES_NO_OPTION);

        if (choix == JOptionPane.YES_OPTION) {
            gestionnaireConnexion.deconnecter();
            FenetreLogin fenetreLogin = new FenetreLogin();
            fenetreLogin.setVisible(true);
            this.dispose();
        }
    }
}
