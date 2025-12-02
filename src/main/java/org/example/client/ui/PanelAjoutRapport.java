package org.example.client.ui;

import org.example.client.crypto.GestionnaireCryptoClient;
import org.example.client.reseau.GestionnaireConnexion;
import org.example.shared.Protocol;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Base64;

/**
 * Panneau permettant à un médecin d'ajouter un nouveau rapport médical.
 * Ce formulaire permet de sélectionner un patient, une date et de saisir le
 * contenu du rapport.
 * Les données sont chiffrées et signées avant d'être envoyées au serveur.
 */
public class PanelAjoutRapport extends JPanel {

    private GestionnaireConnexion gestionnaireConnexion;
    private GestionnaireCryptoClient gestionnaireCrypto;

    private JComboBox<PatientItem> comboPatients;
    private JSpinner spinnerDate;
    private JTextArea zoneTexteRapport;
    private JButton boutonEnregistrer;
    private JButton boutonRafraichir;

    /**
     * Constructeur du panneau d'ajout de rapport.
     *
     * @param connexion Le gestionnaire de connexion réseau
     * @param crypto    Le gestionnaire de cryptographie
     */
    public PanelAjoutRapport(GestionnaireConnexion connexion, GestionnaireCryptoClient crypto) {
        this.gestionnaireConnexion = connexion;
        this.gestionnaireCrypto = crypto;

        initialiserInterface();
    }

