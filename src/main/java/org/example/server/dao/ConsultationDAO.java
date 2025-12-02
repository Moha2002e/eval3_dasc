package org.example.server.dao;

import java.sql.*;
import java.util.ArrayList;
import org.example.server.entity.Consultation;
import org.example.server.searchvm.ConsultationSearchVM;

/**
 * DAO (Data Access Object) pour la gestion des entités {@link Consultation}.
 * Permet de vérifier les relations médecin-patient et de rechercher des
 * consultations.
 */
public class ConsultationDAO {

    private Connection connexion;

    public ConsultationDAO(Connection connexion) {
        this.connexion = connexion;
    }

    /**
     * Vérifie si un médecin a eu au moins une consultation avec un patient donné.
     * Cette méthode est utilisée pour vérifier les droits d'accès avant de
     * permettre
     * à un médecin de voir ou créer des rapports pour un patient.
     *
     * @param medecinId L'ID du médecin
     * @param patientId L'ID du patient
     * @return true si une consultation existe entre les deux, false sinon
     * @throws SQLException En cas d'erreur d'accès à la base de données
     */
    public boolean medecinAConsultationAvecPatient(int medecinId, int patientId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM consultations WHERE doctor_id = ? AND patient_id = ?";
        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setInt(1, medecinId);
            stmt.setInt(2, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Recherche des consultations en fonction de critères dynamiques.
     * Effectue des jointures avec les tables Patient et Doctor pour récupérer les
     * détails associés.
     *
     * @param csearchvm L'objet contenant les critères de recherche (noms, dates,
     *                  motif)
     * @return Une liste de consultations enrichies avec les infos patient/médecin
     */
    public ArrayList<Consultation> load(ConsultationSearchVM csearchvm) {
        ArrayList<Consultation> consultations = new ArrayList<>();
        try {
            String query = "SELECT c.*, p.first_name AS p_first_name, p.last_name AS p_last_name, p.birth_date AS p_birth_date "
                    +
                    "FROM consultations c " +
                    "LEFT JOIN patient p ON c.patient_id = p.id " +
                    "LEFT JOIN doctor d ON c.doctor_id = d.id " +
                    "WHERE 1=1 ";

            if (csearchvm != null) {
                if (csearchvm.getPatientName() != null && !csearchvm.getPatientName().isEmpty()) {
                    query += "AND p.last_name LIKE ? ";
                }
                if (csearchvm.getDoctorName() != null && !csearchvm.getDoctorName().isEmpty()) {
                    query += "AND d.last_name LIKE ? ";
                }
                if (csearchvm.getDateFrom() != null && !csearchvm.getDateFrom().isEmpty()) {
                    query += "AND c.date >= ? ";
                }
                if (csearchvm.getDateTo() != null && !csearchvm.getDateTo().isEmpty()) {
                    query += "AND c.date <= ? ";
                }
                if (csearchvm.getReason() != null && !csearchvm.getReason().isEmpty()) {
                    query += "AND c.reason LIKE ? ";
                }
            }

            query += " ORDER BY c.date DESC";

            PreparedStatement ps = connexion.prepareStatement(query);

            if (csearchvm != null) {
                int index = 1;
                if (csearchvm.getPatientName() != null && !csearchvm.getPatientName().isEmpty()) {
                    ps.setString(index++, "%" + csearchvm.getPatientName() + "%");
                }
                if (csearchvm.getDoctorName() != null && !csearchvm.getDoctorName().isEmpty()) {
                    ps.setString(index++, "%" + csearchvm.getDoctorName() + "%");
                }
                if (csearchvm.getDateFrom() != null && !csearchvm.getDateFrom().isEmpty()) {
                    ps.setString(index++, csearchvm.getDateFrom());
                }
                if (csearchvm.getDateTo() != null && !csearchvm.getDateTo().isEmpty()) {
                    ps.setString(index++, csearchvm.getDateTo());
                }
                if (csearchvm.getReason() != null && !csearchvm.getReason().isEmpty()) {
                    ps.setString(index++, "%" + csearchvm.getReason() + "%");
                }
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Consultation consultation = new Consultation();
                consultation.setId(rs.getInt("id"));
                consultation.setDoctor_id(rs.getInt("doctor_id"));
                consultation.setPatient_id(rs.getInt("patient_id"));
                consultation.setDate(rs.getString("date"));
                consultation.setHour(rs.getString("hour"));
                consultation.setReason(rs.getString("reason"));

                consultation.setPatient_first_name(rs.getString("p_first_name"));
                consultation.setPatient_last_name(rs.getString("p_last_name"));
                consultation.setPatient_birth_date(rs.getString("p_birth_date"));

                consultations.add(consultation);
            }
            rs.close();
            ps.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return consultations;
    }
}
