package com.galaxyrp.galaxyrplauncher;

import com.galaxyrp.galaxyrplauncher.enums.ClientMods;

public class LauncherConfiguration {
    private String serverIp;
    private String serverName;
    private String server2Ip;
    private String server2Name;
    private String googleDriveLink;
    private ClientMods clientMod;
    private int resolutionX;
    private int resolutionY;
    private String customArguments;
    private boolean scanOnStartup;
    private boolean autoDownload;

    public LauncherConfiguration() {
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServer2Ip() {
        return server2Ip;
    }

    public void setServer2Ip(String server2Ip) {
        this.server2Ip = server2Ip;
    }

    public String getServer2Name() {
        return server2Name;
    }

    public void setServer2Name(String server2Name) {
        this.server2Name = server2Name;
    }

    public String getGoogleDriveLink() {
        return googleDriveLink;
    }

    public void setGoogleDriveLink(String googleDriveLink) {
        this.googleDriveLink = googleDriveLink;
    }

    public ClientMods getClientMod() {
        return clientMod;
    }

    public void setClientMod(ClientMods clientMod) {
        this.clientMod = clientMod;
    }

    public int getResolutionX() {
        return resolutionX;
    }

    public void setResolutionX(int resolutionX) {
        this.resolutionX = resolutionX;
    }

    public int getResolutionY() {
        return resolutionY;
    }

    public void setResolutionY(int resolutionY) {
        this.resolutionY = resolutionY;
    }

    public String getCustomArguments() {
        return customArguments;
    }

    public void setCustomArguments(String customArguments) {
        this.customArguments = customArguments;
    }

    public boolean isScanOnStartup() {
        return scanOnStartup;
    }

    public void setScanOnStartup(boolean scanOnStartup) {
        this.scanOnStartup = scanOnStartup;
    }

    public boolean isAutoDownload() {
        return autoDownload;
    }

    public void setAutoDownload(boolean autoDownload) {
        this.autoDownload = autoDownload;
    }
}
