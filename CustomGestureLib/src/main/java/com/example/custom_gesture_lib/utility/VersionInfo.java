package com.example.custom_gesture_lib.utility;


public class VersionInfo {
    private String force;
    private String version;
    private String downloadUrl;

    public VersionInfo(String force, String version, String downloadUrl) {
        this.force = force;
        this.version = version;
        this.downloadUrl = downloadUrl;
    }

    public String getForce() {
        return force;
    }

    public void setForce(String force) {
        this.force = force;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
