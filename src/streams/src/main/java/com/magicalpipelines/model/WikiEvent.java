package com.magicalpipelines.model;
import com.google.gson.annotations.SerializedName;

public class WikiEvent {
    @SerializedName("event_id")
    private Long eventId;
    @SerializedName("title")
    private String title;

    @SerializedName("user_type")
    private String userType;

    @SerializedName("username")
    private String username;

    @SerializedName("timestamp")
    private String dateTime;

    @SerializedName("language")
    private String language;

    @SerializedName("type")
    private String type;

    @SerializedName("is_revert")
    private Boolean isRevert;

    public Long getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public String getUserType() {
        return userType;
    }

    public String getUsername() {
        return username;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getLanguage() {
        return language;
    }

    public String getType() {
        return type;
    }

    public Boolean getRevert() {
        return isRevert;
    }

    @Override
    public String toString() {
        return "WikiEvent{" +
                "eventId=" + eventId +
                ", title='" + title + '\'' +
                ", userType='" + userType + '\'' +
                ", username='" + username + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", language='" + language + '\'' +
                ", type='" + type + '\'' +
                ", isRevert=" + isRevert +
                '}';
    }
}
