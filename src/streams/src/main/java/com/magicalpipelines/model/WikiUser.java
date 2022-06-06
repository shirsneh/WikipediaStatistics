package com.magicalpipelines.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WikiUser extends WikiActive implements Serializable {
    @SerializedName("IsBot")
    private boolean isBot;

    public WikiUser(WikiEvent wikiEvent) {
        super(wikiEvent.getUserName());
        this.isBot = wikiEvent.getIsBot();
    }

    public WikiUser(WikiUser other) {
        super(other.getName());
        this.isBot = other.isBot;
        this.counter = other.counter;
    }

    public WikiUser(String name, boolean isBot) {
        super(name);
        this.isBot = isBot;
    }

    public boolean getIsBot() {
        return isBot;
    }

    @Override
    public String toString() {
        return "WikiUser{" +
                "isBot=" + isBot +
                ", name='" + name + '\'' +
                ", counter=" + counter +
                '}';
    }
}
