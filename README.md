# GalaxyRP Launcher

A launcher made for the game Jedi Knight: Jedi Academy. 

The goal of this project is to create a JKA launcher that provides players with an easy way to manage their mods and connect to online servers. The launcher allows players to quickly and easily browse and update mods and pk3 files from a shared google drive. It also provides a streamlined interface for players to connect to online servers, making it easier join multiplayer matches. The launcher includes features such as one-click updates for all pk3s and a user-friendly interface that makes it easy for players of all skill levels to use. This project aims to enhance the gaming experience for players by simplifying the modding process and making online play more accessible.

While this was made for [GalaxyRP](https://github.com/alexnita3/GalaxyRP), it will work with all JKA mods.


## Installation

Download the latest release, and unzip the contents inside your GameData folder in your JKA installation. 

To configure the mod manager, go into the settings tab, and paste a google drive folder link. A (mock) example of this is:

```bash
  https://drive.google.com/drive/folders/1YiIL-g-fKshPeerjYFyIBgFTfd5jaa3N
```

Anyone who wants to connect to the same google drive folder will need read AND write permissions on the folder.


## Screenshots

Main Screen:

![App Screenshot](https://i.imgur.com/hZMWuwV.png)

Settings Screen:

![App Screenshot](https://i.imgur.com/Ul0tqTA.png)


## Google Drive API

If you plan to compile and run this yourself, you will need to generate your own google drive API Oauth2.0 keys.

#### Oauth key service name:

```http
  drive.googleapis.com
```

Once you have your key, download and save it as:

```http
  client_secrets.json
```

Then place it in the same folder as your debug app.


## Tech Stack

**IDE:** Visual Studio 2019

**App:** WinForms

**APIs:** [Google Drive API](https://developers.google.com/drive/api/guides/about-sdk)


## Privacy Policy

The only data I have access to about the users of this app are the number of api calls and the success rate of those calls.

I do **NOT** have access to: private email addresses used with this, contents of the downloads, any of the settings used, any passwords, IPs and configs.

I have never and will never use the information this app collects for any financial benefit, nor will I share it with anyone else, unless to help the development of this app.


## Authors

- [@alexnita3](https://github.com/alexnita3)