package com.example.weatherexpect.Data.Response.Data

import com.google.gson.annotations.SerializedName

data class Refer(
    @SerializedName("sources")val sources: List<String>?,
    @SerializedName("license")val license: List<String>?
)

/*
*refer.sources 原始数据来源，或数据源说明，可能为空
refer.license 数据许可或版权声明，可能为空
* */