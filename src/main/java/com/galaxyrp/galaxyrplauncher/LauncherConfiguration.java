package com.galaxyrp.galaxyrplauncher;

import com.galaxyrp.galaxyrplauncher.enums.GameMods;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LauncherConfiguration {
    private String serverIp;
    private String serverName;
    private String server2Ip;
    private String server2Name;
    private String googleDriveLink;
    private GameMods clientMod;
    private int resolutionX;
    private int resolutionY;
    private String customArguments;
    private boolean scanOnStartup;
    private boolean autoDownload;

    public LauncherConfiguration() {
    }
}
