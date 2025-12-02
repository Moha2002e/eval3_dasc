# 📘 MODE D'EMPLOI - Projet MRPS (Medical Report Protocol Secure)

Ce guide rapide vous explique comment installer, configurer et lancer le projet étape par étape.

---

## ⚠️ Étape 1 : Préparation de la Base de Données (IMPORTANT)

Avant de lancer le serveur, vous devez vous assurer que la base de données est prête et contient la table `reports`.

1.  Assurez-vous que votre serveur MySQL est lancé.
2.  Exécutez le script SQL fourni pour créer la table des rapports :
    *   **Fichier :** `src/main/resources/db/init_reports_table.sql`
    *   **Commande :**
        ```bash
        mysql -u Student -p PourStudent < src/main/resources/db/init_reports_table.sql
        ```
    *(Adaptez les identifiants si nécessaire)*.

---

## 🔑 Étape 2 : Génération des Clés de Sécurité

Le système utilise la cryptographie RSA. Vous devez générer les clés avant le premier lancement.

1.  Compilez et exécutez le générateur de clés :
    ```bash
    # Si vous utilisez Maven
    mvn exec:java -Dexec.mainClass="org.example.server.crypto.GenerateurCles"
    ```
    *Ou manuellement :*
    ```bash
    javac -cp "..." src/main/java/org/example/server/crypto/GenerateurCles.java
    java -cp "..." org.example.server.crypto.GenerateurCles
    ```

✅ **Vérification :** Assurez-vous que 4 fichiers `.key` sont apparus dans `src/main/resources/keys/`.

---

## 🚀 Étape 3 : Démarrer le Serveur

Le serveur doit être lancé en premier pour accepter les connexions.

1.  Lancez la classe principale du serveur :
    *   **Classe :** `org.example.server.MedicalReportServer`
    *   **Commande Maven :**
        ```bash
        mvn exec:java -Dexec.mainClass="org.example.server.MedicalReportServer"
        ```

✅ **Succès :** Vous devriez voir le message : `🚀 Serveur MRPS démarré sur le port 5000`.

---

## 💻 Étape 4 : Démarrer le Client

Une fois le serveur prêt, vous pouvez lancer l'application cliente (interface graphique).

1.  Lancez la fenêtre de connexion :
    *   **Classe :** `org.example.client.ui.FenetreLogin`
    *   **Commande Maven :**
        ```bash
        mvn exec:java -Dexec.mainClass="org.example.client.ui.FenetreLogin"
        ```

2.  **Connectez-vous :**
    *   **Hôte :** `localhost`
    *   **Port :** `5000`
    *   **Login :** `prenom.nom` (ex: un médecin existant dans votre BD)
    *   **Mot de passe :** Le mot de passe du médecin.

---

## 📝 Résumé des Commandes (Maven)

```bash
# 1. Générer les clés (une seule fois)
mvn exec:java -Dexec.mainClass="org.example.server.crypto.GenerateurCles"

# 2. Lancer le serveur (dans un terminal)
mvn exec:java -Dexec.mainClass="org.example.server.MedicalReportServer"

# 3. Lancer le client (dans un autre terminal)
mvn exec:java -Dexec.mainClass="org.example.client.ui.FenetreLogin"
```
