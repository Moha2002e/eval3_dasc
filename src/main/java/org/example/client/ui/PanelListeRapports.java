package org.example.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.client.crypto.GestionnaireCryptoClient;
import org.example.client.reseau.GestionnaireConnexion;
import org.example.shared.Protocol;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Panneau permettant de lister et consulter les rapports médicaux.
 * Offre une vue tabulaire des rapports avec possibilité de filtrage par
 * patient.
 * Affiche le contenu complet du rapport sélectionné.
 * Gère le déchiffrement des données reçues du serveur.
 */
public class PanelListeRapports extends JPanel {

    private GestionnaireConnexion gestionnaireConnexion;
    private GestionnaireCryptoClient gestionnaireCrypto;

    private JComboBox<PatientItem> comboPatients;
    private JButton boutonCharger;
    private JButton boutonTous;
    private JTable tableRapports;
    private DefaultTableModel modeleTable;
    private JTextArea zoneTexteRapport;
    private List<Map<String, Object>> listeRapportsComplets;

    /**
     * Constructeur du panneau de liste des rapports.
     *
     * @param connexion Le gestionnaire de connexion réseau
     * @param crypto    Le gestionnaire de cryptographie
     */
    public PanelListeRapports(GestionnaireConnexion connexion, GestionnaireCryptoClient crypto) {
        this.gestionnaireConnexion = connexion;
        this.gestionnaireCrypto = crypto;

        initialiserInterface();
    }

    /**
     * Initialise l'interface graphique.
     * Configure les filtres, le tableau des rapports et la zone de visualisation.
     */
    private void initialiserInterface() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel filtres
        JPanel panelFiltres = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltres.add(new JLabel("Filtrer par Patient:"));

        comboPatients = new JComboBox<>();
        // Ajouter un item vide pour "Tous les patients"
        comboPatients.addItem(new PatientItem(-1, "Tous", "les patients"));
        panelFiltres.add(comboPatients);

        boutonCharger = new JButton("🔍 Charger");
        boutonCharger.addActionListener(e -> {
            PatientItem selected = (PatientItem) comboPatients.getSelectedItem();
            if (selected != null && selected.getId() != -1) {
                chargerRapports(String.valueOf(selected.getId()));
            } else {
                chargerRapports("");
            }
        });
        panelFiltres.add(boutonCharger);

        // Charger les patients au démarrage
        chargerPatients();

        boutonTous = new JButton("📋 Tous mes Rapports");
        boutonTous.addActionListener(e -> chargerRapports(""));
        panelFiltres.add(boutonTous);

        add(panelFiltres, BorderLayout.NORTH);

