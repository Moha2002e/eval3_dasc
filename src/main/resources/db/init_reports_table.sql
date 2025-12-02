-- ============================================================================
-- Script d'initialisation de la table reports
-- Serveur Rapport Médical Sécurisé (MRPS)
-- ============================================================================

-- Création de la table reports
CREATE TABLE IF NOT EXISTS reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    doctor_id INT NOT NULL COMMENT 'ID du médecin ayant créé le rapport',
    patient_id INT NOT NULL COMMENT 'ID du patient concerné par le rapport',
    date_rapport DATE NOT NULL COMMENT 'Date du rapport médical',
    texte_rapport TEXT NOT NULL COMMENT 'Contenu du rapport médical',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Date de création',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Date de dernière modification',
    
    -- Clés étrangères
    FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE,
    
    -- Index pour optimiser les requêtes
    INDEX idx_doctor (doctor_id),
    INDEX idx_patient (patient_id),
    INDEX idx_date (date_rapport),
    INDEX idx_doctor_patient (doctor_id, patient_id),
    INDEX idx_doctor_date (doctor_id, date_rapport)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Table des rapports médicaux';

-- ============================================================================
-- Vérification de l'intégrité
-- ============================================================================
-- Cette contrainte assure qu'un médecin ne peut créer un rapport que pour
-- un patient avec lequel il a eu au moins une consultation
-- Note: Cette vérification sera faite au niveau applicatif dans le serveur Java
