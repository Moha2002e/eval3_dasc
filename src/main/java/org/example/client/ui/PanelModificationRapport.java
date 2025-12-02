package org.example.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.client.crypto.GestionnaireCryptoClient;
import org.example.client.reseau.GestionnaireConnexion;
import org.example.shared.Protocol;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Panneau permettant à un médecin de modifier un rapport médical existant.
 * Permet de sélectionner un rapport, de visualiser son contenu actuel et de le
 * modifier.
 * Les modifications sont chiffrées avant d'être envoyées au serveur.
 */
public class PanelModificationRapport extends JPanel {

    private GestionnaireConnexion gestionnaireConnexion;
    private GestionnaireCryptoClient gestionnaireCrypto;

    private JComboBox<ReportItem> comboRapports;
    private JTextArea zoneTexteRapport;
    private JButton boutonModifier;
    private JButton boutonCharger;

    /**
     * Constructeur du panneau de modification de rapport.
     *
     * @param connexion Le gestionnaire de connexion réseau
     * @param crypto    Le gestionnaire de cryptographie
     */
    public PanelModificationRapport(GestionnaireConnexion connexion, GestionnaireCryptoClient crypto) {
        this.gestionnaireConnexion = connexion;
        this.gestionnaireCrypto = crypto;

        initialiserInterface();
    }

