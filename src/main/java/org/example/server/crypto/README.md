# Documentation de la Cryptographie (Serveur)

Ce dossier contient les utilitaires cryptographiques utilisés par le serveur pour sécuriser l'application.

## Fichiers

### `GestionnaireCrypto.java`
**Rôle :** Bibliothèque de fonctions cryptographiques.
**Description :**
- Utilise Bouncy Castle comme fournisseur de sécurité.
- **AES** : Chiffrement symétrique des données et des clés de session.
- **RSA** : Chiffrement asymétrique pour l'échange de clés et signatures numériques.
- **SHA-256** : Hachage pour les mots de passe et l'intégrité.
- **HMAC** : Code d'authentification de message pour garantir l'intégrité des échanges.
- **Signatures** : Création et vérification de signatures numériques.

### `GenerateurCles.java`
**Rôle :** Utilitaire de génération de clés (PKI).
**Description :**
- Script utilitaire à exécuter une fois pour initialiser l'environnement.
- Génère les paires de clés RSA (Publique/Privée) pour le serveur et le client.
- Sauvegarde les clés dans le dossier `src/main/resources/keys/`.
