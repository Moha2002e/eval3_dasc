# Documentation des DAO (Data Access Objects)

Ce dossier contient les classes implémentant le pattern DAO pour abstraire l'accès aux tables de la base de données.

## Fichiers

### `DoctorDAO.java`
**Rôle :** Gestion des médecins.
**Description :**
- Vérification de l'existence et récupération des informations de connexion (hash mot de passe).
- Recherche de médecins par critères.

### `PatientDAO.java`
**Rôle :** Gestion des patients.
**Description :**
- Listage des patients.
- Recherche avancée de patients.
- Gestion des liens avec les consultations.

### `ConsultationDAO.java`
**Rôle :** Gestion des consultations.
**Description :**
- Vérification des droits d'accès (un médecin ne peut voir que les patients qu'il a consultés).
- Recherche de consultations.

### `ReportDAO.java`
**Rôle :** Gestion des rapports médicaux.
**Description :**
- CRUD (Create, Read, Update, Delete) pour les rapports.
- Filtrage des rapports par médecin et patient.
