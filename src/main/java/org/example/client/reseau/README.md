# Documentation Réseau (Client)

Ce dossier gère la communication réseau bas niveau côté client.

## Fichiers

### `GestionnaireConnexion.java`
**Rôle :** Gestionnaire de socket TCP.
**Description :**
- Établit la connexion TCP avec le serveur.
- Gère les flux d'entrée (`BufferedReader`) et de sortie (`PrintWriter`).
- Fournit des méthodes simples `envoyerRequete` et `recevoirReponse` pour abstraire la communication réseau.
- Gère la déconnexion propre.
