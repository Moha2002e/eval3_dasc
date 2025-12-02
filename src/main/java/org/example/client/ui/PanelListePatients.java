package org.example.client.ui;

import org.example.client.crypto.GestionnaireCryptoClient;
import org.example.client.reseau.GestionnaireConnexion;
import org.example.shared.Protocol;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panneau affichant la liste des patients sous forme de tableau.
 * Permet de visualiser les informations de base des patients (ID, nom, prénom,
 * date de naissance).
 * Les données sont récupérées depuis le serveur.
 */
public class PanelListePatients extends JPanel {

    private GestionnaireConnexion gestionnaireConnexion;
    private GestionnaireCryptoClient gestionnaireCrypto;

    private JTable tablePatients;
    private DefaultTableModel modeleTable;
    private JButton boutonCharger;

    /**
     * Constructeur du panneau de liste des patients.
     *
     * @param connexion Le gestionnaire de connexion réseau
     * @param crypto    Le gestionnaire de cryptographie
     */
    public PanelListePatients(GestionnaireConnexion connexion, GestionnaireCryptoClient crypto) {
        this.gestionnaireConnexion = connexion;
        this.gestionnaireCrypto = crypto;

        initialiserInterface();
    }

    /**
     * Initialise l'interface graphique.
     * Crée le tableau (JTable) pour afficher les patients et le bouton
     * d'actualisation.
     */
    private void initialiserInterface() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel titre avec bouton
        JPanel panelHaut = new JPanel(new BorderLayout());

        JLabel titre = new JLabel("Liste des Patients");
        titre.setFont(new Font("Arial", Font.BOLD, 16));
        panelHaut.add(titre, BorderLayout.WEST);

        boutonCharger = new JButton("🔄 Actualiser");
        boutonCharger.addActionListener(e -> chargerPatients());
        panelHaut.add(boutonCharger, BorderLayout.EAST);

        add(panelHaut, BorderLayout.NORTH);

        // Table des patients
        String[] colonnes = { "ID", "Prénom", "Nom", "Date de Naissance" };
        modeleTable = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Lecture seule
            }
        };

        tablePatients = new JTable(modeleTable);
        tablePatients.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePatients.setFont(new Font("Arial", Font.PLAIN, 12));
        tablePatients.setRowHeight(25);

        // Largeur des colonnes
        tablePatients.getColumnModel().getColumn(0).setPreferredWidth(50); // ID
        tablePatients.getColumnModel().getColumn(1).setPreferredWidth(150); // Prénom
        tablePatients.getColumnModel().getColumn(2).setPreferredWidth(150); // Nom
        tablePatients.getColumnModel().getColumn(3).setPreferredWidth(150); // Date

        JScrollPane scrollPane = new JScrollPane(tablePatients);
        add(scrollPane, BorderLayout.CENTER);

        // Panel info en bas
        JPanel panelBas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel labelInfo = new JLabel("ℹ️ Cliquez sur 'Actualiser' pour charger la liste des patients");
        labelInfo.setForeground(Color.GRAY);
        panelBas.add(labelInfo);
        add(panelBas, BorderLayout.SOUTH);
    }

    /**
     * Charge la liste des patients depuis le serveur.
     * Envoie une requête LIST_PATIENTS et met à jour le modèle du tableau
     * avec les données reçues.
     */
    private void chargerPatients() {
        boutonCharger.setEnabled(false);
        boutonCharger.setText("Chargement...");

        new Thread(() -> {
            try {
                // Envoyer requête LIST_PATIENTS
                gestionnaireConnexion.envoyerRequete("LIST_PATIENTS");
                String reponse = gestionnaireConnexion.recevoirReponse();

                if (reponse == null) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "Aucune réponse du serveur",
                                "Erreur",
                                JOptionPane.ERROR_MESSAGE);
                        boutonCharger.setEnabled(true);
                        boutonCharger.setText("🔄 Actualiser");
                    });
                    return;
                }

                if (reponse.startsWith(Protocol.RESP_OK)) {
                    String[] parties = reponse.split("\\|");

                    SwingUtilities.invokeLater(() -> {
                        modeleTable.setRowCount(0);

                        // Parser les données: OK|id1,prenom1,nom1,date1|id2,prenom2,nom2,date2|...
                        for (int i = 1; i < parties.length; i++) {
                            String[] patient = parties[i].split(",");
                            if (patient.length >= 4) {
                                modeleTable.addRow(new Object[] {
                                        patient[0], // ID
                                        patient[1], // Prénom
                                        patient[2], // Nom
                                        patient[3] // Date de naissance
                                });
                            }
                        }

                        JOptionPane.showMessageDialog(this,
                                (parties.length - 1) + " patient(s) chargé(s)",
                                "Succès",
                                JOptionPane.INFORMATION_MESSAGE);

                        boutonCharger.setEnabled(true);
                        boutonCharger.setText("🔄 Actualiser");
                    });

                } else {
                    SwingUtilities.invokeLater(() -> {
                        String message = reponse.substring(Protocol.RESP_ERROR.length() + 1);
                        JOptionPane.showMessageDialog(this,
                                "Erreur: " + message,
                                "Erreur",
                                JOptionPane.ERROR_MESSAGE);
                        boutonCharger.setEnabled(true);
                        boutonCharger.setText("🔄 Actualiser");
                    });
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Erreur: " + ex.getMessage(),
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    boutonCharger.setEnabled(true);
                    boutonCharger.setText("🔄 Actualiser");
                });
            }
        }).start();
    }
}
