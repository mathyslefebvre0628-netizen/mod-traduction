# LinguaBridge

**Automatic bidirectional Minecraft chat translation, directly from your client.**

> **Slogan:** Speak your language. Play together.

LinguaBridge is a client-side Fabric mod for Minecraft 26.2. It translates incoming chat to your configured display language and translates your outgoing chat before it is sent to the server. Other players do not need LinguaBridge and no server-side installation is required.

## Status

The `dev-26.2` branch is the active development branch for Minecraft 26.2. The project uses official Mojang mappings, Fabric Loader 0.19.3, Fabric API 0.156.0+26.2 and Java 25.

## Features

- Incoming chat translation.
- Outgoing chat translation before sending.
- French, English, Spanish, German, Italian, Portuguese, Dutch, Polish, Russian, Japanese, Korean and Chinese language choices, plus Auto for writing language.
- F8 toggle, configurable through Minecraft Controls.
- LibreTranslate provider with configurable URL, API key and timeout.
- Asynchronous HTTP requests; the Minecraft render thread is not used for network calls.
- Shared translation executor and bounded LRU cache.
- Concurrent request limit.
- Trivial messages such as `gg`, `lol`, `ok` and `XD` are skipped.
- Commands beginning with `/` are never automatically translated.
- Optional Mod Menu configuration screen.
- Optional Chat Heads compatibility metadata.

## Installation

1. Install Minecraft Java Edition 26.2.
2. Install Java 25.
3. Install Fabric Loader 0.19.3 or a compatible 26.2 release.
4. Install a compatible Fabric API release.
5. Put the LinguaBridge JAR in `.minecraft/mods`.
6. Optionally install Mod Menu 20.0.1 or newer compatible with 26.2.
7. Optionally install Chat Heads.
8. Start Minecraft.
9. Open Mod Menu and select **Configure** for LinguaBridge.

## Configuration

The configuration file is:

`config/linguabridge.json`

Example:

```json
{
  "enabled": true,
  "outgoingLanguage": "fr",
  "incomingLanguage": "fr",
  "autoDetectIncoming": true,
  "showTranslationIndicator": true,
  "provider": "libretranslate",
  "apiUrl": "https://libretranslate.com",
  "apiKey": "",
  "timeoutMs": 4500,
  "maxConcurrentRequests": 2,
  "cacheSize": 256
}
```

The API key is stored locally in the configuration file. It is never hard-coded into the mod.

## LibreTranslate

LinguaBridge sends translation requests to the configured LibreTranslate server. Public instances can require an API key; a self-hosted LibreTranslate instance can be used instead.

The request is asynchronous and uses the `/translate` endpoint. When source language is `auto`, LibreTranslate can return the detected source language.

## Chat and commands

Outgoing messages beginning with `/` are passed through unchanged. This protects commands such as `/gamemode`, `/tp`, `/msg`, `/tell`, `/w` and `/tpa`.

Private-message and group-chat formats vary by server. The architecture intentionally does not rewrite command syntax.

## Chat Heads and formatting limitation

LinguaBridge keeps the sender information supplied by Fabric's receive-chat event and includes the sender name in the translated display when necessary. However, the asynchronous replacement currently uses Minecraft's client chat listener to display the translated component after the original signed message has been cancelled.

Because this replacement is a new visual component, it cannot guarantee preservation of every original `ChatType`, hover/click event, signed-message metadata, or Chat Heads rendering path. The mod does not modify or forge cryptographic chat signatures.

This is a deliberate limitation rather than a fake compatibility claim. A future dedicated mixin can target the exact 26.2 chat rendering path if a stable way to replace only the visible text while retaining the original message metadata is established.

## Signed chat

Incoming messages are treated as a client-side visual transformation. LinguaBridge does not attempt to alter a server-issued cryptographic signature.

For outgoing chat, Fabric's client send event is intercepted before the original text is sent. The translated text is then submitted as a new chat message through the normal client connection. This means the server receives the translated text, but LinguaBridge cannot retroactively change the signature of the original French text because that original text is never submitted.

Server-specific restrictions may still affect signed-chat behavior.

## Privacy

Messages that require translation may be sent to the translation provider configured by the user. Do not use a provider you do not trust for private conversations. LinguaBridge does not intentionally log API keys, private conversations or unnecessary message content.

The translation system can be disabled completely with the Translation option or F8.

## Performance and reliability

Network requests are asynchronous. A shared executor, request semaphore and LRU cache prevent one thread from being created for every chat message and reduce repeated API calls. HTTP errors, invalid JSON and unavailable servers are surfaced as failed translations rather than crashing Minecraft.

## Development

Requirements:

- Java 25
- Gradle 9.x
- Minecraft 26.2
- Fabric Loader 0.19.3
- Fabric API 0.156.0+26.2
- Fabric Loom 1.17.17

Build with:

```bash
./gradlew build
```

On Windows:

```bat
gradlew.bat build
```

The resulting JAR is placed in `build/libs/`.

## Project structure

```text
src/main/java/fr/mathyslefebvre/linguabridge/
├── LinguaBridgeClient.java
├── config/ModConfig.java
├── gui/TranslatorConfigScreen.java
├── modmenu/ModMenuIntegration.java
└── translation/
    ├── LibreTranslateProvider.java
    ├── TranslationManager.java
    ├── TranslationProvider.java
    └── TranslationResult.java
```

## Logo

The GitHub connector used to maintain this repository accepts UTF-8 source files but cannot upload a binary PNG directly. A precise logo-generation prompt is kept in `docs/logo-prompt.md`. The final 128x128 transparent PNG should be installed at:

`src/main/resources/assets/linguabridge/icon.png`

## License

MIT. See `LICENSE`.