        // Panel central avec split
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.6);

        // Table des rapports
        JPanel panelTable = new JPanel(new BorderLayout());
        panelTable.setBorder(BorderFactory.createTitledBorder("Liste des Rapports"));

        String[] colonnes = { "ID", "Patient ID", "Date", "Aperçu" };
        modeleTable = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableRapports = new JTable(modeleTable);
        tableRapports.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableRapports.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                afficherRapportSelectionne();
            }
        });

        JScrollPane scrollTable = new JScrollPane(tableRapports);
        panelTable.add(scrollTable, BorderLayout.CENTER);

        splitPane.setTopComponent(panelTable);

        // Zone de texte pour afficher le rapport complet
        JPanel panelTexte = new JPanel(new BorderLayout());
        panelTexte.setBorder(BorderFactory.createTitledBorder("Contenu Complet du Rapport"));

        zoneTexteRapport = new JTextArea();
        zoneTexteRapport.setLineWrap(true);
        zoneTexteRapport.setWrapStyleWord(true);
        zoneTexteRapport.setFont(new Font("Arial", Font.PLAIN, 12));
        zoneTexteRapport.setEditable(false);

        JScrollPane scrollTexte = new JScrollPane(zoneTexteRapport);
        panelTexte.add(scrollTexte, BorderLayout.CENTER);

        splitPane.setBottomComponent(panelTexte);

        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Charge les rapports depuis le serveur.
     * Peut filtrer par patient si un ID est fourni.
     * Vérifie l'intégrité (HMAC) et déchiffre les données reçues.
     *
     * @param patientId L'ID du patient pour filtrer, ou une chaîne vide pour tous
     *                  les rapports
     */
    private void chargerRapports(String patientId) {
        boutonCharger.setEnabled(false);
        boutonTous.setEnabled(false);

        new Thread(() -> {
            try {
                // Préparer la requête
                String requete;
                if (patientId.isEmpty()) {
                    requete = Protocol.CMD_LIST_REPORTS + "|";
                } else {
                    byte[] patientIdChiffre = gestionnaireCrypto.chiffrer(patientId);
                    String patientIdBase64 = Base64.getEncoder().encodeToString(patientIdChiffre);
                    requete = Protocol.CMD_LIST_REPORTS + "|" + patientIdBase64;
                }

                // Envoyer la requête
                gestionnaireConnexion.envoyerRequete(requete);

                // Recevoir la réponse
                String reponse = gestionnaireConnexion.recevoirReponse();

                if (reponse.startsWith(Protocol.RESP_OK)) {
                    String[] parties = reponse.split("\\|");
                    int count = Integer.parseInt(parties[1]);
                    String jsonChiffreBase64 = parties[2];
                    String hmacBase64 = parties[3];

                    // Déchiffrer le JSON
                    byte[] jsonChiffre = Base64.getDecoder().decode(jsonChiffreBase64);
                    byte[] hmac = Base64.getDecoder().decode(hmacBase64);

                    // Vérifier le HMAC
                    if (!gestionnaireCrypto.verifierHMAC(jsonChiffre, hmac)) {
                        throw new Exception("HMAC invalide - données corrompues");
                    }

                    String json = gestionnaireCrypto.dechiffrer(jsonChiffre);

                    // Parser le JSON
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Map<String, Object>>>() {
                    }.getType();
                    List<Map<String, Object>> rapports = gson.fromJson(json, listType);

                    // Stocker la liste complète
                    listeRapportsComplets = rapports;

                    // Mettre à jour la table
                    SwingUtilities.invokeLater(() -> {
                        modeleTable.setRowCount(0);
                        for (Map<String, Object> rapport : rapports) {
                            int id = ((Double) rapport.get("id")).intValue();
                            int patId = ((Double) rapport.get("patientId")).intValue();
                            String date = (String) rapport.get("dateRapport");
                            String texte = (String) rapport.get("texteRapport");

                            String apercu = texte.length() > 50 ? texte.substring(0, 50) + "..." : texte;

                            modeleTable.addRow(new Object[] { id, patId, date, apercu });
                        }

                        JOptionPane.showMessageDialog(this,
                                count + " rapport(s) chargé(s)",
                                "Succès",
                                JOptionPane.INFORMATION_MESSAGE);

                        boutonCharger.setEnabled(true);
                        boutonTous.setEnabled(true);
                    });

                } else {
                    SwingUtilities.invokeLater(() -> {
                        String message = reponse.substring(Protocol.RESP_ERROR.length() + 1);
                        JOptionPane.showMessageDialog(this,
                                "Erreur: " + message,
                                "Erreur",
                                JOptionPane.ERROR_MESSAGE);
                        boutonCharger.setEnabled(true);
                        boutonTous.setEnabled(true);
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
                    boutonTous.setEnabled(true);
                });
            }
        }).start();
    }

    /**
     * Affiche le contenu complet du rapport sélectionné dans le tableau.
     * Récupère le texte complet depuis la liste en mémoire.
     */
    private void afficherRapportSelectionne() {
        int ligneSelectionnee = tableRapports.getSelectedRow();
        if (ligneSelectionnee >= 0) {
            // Récupérer l'ID du rapport sélectionné (colonne 0)
            int idSelectionne = (int) modeleTable.getValueAt(ligneSelectionnee, 0);

            // Chercher le contenu complet dans la liste
            if (listeRapportsComplets != null) {
                for (Map<String, Object> rapport : listeRapportsComplets) {
                    int id = ((Double) rapport.get("id")).intValue();
                    if (id == idSelectionne) {
                        String texteComplet = (String) rapport.get("texteRapport");
                        zoneTexteRapport.setText(texteComplet);
                        return;
                    }
                }
            }

            // Fallback
            String apercu = (String) modeleTable.getValueAt(ligneSelectionnee, 3);
            zoneTexteRapport.setText(apercu);
        }
    }

    /**
     * Charge la liste des patients pour le filtre.
     */
    private void chargerPatients() {
        new Thread(() -> {
            try {
                gestionnaireConnexion.envoyerRequete(Protocol.CMD_LIST_PATIENTS);
                String reponse = gestionnaireConnexion.recevoirReponse();

                SwingUtilities.invokeLater(() -> {
                    if (reponse.startsWith(Protocol.RESP_OK)) {
                        String[] parties = reponse.split("\\|");
                        for (int i = 1; i < parties.length; i++) {
                            String[] infos = parties[i].split(",");
                            if (infos.length >= 3) {
                                int id = Integer.parseInt(infos[0]);
                                String prenom = infos[1];
                                String nom = infos[2];
                                comboPatients.addItem(new PatientItem(id, prenom, nom));
                            }
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Classe interne pour les éléments de la combobox patients.
     */
    private static class PatientItem {
        private int id;
        private String prenom;
        private String nom;

        public PatientItem(int id, String prenom, String nom) {
            this.id = id;
            this.prenom = prenom;
            this.nom = nom;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            if (id == -1)
                return prenom + " " + nom;
            return id + " - " + prenom + " " + nom;
        }
    }
}
