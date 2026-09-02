# GalaxyRP Launcher

![App Screenshot](https://i.imgur.com/HHCIjsE.png)

A launcher made for the game Jedi Knight: Jedi Academy. 

The goal of this project is to create a JKA launcher that provides players with an easy way to manage their mods and connect to online servers. The launcher allows players to quickly and easily browse and update mods and pk3 files from a shared google drive. It also provides a streamlined interface for players to connect to online servers, making it easier join multiplayer matches. The launcher includes features such as one-click updates for all pk3s and a user-friendly interface that makes it easy for players of all skill levels to use. This project aims to enhance the gaming experience for players by simplifying the modding process and making online play more accessible.

While this was made for [GalaxyRP](https://github.com/alexnita3/GalaxyRP), it will work with all JKA mods.


## Installation

Download the latest release and place the launcher in a location you can run it from. For a Java-based app, the easiest way is to use the generated executable or run the packaged JAR directly.

To configure the mod manager, go into the settings tab and paste a Google Drive folder link. For example:

```bash
https://drive.google.com/drive/folders/1YiIL-g-fKshPeerjYFyIBgFTfd5jaa3N
```

The folder must be shared with the Google account used by the OAuth credentials for this app, and users who need to share the same folder must have access to read and write files in that folder.


## Screenshots

Main Screen:

![App Screenshot](https://i.imgur.com/xJvFmjr.png)

Settings Screen:

![App Screenshot](https://i.imgur.com/UaZ5olN.png)


## Tech Stack

**IDE:** Intellij IDEA

**Framework:** JavaFX

**APIs:** [Google Drive API](https://developers.google.com/drive/api/guides/about-sdk)


## Privacy Policy

The only data I have access to about the users of this app are the number of api calls and the success rate of those calls.

I do **NOT** have access to: private email addresses used with this, contents of the downloads, any of the settings used, any passwords, IPs and configs.

I have never and will never use the information this app collects for any financial benefit, nor will I share it with anyone else, unless to help the development of this app.


## Getting started with development

Prerequisites:

- JDK 24
- IntelliJ IDEA or another Java IDE
- Git
- Maven wrapper included in the repo (`mvnw` / `mvnw.cmd`)
- Optional: WiX Toolset if you want to generate a Windows `.exe` installer

Setup:

1. Clone the repository.
2. Create your Google Drive OAuth client credentials in Google Cloud Console.
3. Copy the credentials file in the root of the project.

4. Build the project:

```powershell
cd GalaxyRP-Launcher
.\mvnw clean package
```

5. Run the packaged jar:

```powershell
java -jar .\target\GalaxyRPLauncher-1.0-SNAPSHOT.jar
```

6. If you want an executable installer, install WiX and run the same package command with the installer step enabled. The project is configured to create a Windows EXE via `jpackage`.

The generated jar and installer live under `target/`.

## Google Drive API

If you plan to compile and run this yourself, you will need your own Google Drive API OAuth 2.0 client credentials.

Follow the official quickstart here:
https://developers.google.com/workspace/drive/api/quickstart/java

The app expects those credentials in the following form:

- a packaged resource at `src/main/resources/credentials.json`

The project uses a `credentials.json` structure like this:

```json
{
  "installed": {
    "client_id": "...",
    "project_id": "...",
    "auth_uri": "https://accounts.google.com/o/oauth2/auth",
    "token_uri": "https://oauth2.googleapis.com/token",
    "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
    "client_secret": "...",
    "redirect_uris": ["http://localhost"]
  }
}
```

Do not keep private OAuth secrets in source control. Prefer environment variables or a local `.gitignored` credentials file.


## Authors

- [@alexnita3](https://github.com/alexnita3)