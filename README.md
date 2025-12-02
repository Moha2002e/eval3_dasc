# Guide d'Utilisation - Serveur Rapport Médical Sécurisé (MRPS)

## 📋 Table des Matières

1. [Prérequis](#prérequis)
2. [Installation](#installation)
3. [Configuration](#configuration)
4. [Démarrage](#démarrage)
5. [Utilisation de l'Application Cliente](#utilisation-de-lapplication-cliente)
6. [Protocole MRPS](#protocole-mrps)
7. [Dépannage](#dépannage)

---

## 🔧 Prérequis

### Logiciels Requis
- **Java JDK 17** ou supérieur
- **MySQL 8.0** ou supérieur
- **Maven** (optionnel, pour gestion dépendances)
- **Bouncy Castle** (inclus dans pom.xml)

### Base de Données
- Serveur MySQL accessible
- Base de données `PourStudent` créée
- Tables: `specialties`, `patient`, `doctor`, `consultations`, `reports`

---

## 📦 Installation

### 1. Cloner/Télécharger le Projet

```bash
cd eval_3
```

### 2. Initialiser la Base de Données

**Option A: Script SQL**
```bash
mysql -h 192.168.0.15 -u Student -p PourStudent < src/main/resources/db/init_reports_table.sql
```

**Option B: Programme C**
```bash
cd src/main/resources/db
gcc -o init_database init_database.c -lmysqlclient
./init_database
```

### 3. Générer les Clés RSA

```bash
# Compiler le générateur
javac -d target/classes -cp "target/classes;C:\Users\moha4\.m2\repository\org\bouncycastle\bcprov-jdk18on\1.78.1\bcprov-jdk18on-1.78.1.jar" src/main/java/org/example/server/crypto/GenerateurCles.java

# Exécuter
java -cp "target/classes;C:\Users\moha4\.m2\repository\org\bouncycastle\bcprov-jdk18on\1.78.1\bcprov-jdk18on-1.78.1.jar" org.example.server.crypto.GenerateurCles
```

Cela créera 4 fichiers dans `src/main/resources/keys/`:
- `serveur_public.key`
- `serveur_prive.key`
- `client_public.key`
- `client_prive.key`

---

## ⚙️ Configuration

### Fichier `config.properties`

Éditer `src/main/resources/config.properties`:

```properties
# Configuration Serveur
PORT_REPORT_SECURE=5000
THREAD_POOL_SIZE=10
SERVER_HOST=localhost

# Configuration Base de Données
DB_URL=jdbc:mysql://192.168.0.15:3306/PourStudent
DB_USER=Student
DB_PASSWORD=PourStudent1_
```

**Paramètres:**
- `PORT_REPORT_SECURE`: Port d'écoute du serveur (défaut: 5000)
- `THREAD_POOL_SIZE`: Nombre de threads dans le pool (défaut: 10)
- `DB_URL`: URL de connexion MySQL
- `DB_USER`: Utilisateur MySQL
- `DB_PASSWORD`: Mot de passe MySQL

---

## 🚀 Démarrage

### 1. Compiler le Projet

**Avec Maven:**
```bash
mvn clean compile
```

**Sans Maven (javac):**
```bash
javac -d target/classes -cp "lib/*" -sourcepath src/main/java src/main/java/org/example/server/MedicalReportServer.java
```

### 2. Démarrer le Serveur

```bash
java -cp "target/classes;lib/*" org.example.server.MedicalReportServer
```

**Sortie attendue:**
```
Démarrage du Serveur Rapport Médical...
Port: 5000
Taille du pool de threads: 10
Serveur en écoute sur le port 5000
```

### 3. Lancer l'Application Cliente

```bash
java -cp "target/classes;lib/*" org.example.client.ui.FenetreLogin
```

---

## 💻 Utilisation de l'Application Cliente

### Étape 1: Connexion

![Interface de connexion](docs/login_interface.png)

1. **Configuration Serveur**
   - Hôte: `localhost` (ou IP du serveur)
   - Port: `5000`

2. **Identifiants Médecin**
   - Login: Format `prenom.nom` (ex: `jean.dupont`)
   - Mot de passe: Mot de passe du médecin en BD

3. Cliquer sur **"Se Connecter"**

**Processus d'authentification:**
- Envoi du login au serveur
- Réception du sel aléatoire
- Calcul du digest salé: `SHA-256(login + password + sel)`
- Génération clé de session AES-256
- Chiffrement clé avec RSA-2048
- Envoi digest + clé chiffrée
- Authentification réussie ✓

### Étape 2: Fenêtre Principale

Après connexion, 3 onglets disponibles:

#### 📝 Onglet "Ajouter un Rapport"

1. **Remplir le formulaire:**
   - ID Patient: Numéro du patient
   - Date: Format `YYYY-MM-DD` (ex: `2024-12-02`)
   - Contenu: Texte du rapport médical

2. **Cliquer sur "📝 Enregistrer le Rapport"**

**Processus:**
- Chiffrement AES des données (date, patient_id, texte)
- Signature RSA des données
- Envoi au serveur
- Vérification relation médecin-patient
- Insertion en BD
- Confirmation avec ID du rapport

#### ✏️ Onglet "Modifier un Rapport"

1. **Entrer l'ID du rapport** à modifier
2. **Saisir le nouveau contenu** dans la zone de texte
3. **Cliquer sur "✏️ Modifier le Rapport"**

**Processus:**
- Chiffrement AES des données
- Envoi au serveur
- Vérification propriété (seul le médecin auteur peut modifier)
- Mise à jour en BD

#### 📋 Onglet "Liste des Rapports"

**Affichage de tous les rapports:**
1. Cliquer sur **"📋 Tous mes Rapports"**

**Filtrage par patient:**
1. Entrer l'ID du patient
2. Cliquer sur **"🔍 Charger"**

**Tableau (JTable):**
- Colonnes: ID, Patient ID, Date, Aperçu
- Cliquer sur une ligne pour voir le rapport complet

**Zone de texte (JTextArea):**
- Affiche le contenu complet du rapport sélectionné

**Processus:**
- Envoi requête LIST_REPORTS
- Réception données chiffrées + HMAC
- Vérification HMAC (intégrité)
- Déchiffrement AES
- Parsing JSON
- Affichage dans JTable

### Étape 3: Déconnexion

Cliquer sur **"Déconnexion"** en bas de la fenêtre.

---

## 📡 Protocole MRPS

### Commandes Disponibles

#### 1. LOGIN
```
Client → Serveur: LOGIN|<login>
Serveur → Client: SALT|<sel_base64>
Client → Serveur: DIGEST|<digest_base64>|<cle_session_chiffree_RSA>
Serveur → Client: OK|<session_id>
```

#### 2. ADD_REPORT
```
Client → Serveur: ADD_REPORT|<date_chiffree>|<patient_id_chiffre>|<texte_chiffre>|<signature>
Serveur → Client: OK|<report_id>
```

#### 3. EDIT_REPORT
```
Client → Serveur: EDIT_REPORT|<report_id_chiffre>|<texte_chiffre>
Serveur → Client: OK
```

#### 4. LIST_REPORTS
```
Client → Serveur: LIST_REPORTS|<patient_id_chiffre_optionnel>
Serveur → Client: OK|<count>|<rapports_json_chiffres>|<hmac>
```

#### 5. LOGOUT
```
Client → Serveur: LOGOUT
Serveur → Client: OK
```

---

## 🔐 Sécurité

### Cryptographie Utilisée

| Opération | Algorithme | Taille Clé |
|-----------|------------|------------|
| Chiffrement symétrique | AES | 256 bits |
| Chiffrement asymétrique | RSA | 2048 bits |
| Hash | SHA-256 | 256 bits |
| HMAC | HMAC-SHA256 | 256 bits |
| Signature | SHA256withRSA | 2048 bits |

### Flux de Sécurité

1. **Authentification**: Digest salé empêche rejeu
2. **Échange de clés**: RSA pour transmettre clé AES
3. **Chiffrement données**: AES pour performance
4. **Intégrité**: HMAC vérifie non-modification
5. **Authenticité**: Signature RSA prouve origine

---

## 🐛 Dépannage

### Erreur: "Impossible de se connecter au serveur"

**Causes possibles:**
- Serveur non démarré
- Port incorrect
- Firewall bloque la connexion

**Solutions:**
1. Vérifier que le serveur est démarré
2. Vérifier le port dans `config.properties`
3. Tester avec `telnet localhost 5000`

### Erreur: "Authentification échouée"

**Causes possibles:**
- Login incorrect
- Mot de passe incorrect
- Médecin n'existe pas en BD

**Solutions:**
1. Vérifier le login (format: `prenom.nom`)
2. Vérifier le mot de passe en BD
3. Vérifier table `doctor`

### Erreur: "Aucune consultation trouvée avec ce patient"

**Cause:**
Le médecin n'a pas de consultation avec ce patient.

**Solution:**
Ajouter une consultation dans la table `consultations` avant de créer un rapport.

### Erreur: "HMAC invalide - données corrompues"

**Causes possibles:**
- Clé de session différente
- Données modifiées en transit
- Problème réseau

**Solutions:**
1. Se déconnecter et se reconnecter
2. Vérifier la connexion réseau
3. Redémarrer le serveur

### Erreur: "Clés RSA non trouvées"

**Cause:**
Les fichiers de clés n'existent pas.

**Solution:**
Exécuter `GenerateurCles` pour créer les clés:
```bash
java -cp "target/classes;lib/*" org.example.server.crypto.GenerateurCles
```

---

## 📚 Structure du Projet

```
eval_3/
├── src/main/
│   ├── java/org/example/
│   │   ├── server/
│   │   │   ├── MedicalReportServer.java
│   │   │   ├── ClientHandler.java
│   │   │   ├── bd/BdManager.java
│   │   │   └── crypto/
│   │   │       ├── GestionnaireCrypto.java
│   │   │       ├── GenerateurCles.java
│   │   │       └── TestCrypto.java
│   │   ├── client/
│   │   │   ├── ui/
│   │   │   │   ├── FenetreLogin.java
│   │   │   │   ├── FenetrePrincipale.java
│   │   │   │   ├── PanelAjoutRapport.java
│   │   │   │   ├── PanelModificationRapport.java
│   │   │   │   └── PanelListeRapports.java
│   │   │   ├── crypto/GestionnaireCryptoClient.java
│   │   │   └── reseau/GestionnaireConnexion.java
│   │   └── shared/Protocol.java
│   └── resources/
│       ├── config.properties
│       ├── db/
│       │   ├── init_database.c
│       │   └── init_reports_table.sql
│       └── keys/
│           ├── serveur_public.key
│           ├── serveur_prive.key
│           ├── client_public.key
│           └── client_prive.key
└── pom.xml
```

---

## 📞 Support

Pour toute question ou problème:
1. Consulter les logs du serveur
2. Vérifier la configuration
3. Tester avec `TestCrypto.java`

---

## 📝 Notes Importantes

- **Mots de passe**: Stockés hashés en BD (ne jamais transmettre en clair)
- **Clés RSA**: À protéger (ne pas partager les clés privées)
- **Relation médecin-patient**: Obligatoire pour créer un rapport
- **Multi-threading**: Le serveur gère plusieurs clients simultanément
- **Connexions persistantes**: Modèle "à la demande" (non requête unique)

---

**Version:** 1.0  
**Date:** Décembre 2024  
**Auteur:** Projet MRPS - Architecture Client/Serveur & Cryptographie
