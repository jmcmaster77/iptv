package com.soft77reload.iptv

import com.google.gson.annotations.SerializedName
// no lo estoy utilizando ya que el servidor envia string
data class LoginResponse(
    @SerializedName("video1") var video1: String,
    @SerializedName("video2") var video2: String,
)

