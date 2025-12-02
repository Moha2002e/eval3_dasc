# Documentation de la Gestion de Base de Données

Ce dossier contient les classes responsables de la gestion centrale de la base de données.

## Fichiers

### `BdManager.java`
**Rôle :** Façade d'accès aux données.
**Description :**
- Gère la connexion JDBC à la base de données (MySQL/MariaDB).
- Charge la configuration depuis `config.properties`.
- Instancie et centralise l'accès aux différents DAO (`DoctorDAO`, `PatientDAO`, etc.).
- Fournit des méthodes de haut niveau pour les opérations métier (ex: `ajouterRapport`, `medecinExiste`).
- Simplifie l'utilisation de la base de données pour le reste du serveur.
