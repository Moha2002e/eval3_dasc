package org.example.server.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.example.server.entity.Report;
import org.example.server.searchvm.ReportSearchVM;

/**
 * DAO (Data Access Object) pour la gestion des entités {@link Report}.
 * Permet de créer, modifier, lister et rechercher des rapports médicaux.
 */
public class ReportDAO {

    private Connection connexion;

    public ReportDAO(Connection connexion) {
        this.connexion = connexion;
    }

    /**
     * Crée un nouveau rapport médical dans la base de données.
     *
     * @param medecinId    L'ID du médecin créateur
     * @param patientId    L'ID du patient concerné
     * @param date         La date du rapport (format YYYY-MM-DD)
     * @param texteRapport Le contenu textuel du rapport
     * @return L'ID du rapport généré, ou -1 en cas d'échec
     * @throws SQLException En cas d'erreur d'accès à la base de données
     */
    public int ajouterRapport(int medecinId, int patientId, String date, String texteRapport) throws SQLException {
        ArrayList<Report> reports = new ArrayList<>();
        String sql = "INSERT INTO reports (doctor_id, patient_id, date_rapport, texte_rapport) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, medecinId);
            stmt.setInt(2, patientId);
            stmt.setDate(3, Date.valueOf(date));
            stmt.setString(4, texteRapport);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Met à jour le contenu d'un rapport médical existant.
     * Vérifie que le rapport appartient bien au médecin qui tente de le modifier.
     *
     * @param rapportId    L'ID du rapport à modifier
     * @param nouveauTexte Le nouveau contenu du rapport
     * @param medecinId    L'ID du médecin effectuant la modification (pour
     *                     vérification)
     * @return true si la modification a réussi, false sinon (ex: rapport
     *         introuvable ou mauvais médecin)
     * @throws SQLException En cas d'erreur d'accès à la base de données
     */
    public boolean modifierRapport(int rapportId, String nouveauTexte, int medecinId) throws SQLException {
        String sql = "UPDATE reports SET texte_rapport = ? WHERE id = ? AND doctor_id = ?";
        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setString(1, nouveauTexte);
            stmt.setInt(2, rapportId);
            stmt.setInt(3, medecinId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Liste tous les rapports créés par un médecin donné, triés par date
     * décroissante.
     *
     * @param medecinId L'ID du médecin
     * @return Une liste de rapports
     * @throws SQLException En cas d'erreur d'accès à la base de données
     */
    public List<Rapport> listerRapportsMedecin(int medecinId) throws SQLException {
        String sql = "SELECT * FROM reports WHERE doctor_id = ? ORDER BY date_rapport DESC";
        List<Rapport> rapports = new ArrayList<>();
        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setInt(1, medecinId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rapports.add(new Rapport(
                        rs.getInt("id"),
                        rs.getInt("doctor_id"),
                        rs.getInt("patient_id"),
                        rs.getDate("date_rapport"),
                        rs.getString("texte_rapport")));
            }
        }
        return rapports;
    }

    /**
     * Liste les rapports d'un médecin concernant un patient spécifique.
     *
     * @param medecinId L'ID du médecin
     * @param patientId L'ID du patient
     * @return Une liste de rapports filtrée
     * @throws SQLException En cas d'erreur d'accès à la base de données
     */
    public List<Rapport> listerRapportsMedecinPatient(int medecinId, int patientId) throws SQLException {
        String sql = "SELECT * FROM reports WHERE doctor_id = ? AND patient_id = ? ORDER BY date_rapport DESC";
        List<Rapport> rapports = new ArrayList<>();
        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setInt(1, medecinId);
            stmt.setInt(2, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rapports.add(new Rapport(
                        rs.getInt("id"),
                        rs.getInt("doctor_id"),
                        rs.getInt("patient_id"),
                        rs.getDate("date_rapport"),
                        rs.getString("texte_rapport")));
            }
        }
        return rapports;
    }

