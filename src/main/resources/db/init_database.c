/*
 * Script d'initialisation complète de la base de données
 * Serveur Rapport Médical Sécurisé (MRPS)
 */

#include <mysql/mysql.h>
#include <stdio.h>
#include <stdlib.h>

void finish_with_error(MYSQL *connexion) {
    fprintf(stderr, "Erreur MySQL: %s\n", mysql_error(connexion));
    mysql_close(connexion);
    exit(1);
}

int main() {
    MYSQL *connexion = mysql_init(NULL);
    
    if (connexion == NULL) {
        fprintf(stderr, "mysql_init() a échoué\n");
        exit(1);
    }
    
    // Connexion à MySQL
    if (mysql_real_connect(connexion, "192.168.0.15", "Student", "PourStudent1_",
                          "PourStudent", 3306, NULL, 0) == NULL) {
        finish_with_error(connexion);
    }
    
    printf("Connexion à la base de données réussie.\n");
    
    // Création de la table specialties
    if (mysql_query(connexion, "CREATE TABLE IF NOT EXISTS specialties ("
                               "id INT AUTO_INCREMENT PRIMARY KEY, "
                               "name VARCHAR(100) NOT NULL UNIQUE COMMENT 'Nom de la spécialité médicale', "
                               "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Date de création', "
                               "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Date de dernière modification'"
                               ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci "
                               "COMMENT='Table des spécialités médicales';"))
        finish_with_error(connexion);
    
    printf("Table 'specialties' créée avec succès.\n");
    
    // Création de la table patient
    if (mysql_query(connexion, "CREATE TABLE IF NOT EXISTS patient ("
                               "id INT AUTO_INCREMENT PRIMARY KEY, "
                               "first_name VARCHAR(100) NOT NULL COMMENT 'Prénom du patient', "
                               "last_name VARCHAR(100) NOT NULL COMMENT 'Nom de famille du patient', "
                               "birth_date DATE COMMENT 'Date de naissance du patient', "
                               "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Date de création', "
                               "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Date de dernière modification', "
                               "INDEX idx_last_name (last_name), "
                               "INDEX idx_birth_date (birth_date), "
                               "INDEX idx_full_name (first_name, last_name)"
                               ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci "
                               "COMMENT='Table des patients';"))
        finish_with_error(connexion);
    
    printf("Table 'patient' créée avec succès.\n");
    
    // Création de la table doctor
    if (mysql_query(connexion, "CREATE TABLE IF NOT EXISTS doctor ("
                               "id INT AUTO_INCREMENT PRIMARY KEY, "
                               "first_name VARCHAR(100) NOT NULL COMMENT 'Prénom du médecin', "
                               "last_name VARCHAR(100) NOT NULL COMMENT 'Nom de famille du médecin', "
                               "password VARCHAR(255) NOT NULL COMMENT 'Mot de passe du médecin (hashé)', "
                               "specialite_id INT COMMENT 'ID de la spécialité du médecin', "
                               "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Date de création', "
                               "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Date de dernière modification', "
                               "FOREIGN KEY (specialite_id) REFERENCES specialties(id) ON DELETE SET NULL, "
                               "INDEX idx_last_name (last_name), "
                               "INDEX idx_specialite (specialite_id), "
                               "INDEX idx_full_name (first_name, last_name)"
                               ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci "
                               "COMMENT='Table des médecins';"))
        finish_with_error(connexion);
    
    printf("Table 'doctor' créée avec succès.\n");
    
    // Création de la table consultations
    if (mysql_query(connexion, "CREATE TABLE IF NOT EXISTS consultations ("
                               "id INT AUTO_INCREMENT PRIMARY KEY, "
                               "patient_id INT NULL COMMENT 'ID du patient (NULL = consultation libre)', "
                               "doctor_id INT NOT NULL COMMENT 'ID du médecin', "
                               "date DATE NOT NULL COMMENT 'Date de la consultation', "
                               "hour TIME COMMENT 'Heure de la consultation', "
                               "reason TEXT COMMENT 'Motif de la consultation', "
                               "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Date de création', "
                               "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Date de dernière modification', "
                               "FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE, "
                               "FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE CASCADE, "
                               "INDEX idx_date (date), "
                               "INDEX idx_patient (patient_id), "
                               "INDEX idx_doctor (doctor_id), "
                               "INDEX idx_doctor_date (doctor_id, date), "
                               "INDEX idx_patient_date (patient_id, date), "
                               "UNIQUE KEY unique_appointment (doctor_id, date, hour)"
                               ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci "
                               "COMMENT='Table des consultations médicales';"))
        finish_with_error(connexion);
    
    printf("Table 'consultations' créée avec succès.\n");
    
    // Création de la table reports (NOUVELLE)
    if (mysql_query(connexion, "CREATE TABLE IF NOT EXISTS reports ("
                               "id INT AUTO_INCREMENT PRIMARY KEY, "
                               "doctor_id INT NOT NULL COMMENT 'ID du médecin ayant créé le rapport', "
                               "patient_id INT NOT NULL COMMENT 'ID du patient concerné par le rapport', "
                               "date_rapport DATE NOT NULL COMMENT 'Date du rapport médical', "
                               "texte_rapport TEXT NOT NULL COMMENT 'Contenu du rapport médical', "
                               "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Date de création', "
                               "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Date de dernière modification', "
                               "FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE CASCADE, "
                               "FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE, "
                               "INDEX idx_doctor (doctor_id), "
                               "INDEX idx_patient (patient_id), "
                               "INDEX idx_date (date_rapport), "
                               "INDEX idx_doctor_patient (doctor_id, patient_id), "
                               "INDEX idx_doctor_date (doctor_id, date_rapport)"
                               ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci "
                               "COMMENT='Table des rapports médicaux';"))
        finish_with_error(connexion);
    
    printf("Table 'reports' créée avec succès.\n");
    
    printf("\n=== Initialisation de la base de données terminée avec succès ===\n");
    
    mysql_close(connexion);
    return 0;
}
