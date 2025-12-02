# Documentation de la Cryptographie (Client)

Ce dossier contient la logique cryptographique spécifique au client.

## Fichiers

### `GestionnaireCryptoClient.java`
**Rôle :** Gestionnaire de sécurité côté client.
**Description :**
- Charge les clés RSA (Privée Client, Publique Serveur) au démarrage.
- **Authentification** : Calcule le digest salé pour le login sécurisé.
- **Session** : Génère et chiffre la clé de session AES pour l'envoyer au serveur.
- **Échanges** : Chiffre et déchiffre les messages avec la clé de session AES.
- **Signature** : Signe les données envoyées (ex: nouveaux rapports) pour garantir leur authenticité.
- **Intégrité** : Vérifie les HMAC des données reçues.
