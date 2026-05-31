package com.hrconnect.netlib.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VerdictRequest(
    val verdict: String,   // "accepted" или "rejected"
    val comment: String = "",
)