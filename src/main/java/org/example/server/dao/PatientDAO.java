package org.example.server.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.example.server.entity.Patient;
import org.example.server.searchvm.PatientSearchVM;

/**
 * DAO (Data Access Object) pour la gestion des entités {@link Patient}.
 * Permet de lister et rechercher des patients.
 */
public class PatientDAO {

    private Connection connexion;

    public PatientDAO(Connection connexion) {
        this.connexion = connexion;
    }

    /**
     * Liste tous les patients qui ont au moins une consultation enregistrée.
     * Utilise une requête DISTINCT pour éviter les doublons.
     *
     * @return Une liste de patients ayant des consultations
     * @throws SQLException En cas d'erreur d'accès à la base de données
     */
    public List<Patient> listerPatientsAvecConsultations() throws SQLException {
        String sql = "SELECT DISTINCT p.id, p.first_name, p.last_name, p.birth_date " +
                "FROM patient p " +
                "INNER JOIN consultations c ON p.id = c.patient_id " +
                "ORDER BY p.last_name, p.first_name";
        List<Patient> patients = new ArrayList<>();
        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                patients.add(new Patient(
                        rs.getInt("id"),
                        rs.getString("last_name"),
                        rs.getString("first_name"),
                        rs.getString("birth_date")));
            }
        }
        return patients;
    }

    /**
     * Charge tous les patients sans filtre.
     *
     * @return Une liste contenant tous les patients
     */
    public ArrayList<Patient> load() {
        return load(null);
    }

    /**
     * Recherche des patients en fonction de critères dynamiques.
     * Si un ID de médecin est fourni dans le SearchVM, ne retourne que les patients
     * ayant eu une consultation avec ce médecin.
     *
     * @param psearchvm L'objet contenant les critères de recherche (nom, prénom,
     *                  dates, ID médecin)
     * @return Une liste de patients correspondant aux critères
     */
    public ArrayList<Patient> load(PatientSearchVM psearchvm) {
        ArrayList<Patient> patients = new ArrayList<>();
        try {
            // On utilise DISTINCT car un patient peut avoir plusieurs consultations avec le
            // même médecin
            String query = "SELECT DISTINCT p.* FROM patient p ";

            // Si on filtre par médecin, on doit faire une jointure
            if (psearchvm != null && psearchvm.getDoctorId() != null) {
                query += "INNER JOIN consultations c ON p.id = c.patient_id ";
            }

            query += "WHERE 1=1 ";

            if (psearchvm != null) {
                if (psearchvm.getDoctorId() != null) {
                    query += "AND c.doctor_id = ? ";
                }
                if (psearchvm.getLastName() != null && !psearchvm.getLastName().isEmpty()) {
                    query += "AND p.last_name LIKE ? ";
                }
                if (psearchvm.getFirstName() != null && !psearchvm.getFirstName().isEmpty()) {
                    query += "AND p.first_name LIKE ? ";
                }
                if (psearchvm.getBirthDateFrom() != null && !psearchvm.getBirthDateFrom().isEmpty()) {
                    query += "AND p.birth_date >= ? ";
                }
                if (psearchvm.getBirthDateTo() != null && !psearchvm.getBirthDateTo().isEmpty()) {
                    query += "AND p.birth_date <= ? ";
                }
            }

            query += " ORDER BY p.last_name, p.first_name";

            PreparedStatement ps = connexion.prepareStatement(query);

            if (psearchvm != null) {
                int index = 1;
                if (psearchvm.getDoctorId() != null) {
                    ps.setInt(index++, psearchvm.getDoctorId());
                }
                if (psearchvm.getLastName() != null && !psearchvm.getLastName().isEmpty()) {
                    ps.setString(index++, "%" + psearchvm.getLastName() + "%");
                }
                if (psearchvm.getFirstName() != null && !psearchvm.getFirstName().isEmpty()) {
                    ps.setString(index++, "%" + psearchvm.getFirstName() + "%");
                }
                if (psearchvm.getBirthDateFrom() != null && !psearchvm.getBirthDateFrom().isEmpty()) {
                    ps.setString(index++, psearchvm.getBirthDateFrom());
                }
                if (psearchvm.getBirthDateTo() != null && !psearchvm.getBirthDateTo().isEmpty()) {
                    ps.setString(index++, psearchvm.getBirthDateTo());
                }
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Patient patient = new Patient();
                patient.setId(rs.getInt("id"));
                patient.setLast_name(rs.getString("last_name"));
                patient.setFirst_name(rs.getString("first_name"));
                patient.setBirth_date(rs.getString("birth_date"));

                patients.add(patient);
            }
            rs.close();
            ps.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return patients;
    }
}
