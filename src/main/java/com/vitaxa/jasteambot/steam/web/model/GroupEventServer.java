package com.vitaxa.jasteambot.steam.web.model;

public class GroupEventServer {

    private final String ip;

    private final String password;

    public GroupEventServer(String ip, String password) {
        this.ip = ip;
        this.password = password;
    }

    public String getIp() {
        return ip;
    }

    public String getPassword() {
        return password;
    }
}
