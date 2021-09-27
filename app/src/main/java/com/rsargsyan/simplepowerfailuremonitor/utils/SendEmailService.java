package com.rsargsyan.simplepowerfailuremonitor.utils;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SendEmailService {

    @POST("/prod")
    Call<SendEmailStatus> sendEmail(@Query("port") String port,
                         @Query("to") String to,
                         @Query("subject") String subject,
                         @Query("body") String body);
}
