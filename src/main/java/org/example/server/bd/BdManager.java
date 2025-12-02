package org.example.server.bd;

import org.example.server.dao.ConsultationDAO;
import org.example.server.dao.DoctorDAO;
import org.example.server.dao.PatientDAO;
import org.example.server.dao.ReportDAO;
import org.example.server.entity.Consultation;
import org.example.server.entity.Doctor;
import org.example.server.entity.Patient;
import org.example.server.entity.Report;
import org.example.server.searchvm.ConsultationSearchVM;
import org.example.server.searchvm.DoctorSearchVM;
import org.example.server.searchvm.PatientSearchVM;
import org.example.server.searchvm.ReportSearchVM;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.List;
import java.util.Properties;

/**
 * Gestionnaire de base de données utilisant le pattern DAO (Data Access
 * Object).
 * Cette classe agit comme une façade pour accéder aux différents DAO (Doctor,
 * Patient, Consultation, Report).
 * Elle gère également la connexion à la base de données et le chargement de la
 * configuration.
 */
public class BdManager {
    private Properties properties;
    private Connection connexion;

    // DAOs
    private DoctorDAO doctorDAO;
    private PatientDAO patientDAO;
    private ConsultationDAO consultationDAO;
    private ReportDAO reportDAO;

    public BdManager() {
        properties = new Properties();
        loadProperties();
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Erreur : le fichier config.properties n'a pas été trouvé");
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            System.out.println("Erreur : " + ex.getMessage());
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Établit la connexion à la base de données en utilisant les paramètres du
     * fichier de configuration.
     * Initialise également les instances des DAO.
     *
     * @throws SQLException En cas d'erreur de connexion
     */
    public void connecter() throws SQLException {
        String url = getProperty("DB_URL");
        String user = getProperty("DB_USER");
        String password = getProperty("DB_PASSWORD");
        connexion = DriverManager.getConnection(url, user, password);

        // Initialiser les DAOs
        doctorDAO = new DoctorDAO(connexion);
        patientDAO = new PatientDAO(connexion);
        consultationDAO = new ConsultationDAO(connexion);
        reportDAO = new ReportDAO(connexion);
    }

    /**
     * Ferme proprement la connexion à la base de données si elle est ouverte.
     *
     * @throws SQLException En cas d'erreur lors de la fermeture
     */
    public void deconnecter() throws SQLException {
        if (connexion != null && !connexion.isClosed()) {
            connexion.close();
        }
    }

    // ==================== Méthodes Doctor ====================

    /**
     * Récupère le mot de passe haché d'un médecin via son login.
     *
     * @param login Le login du médecin
     * @return Le mot de passe haché
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    public String getMotDePasseMedecin(String login) throws SQLException {
        return doctorDAO.getMotDePasseMedecin(login);
    }

    /**
     * Vérifie l'existence d'un médecin dans la base de données.
     *
     * @param login Le login du médecin
     * @return true si le médecin existe, false sinon
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    public boolean medecinExiste(String login) throws SQLException {
        return doctorDAO.medecinExiste(login);
    }

    /**
     * Récupère l'identifiant unique d'un médecin.
     *
     * @param login Le login du médecin
     * @return L'ID du médecin
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    public Integer getIdMedecin(String login) throws SQLException {
        return doctorDAO.getIdMedecin(login);
    }

    /**
     * Recherche des médecins selon des critères spécifiques.
     *
     * @param dsearchvm Les critères de recherche
     * @return Une liste de médecins correspondant aux critères
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    public List<Doctor> loadDoctors(DoctorSearchVM dsearchvm) throws SQLException {
        return doctorDAO.load(dsearchvm);
    }

    // ==================== Méthodes Consultation ====================

    /**
     * Vérifie si une relation de consultation existe entre un médecin et un
     * patient.
     *
     * @param medecinId L'ID du médecin
     * @param patientId L'ID du patient
     * @return true si une consultation existe, false sinon
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    public boolean medecinAConsultationAvecPatient(int medecinId, int patientId) throws SQLException {
        return consultationDAO.medecinAConsultationAvecPatient(medecinId, patientId);
    }

    /**
     * Recherche des consultations selon des critères spécifiques.
     *
     * @param csearchvm Les critères de recherche
     * @return Une liste de consultations correspondant aux critères
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    public List<Consultation> loadConsultations(ConsultationSearchVM csearchvm) throws SQLException {
        return consultationDAO.load(csearchvm);
    }

    // ==================== Méthodes Patient ====================

    /**
     * Liste tous les patients disponibles dans la base de données.
     *
     * @return Une liste de tous les patients
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    public List<Patient> listerTousPatients() throws SQLException {
        return patientDAO.load();
    }

    /**
     * Recherche des patients selon des critères spécifiques.
     *
     * @param psearchvm Les critères de recherche
     * @return Une liste de patients correspondant aux critères
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    public List<Patient> load(PatientSearchVM psearchvm) throws SQLException {
        return patientDAO.load(psearchvm);
    }

    // ==================== Méthodes Report ====================

    /**
     * Ajoute un nouveau rapport médical.
     *
     * @param medecinId    L'ID du médecin créateur
     * @param patientId    L'ID du patient concerné
     * @param date         La date du rapport
     * @param texteRapport Le contenu du rapport
     * @return L'ID du nouveau rapport
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    public int ajouterRapport(int medecinId, int patientId, String date, String texteRapport) throws SQLException {
        return reportDAO.ajouterRapport(medecinId, patientId, date, texteRapport);
    }

    /**
     * Modifie le contenu d'un rapport existant.
     *
     * @param rapportId    L'ID du rapport à modifier
     * @param nouveauTexte Le nouveau texte du rapport
     * @param medecinId    L'ID du médecin (pour vérification des droits)
     * @return true si la modification a réussi
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    public boolean modifierRapport(int rapportId, String nouveauTexte, int medecinId) throws SQLException {
        return reportDAO.modifierRapport(rapportId, nouveauTexte, medecinId);
    }

    /**
     * Liste tous les rapports créés par un médecin.
     *
     * @param medecinId L'ID du médecin
     * @return Une liste de rapports
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    public List<ReportDAO.Rapport> listerRapportsMedecin(int medecinId) throws SQLException {
        return reportDAO.listerRapportsMedecin(medecinId);
    }

    /**
     * Liste les rapports d'un médecin pour un patient spécifique.
     *
     * @param medecinId L'ID du médecin
     * @param patientId L'ID du patient
     * @return Une liste de rapports filtrée
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    public List<ReportDAO.Rapport> listerRapportsMedecinPatient(int medecinId, int patientId) throws SQLException {
        return reportDAO.listerRapportsMedecinPatient(medecinId, patientId);
    }

    /**
     * Recherche des rapports selon des critères spécifiques.
     *
     * @param rsearchvm Les critères de recherche
     * @return Une liste de rapports correspondant aux critères
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    public List<Report> loadReports(ReportSearchVM rsearchvm) throws SQLException {
        return reportDAO.load(rsearchvm);
    }

    // ==================== Classe Rapport (pour compatibilité) ====================

    /**
     * @deprecated Utiliser ReportDAO.Rapport à la place
     */
    @Deprecated
    public static class Rapport extends ReportDAO.Rapport {
        public Rapport(int id, int medecinId, int patientId, Date dateRapport, String texteRapport) {
            super(id, medecinId, patientId, dateRapport, texteRapport);
        }
    }

    public static void main(String[] args) {
        BdManager manager = new BdManager();
        try {
            manager.connecter();
            System.out.println("✓ Connexion réussie à la base de données.");
            manager.deconnecter();
            System.out.println("✓ Déconnexion réussie de la base de données.");
        } catch (SQLException e) {
            System.err.println("✗ Erreur de connexion: " + e.getMessage());
        }
    }
}
