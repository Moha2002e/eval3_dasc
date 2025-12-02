# Documentation Partagée

Ce dossier contient les éléments communs au client et au serveur.

## Fichiers

### `Protocol.java`
**Rôle :** Définition du protocole de communication.
**Description :**
- Contient les constantes partagées définissant le langage commun entre client et serveur.
- **Commandes** : `LOGIN`, `LOGOUT`, `ADD_REPORT`, `EDIT_REPORT`, `LIST_REPORTS`, `LIST_PATIENTS`.
- **Réponses** : `OK`, `ERROR`, `SALT`.
- **Configuration** : Taille du sel, séparateurs, etc.