    /**
     * Initialise l'interface graphique du formulaire.
     * Configure les champs de sélection du patient, de la date et de saisie du
     * texte.
     */
    private void initialiserInterface() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel formulaire
        JPanel panelFormulaire = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Patient
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        panelFormulaire.add(new JLabel("Patient:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        comboPatients = new JComboBox<>();
        panelFormulaire.add(comboPatients, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.1;
        boutonRafraichir = new JButton("🔄");
        boutonRafraichir.setToolTipText("Rafraîchir la liste des patients");
        boutonRafraichir.addActionListener(e -> chargerPatients());
        panelFormulaire.add(boutonRafraichir, gbc);

        // Date
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        panelFormulaire.add(new JLabel("Date:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.gridwidth = 2; // Span across refresh button column
        SpinnerDateModel model = new SpinnerDateModel();
        spinnerDate = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerDate, "yyyy-MM-dd");
        spinnerDate.setEditor(editor);
        spinnerDate.setValue(java.sql.Date.valueOf(LocalDate.now()));
        panelFormulaire.add(spinnerDate, gbc);
        gbc.gridwidth = 1; // Reset

        // Charger les patients au démarrage
        chargerPatients();

        add(panelFormulaire, BorderLayout.NORTH);

        // Zone de texte pour le rapport
        JPanel panelTexte = new JPanel(new BorderLayout());
        panelTexte.setBorder(BorderFactory.createTitledBorder("Contenu du Rapport Médical"));

        zoneTexteRapport = new JTextArea();
        zoneTexteRapport.setLineWrap(true);
        zoneTexteRapport.setWrapStyleWord(true);
        zoneTexteRapport.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(zoneTexteRapport);
        panelTexte.add(scrollPane, BorderLayout.CENTER);

        add(panelTexte, BorderLayout.CENTER);

        // Bouton enregistrer
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        boutonEnregistrer = new JButton("📝 Enregistrer le Rapport");
        boutonEnregistrer.setFont(new Font("Arial", Font.BOLD, 14));
        boutonEnregistrer.addActionListener(e -> enregistrerRapport());
        panelBoutons.add(boutonEnregistrer);

        add(panelBoutons, BorderLayout.SOUTH);
    }

    /**
     * Gère l'enregistrement du rapport.
     * Cette méthode est appelée lors du clic sur le bouton "Enregistrer".
     * Elle effectue les opérations suivantes :
     * 1. Validation des champs.
     * 2. Chiffrement des données sensibles (date, ID patient, contenu).
     * 3. Signature numérique des données concaténées.
     * 4. Envoi de la requête ADD_REPORT au serveur.
     * 5. Affichage du résultat.
     */
    private void enregistrerRapport() {
        PatientItem patientSelectionne = (PatientItem) comboPatients.getSelectedItem();
        if (patientSelectionne == null) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un patient",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String patientId = String.valueOf(patientSelectionne.getId());

        java.util.Date dateValue = (java.util.Date) spinnerDate.getValue();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(dateValue);

        String texteRapport = zoneTexteRapport.getText().trim();

        if (patientId.isEmpty() || date.isEmpty() || texteRapport.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez remplir tous les champs",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boutonEnregistrer.setEnabled(false);
        boutonEnregistrer.setText("Enregistrement...");

        new Thread(() -> {
            try {
                // Chiffrer les données
                byte[] dateChiffree = gestionnaireCrypto.chiffrer(date);
                byte[] patientIdChiffre = gestionnaireCrypto.chiffrer(patientId);
                byte[] texteChiffre = gestionnaireCrypto.chiffrer(texteRapport);

                // Signer les données
                byte[] donneesASignier = (date + patientId + texteRapport).getBytes();
                byte[] signature = gestionnaireCrypto.signer(donneesASignier);

                // Encoder en Base64
                String dateBase64 = Base64.getEncoder().encodeToString(dateChiffree);
                String patientIdBase64 = Base64.getEncoder().encodeToString(patientIdChiffre);
                String texteBase64 = Base64.getEncoder().encodeToString(texteChiffre);
                String signatureBase64 = Base64.getEncoder().encodeToString(signature);

                // Envoyer la requête
                String requete = Protocol.CMD_ADD_REPORT + "|" + dateBase64 + "|" +
                        patientIdBase64 + "|" + texteBase64 + "|" + signatureBase64;
                gestionnaireConnexion.envoyerRequete(requete);

                // Recevoir la réponse
                String reponse = gestionnaireConnexion.recevoirReponse();

                SwingUtilities.invokeLater(() -> {
                    if (reponse.startsWith(Protocol.RESP_OK)) {
                        String[] parties = reponse.split("\\|");
                        String rapportId = parties[1];
                        JOptionPane.showMessageDialog(this,
                                "Rapport enregistré avec succès!\nID: " + rapportId,
                                "Succès",
                                JOptionPane.INFORMATION_MESSAGE);

                        // Réinitialiser le formulaire
                        // comboPatients.setSelectedIndex(0); // Optional: reset selection
                        spinnerDate.setValue(java.sql.Date.valueOf(LocalDate.now()));
                        zoneTexteRapport.setText("");
                    } else {
                        String message = reponse.substring(Protocol.RESP_ERROR.length() + 1);
                        JOptionPane.showMessageDialog(this,
                                "Erreur: " + message,
                                "Erreur",
                                JOptionPane.ERROR_MESSAGE);
                    }

                    boutonEnregistrer.setEnabled(true);
                    boutonEnregistrer.setText("📝 Enregistrer le Rapport");
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Erreur: " + ex.getMessage(),
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    boutonEnregistrer.setEnabled(true);
                    boutonEnregistrer.setText("📝 Enregistrer le Rapport");
                });
            }
        }).start();
    }

    /**
     * Charge la liste des patients disponibles depuis le serveur.
     * Met à jour la liste déroulante des patients.
     */
    private void chargerPatients() {
        boutonRafraichir.setEnabled(false);
        comboPatients.removeAllItems();

        new Thread(() -> {
            try {
                gestionnaireConnexion.envoyerRequete(Protocol.CMD_LIST_PATIENTS);
                String reponse = gestionnaireConnexion.recevoirReponse();

                SwingUtilities.invokeLater(() -> {
                    if (reponse.startsWith(Protocol.RESP_OK)) {
                        String[] parties = reponse.split("\\|");
                        // Format: OK|id,prenom,nom,date|id,prenom,nom,date|...
                        for (int i = 1; i < parties.length; i++) {
                            String[] infos = parties[i].split(",");
                            if (infos.length >= 3) {
                                int id = Integer.parseInt(infos[0]);
                                String prenom = infos[1];
                                String nom = infos[2];
                                comboPatients.addItem(new PatientItem(id, prenom, nom));
                            }
                        }
                    } else {
                        // Gérer erreur silencieusement ou log
                        System.err.println("Erreur chargement patients: " + reponse);
                    }
                    boutonRafraichir.setEnabled(true);
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> boutonRafraichir.setEnabled(true));
            }
        }).start();
    }

    /**
     * Classe interne représentant un élément de la liste des patients.
     * Permet de stocker l'ID et le nom complet pour l'affichage dans le JComboBox.
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
            return id + " - " + prenom + " " + nom;
        }
    }
}
