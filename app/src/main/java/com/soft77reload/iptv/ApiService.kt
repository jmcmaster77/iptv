package com.soft77reload.iptv

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.GET
import retrofit2.http.Url
import javax.crypto.spec.RC2ParameterSpec

interface ApiService {
    @POST
    suspend fun toLogin(@Url url: String): Response<String>

    @GET
    suspend fun toChannelList(@Url url:String): Response<String>
}