    /**
     * Initialise l'interface graphique.
     * Configure la sélection du rapport et la zone d'édition.
     */
    private void initialiserInterface() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel ID rapport
        JPanel panelId = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.weightx = 0.2;
        panelId.add(new JLabel("Rapport à modifier:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        comboRapports = new JComboBox<>();
        comboRapports.addActionListener(e -> afficherRapportSelectionne());
        panelId.add(comboRapports, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.1;
        boutonCharger = new JButton("🔄");
        boutonCharger.setToolTipText("Charger la liste des rapports");
        boutonCharger.addActionListener(e -> chargerRapports());
        panelId.add(boutonCharger, gbc);

        add(panelId, BorderLayout.NORTH);

        // Zone de texte pour le nouveau contenu
        JPanel panelTexte = new JPanel(new BorderLayout());
        panelTexte.setBorder(BorderFactory.createTitledBorder("Nouveau Contenu du Rapport"));

        zoneTexteRapport = new JTextArea();
        zoneTexteRapport.setLineWrap(true);
        zoneTexteRapport.setWrapStyleWord(true);
        zoneTexteRapport.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(zoneTexteRapport);
        panelTexte.add(scrollPane, BorderLayout.CENTER);

        add(panelTexte, BorderLayout.CENTER);

        // Charger les rapports au démarrage (après initialisation de zoneTexteRapport)
        chargerRapports();

        // Bouton modifier
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        boutonModifier = new JButton("✏️ Modifier le Rapport");
        boutonModifier.setFont(new Font("Arial", Font.BOLD, 14));
        boutonModifier.addActionListener(e -> modifierRapport());
        panelBoutons.add(boutonModifier);

        add(panelBoutons, BorderLayout.SOUTH);
    }

    /**
     * Gère la modification du rapport.
     * Cette méthode est appelée lors du clic sur le bouton "Modifier".
     * Elle chiffre le nouveau contenu et envoie la requête EDIT_REPORT au serveur.
     */
    private void modifierRapport() {
        ReportItem rapportSelectionne = (ReportItem) comboRapports.getSelectedItem();
        if (rapportSelectionne == null) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un rapport",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String rapportId = String.valueOf(rapportSelectionne.getId());
        String nouveauTexte = zoneTexteRapport.getText().trim();

        if (rapportId.isEmpty() || nouveauTexte.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez remplir tous les champs",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boutonModifier.setEnabled(false);
        boutonModifier.setText("Modification...");

        new Thread(() -> {
            try {
                // Chiffrer les données
                byte[] rapportIdChiffre = gestionnaireCrypto.chiffrer(rapportId);
                byte[] texteChiffre = gestionnaireCrypto.chiffrer(nouveauTexte);

                // Encoder en Base64
                String rapportIdBase64 = Base64.getEncoder().encodeToString(rapportIdChiffre);
                String texteBase64 = Base64.getEncoder().encodeToString(texteChiffre);

                // Envoyer la requête
                String requete = Protocol.CMD_EDIT_REPORT + "|" + rapportIdBase64 + "|" + texteBase64;
                gestionnaireConnexion.envoyerRequete(requete);

                // Recevoir la réponse
                String reponse = gestionnaireConnexion.recevoirReponse();

                SwingUtilities.invokeLater(() -> {
                    if (reponse.startsWith(Protocol.RESP_OK)) {
                        JOptionPane.showMessageDialog(this,
                                "Rapport modifié avec succès!",
                                "Succès",
                                JOptionPane.INFORMATION_MESSAGE);

                        // Réinitialiser le formulaire
                        // comboRapports.setSelectedIndex(-1);
                        zoneTexteRapport.setText("");
                        chargerRapports(); // Recharger la liste pour mettre à jour si besoin
                        zoneTexteRapport.setText("");
                    } else {
                        String message = reponse.substring(Protocol.RESP_ERROR.length() + 1);
                        JOptionPane.showMessageDialog(this,
                                "Erreur: " + message,
                                "Erreur",
                                JOptionPane.ERROR_MESSAGE);
                    }

                    boutonModifier.setEnabled(true);
                    boutonModifier.setText("✏️ Modifier le Rapport");
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Erreur: " + ex.getMessage(),
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    boutonModifier.setEnabled(true);
                    boutonModifier.setText("✏️ Modifier le Rapport");
                });
            }
        }).start();
    }

    /**
     * Charge la liste des rapports disponibles pour le médecin connecté.
     * Récupère, vérifie (HMAC) et déchiffre les rapports pour remplir la liste
     * déroulante.
     */
    private void chargerRapports() {
        boutonCharger.setEnabled(false);
        comboRapports.removeAllItems();
        zoneTexteRapport.setText("");

        new Thread(() -> {
            try {
                // Demander tous les rapports du médecin
                String requete = Protocol.CMD_LIST_REPORTS + "|";
                gestionnaireConnexion.envoyerRequete(requete);
                String reponse = gestionnaireConnexion.recevoirReponse();

                if (reponse.startsWith(Protocol.RESP_OK)) {
                    String[] parties = reponse.split("\\|");
                    // OK|count|json_encrypted|hmac
                    if (parties.length >= 4) {
                        String jsonChiffreBase64 = parties[2];
                        String hmacBase64 = parties[3];

                        byte[] jsonChiffre = Base64.getDecoder().decode(jsonChiffreBase64);
                        byte[] hmac = Base64.getDecoder().decode(hmacBase64);

                        if (gestionnaireCrypto.verifierHMAC(jsonChiffre, hmac)) {
                            String json = gestionnaireCrypto.dechiffrer(jsonChiffre);

                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<Map<String, Object>>>() {
                            }.getType();
                            List<Map<String, Object>> rapports = gson.fromJson(json, listType);

                            SwingUtilities.invokeLater(() -> {
                                for (Map<String, Object> rapport : rapports) {
                                    int id = ((Double) rapport.get("id")).intValue();
                                    String date = (String) rapport.get("dateRapport");
                                    String texte = (String) rapport.get("texteRapport");
                                    // On pourrait aussi récupérer l'ID patient si dispo
                                    comboRapports.addItem(new ReportItem(id, date, texte));
                                }
                                boutonCharger.setEnabled(true);
                            });
                        }
                    }
                } else {
                    SwingUtilities.invokeLater(() -> boutonCharger.setEnabled(true));
                }
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> boutonCharger.setEnabled(true));
            }
        }).start();
    }

    /**
     * Affiche le contenu du rapport sélectionné dans la zone de texte.
     */
    private void afficherRapportSelectionne() {
        ReportItem item = (ReportItem) comboRapports.getSelectedItem();
        if (item != null) {
            zoneTexteRapport.setText(item.getTexte());
        } else {
            zoneTexteRapport.setText("");
        }
    }

    /**
     * Classe interne pour les éléments de la combobox rapports.
     */
    private static class ReportItem {
        private int id;
        private String date;
        private String texte;

        public ReportItem(int id, String date, String texte) {
            this.id = id;
            this.date = date;
            this.texte = texte;
        }

        public int getId() {
            return id;
        }

        public String getTexte() {
            return texte;
        }

        @Override
        public String toString() {
            return "Rapport #" + id + " (" + date + ")";
        }
    }
}
