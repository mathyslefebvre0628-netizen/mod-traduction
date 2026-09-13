# LinguaBridge

Mod Fabric client-side de traduction automatique du chat Minecraft.

> Parle ta langue. Joue avec tout le monde.

## Cible

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.3+
- Fabric API 0.156.0+26.2
- Java 25

## Fonctionnement

LinguaBridge intercepte le chat côté client et utilise un fournisseur de traduction configurable. La traduction est asynchrone afin de ne pas bloquer le thread Minecraft.

Fonctions déjà présentes dans la branche `dev-26.2` :

- traduction sortante français → anglais par défaut ;
- interception de la réception du chat ;
- traduction entrante vers le français ;
- désactivation automatique des commandes commençant par `/` ;
- cache local des traductions ;
- limitation du nombre de requêtes simultanées ;
- touche F8 pour activer/désactiver le mod ;
- configuration locale `config/linguabridge.json` ;
- fournisseur LibreTranslate abstrait derrière `TranslationProvider`.

## LibreTranslate

Configurer `apiUrl` et éventuellement `apiKey` dans `config/linguabridge.json`.

Aucune clé API n'est intégrée au mod.

## Limites actuelles

La première implémentation 26.2 affiche les traductions entrantes comme des messages système client. Elle ne prétend donc pas conserver intégralement les métadonnées de chat signées ou les Chat Heads. Une couche de rendu dédiée devra être ajoutée pour remplacer uniquement le texte tout en conservant l'identité et les métadonnées du message original.

La signature cryptographique du chat ne peut pas être modifiée côté client. Les messages sortants sont renvoyés via l'API de chat du client après traduction ; le comportement exact dépend des mécanismes de chat signé du serveur.

## Compilation

Le projet utilise Fabric Loom 1.17.17 et le toolchain Minecraft 26.2. Avec Gradle 9.x et Java 25 :

```text
gradle build
```

Le JAR est produit dans `build/libs/`.

## Branche de développement

Le développement Minecraft 26.2 est effectué sur `dev-26.2`. La branche `main` conserve le README du dépôt et sera mise à jour après validation du projet.
