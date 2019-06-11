package com.vitaxa.jasteambot.socket.response.model;

public class BotInfo {

    private String botName;

    private boolean running;

    private String type;

    public BotInfo() {
    }

    public BotInfo(String queueName, boolean running, String type) {
        this.botName = queueName;
        this.running = running;
        this.type = type;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
