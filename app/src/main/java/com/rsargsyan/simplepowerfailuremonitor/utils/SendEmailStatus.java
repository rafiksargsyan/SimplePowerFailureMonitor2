package com.rsargsyan.simplepowerfailuremonitor.utils;

import com.google.gson.annotations.SerializedName;

public class SendEmailStatus {
    @SerializedName("statusCode")
    private String statusCode;

    public SendEmailStatus(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
}
