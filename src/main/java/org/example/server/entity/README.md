# Documentation des Entités

Ce dossier contient les classes POJO (Plain Old Java Objects) représentant les tables de la base de données.

## Fichiers

### `Doctor.java`
**Rôle :** Représente un médecin.
**Attributs :** ID, nom, prénom, spécialité, etc.

### `Patient.java`
**Rôle :** Représente un patient.
**Attributs :** ID, nom, prénom, date de naissance, etc.

### `Consultation.java`
**Rôle :** Représente une consultation médicale.
**Attributs :** ID, date, motif, ID médecin, ID patient.

### `Report.java`
**Rôle :** Représente un rapport médical.
**Attributs :** ID, date, contenu textuel, ID médecin, ID patient.
