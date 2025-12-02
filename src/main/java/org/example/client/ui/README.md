# Documentation de l'Interface Utilisateur (Client)

Ce dossier contient l'interface graphique (GUI) de l'application cliente, réalisée avec Swing.

## Fichiers

### `FenetreLogin.java`
**Rôle :** Point d'entrée de l'application.
**Description :** Fenêtre de connexion sécurisée. Gère l'authentification initiale auprès du serveur.

### `FenetrePrincipale.java`
**Rôle :** Conteneur principal.
**Description :** Fenêtre principale contenant les onglets de navigation une fois l'utilisateur connecté.

### `PanelAjoutRapport.java`
**Rôle :** Formulaire de création.
**Description :** Permet de rédiger un nouveau rapport. Chiffre et signe les données avant envoi.

### `PanelModificationRapport.java`
**Rôle :** Formulaire d'édition.
**Description :** Permet de modifier un rapport existant. Charge les données, permet l'édition et envoie les mises à jour chiffrées.

### `PanelListeRapports.java`
**Rôle :** Consultation des rapports.
**Description :** Affiche la liste des rapports médicaux avec filtrage par patient. Déchiffre et affiche le contenu sécurisé.

### `PanelListePatients.java`
**Rôle :** Consultation des patients.
**Description :** Affiche la liste des patients associés au médecin connecté.
