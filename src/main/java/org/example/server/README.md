# Documentation du Serveur

Ce dossier contient le cœur de l'application serveur (MRPS - Medical Report Protocol Secure).

## Fichiers

### `MedicalReportServer.java`
**Rôle :** Point d'entrée principal du serveur.
**Description :** 
- Initialise le serveur sur un port spécifique.
- Gère un pool de threads (`ExecutorService`) pour accepter plusieurs clients simultanément.
- Écoute les connexions entrantes et délègue leur traitement à `ClientHandler`.

### `ClientHandler.java`
**Rôle :** Gestionnaire de session client.
**Description :**
- Exécuté dans un thread séparé pour chaque client connecté.
- Implémente le protocole de communication MRPS.
- Gère l'authentification sécurisée (Challenge-Response).
- Traite les requêtes (ajout, modification, lecture de rapports).
- Assure le chiffrement/déchiffrement des échanges et la vérification des signatures.
