package com.hrconnect.netlib.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val full_name: String,
    val password: String,
    val role: String = "candidate",
)