package com.hrconnect.netlib.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SubmitGitRequest(
    val assignment_id: String,
    val candidate_name: String,
    val candidate_email: String,
    val git_url: String,
)