    /**
     * Recherche des rapports en fonction de critères dynamiques.
     * Effectue des jointures pour inclure les noms des patients et médecins.
     *
     * @param rsearchvm L'objet contenant les critères de recherche (noms, dates,
     *                  contenu)
     * @return Une liste de rapports correspondant aux critères
     */
    public ArrayList<Report> load(ReportSearchVM rsearchvm) {
        ArrayList<Report> reports = new ArrayList<>();
        try {
            String query = "SELECT r.*, p.first_name AS p_first_name, p.last_name AS p_last_name, d.last_name AS d_last_name "
                    +
                    "FROM reports r " +
                    "LEFT JOIN patient p ON r.patient_id = p.id " +
                    "LEFT JOIN doctor d ON r.doctor_id = d.id " +
                    "WHERE 1=1 ";

            if (rsearchvm != null) {
                if (rsearchvm.getPatientName() != null && !rsearchvm.getPatientName().isEmpty()) {
                    query += "AND p.last_name LIKE ? ";
                }
                if (rsearchvm.getDoctorName() != null && !rsearchvm.getDoctorName().isEmpty()) {
                    query += "AND d.last_name LIKE ? ";
                }
                if (rsearchvm.getDateFrom() != null && !rsearchvm.getDateFrom().isEmpty()) {
                    query += "AND r.date_rapport >= ? ";
                }
                if (rsearchvm.getDateTo() != null && !rsearchvm.getDateTo().isEmpty()) {
                    query += "AND r.date_rapport <= ? ";
                }
                if (rsearchvm.getContent() != null && !rsearchvm.getContent().isEmpty()) {
                    query += "AND r.texte_rapport LIKE ? ";
                }
            }

            query += " ORDER BY r.date_rapport DESC";

            PreparedStatement ps = connexion.prepareStatement(query);

            if (rsearchvm != null) {
                int index = 1;
                if (rsearchvm.getPatientName() != null && !rsearchvm.getPatientName().isEmpty()) {
                    ps.setString(index++, "%" + rsearchvm.getPatientName() + "%");
                }
                if (rsearchvm.getDoctorName() != null && !rsearchvm.getDoctorName().isEmpty()) {
                    ps.setString(index++, "%" + rsearchvm.getDoctorName() + "%");
                }
                if (rsearchvm.getDateFrom() != null && !rsearchvm.getDateFrom().isEmpty()) {
                    ps.setString(index++, rsearchvm.getDateFrom());
                }
                if (rsearchvm.getDateTo() != null && !rsearchvm.getDateTo().isEmpty()) {
                    ps.setString(index++, rsearchvm.getDateTo());
                }
                if (rsearchvm.getContent() != null && !rsearchvm.getContent().isEmpty()) {
                    ps.setString(index++, "%" + rsearchvm.getContent() + "%");
                }
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Report report = new Report();
                report.setId(rs.getInt("id"));
                report.setDoctor_id(rs.getInt("doctor_id"));
                report.setPatient_id(rs.getInt("patient_id"));
                report.setDate(rs.getString("date_rapport"));
                report.setContent(rs.getString("texte_rapport"));
                // report.setTitle(rs.getString("title")); // Uncomment if title column exists

                reports.add(report);
            }
            rs.close();
            ps.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return reports;
    }

    /**
     * Classe interne représentant un rapport (pour compatibilité avec l'existant).
     * Note: Il est recommandé d'utiliser l'entité {@link Report} pour les nouveaux
     * développements.
     */
    public static class Rapport {
        public int id;
        public int medecinId;
        public int patientId;
        public Date dateRapport;
        public String texteRapport;

        public Rapport(int id, int medecinId, int patientId, Date dateRapport, String texteRapport) {
            this.id = id;
            this.medecinId = medecinId;
            this.patientId = patientId;
            this.dateRapport = dateRapport;
            this.texteRapport = texteRapport;
        }
    }
}
