package com.hrconnect.android.data.mappers

import com.autocheckmobile.data.mapper.BaseMapper
import com.hrconnect.android.domain.model.Submission
import com.hrconnect.android.domain.model.SubmissionStatus
import com.hrconnect.android.domain.model.Verdict

object SubmissionMapper {

    fun toSubmission(data: Any): Submission {
        @Suppress("UNCHECKED_CAST")
        val map = data as Map<String, Any>

        // Маппинг статуса
        val statusStr = BaseMapper.getFromMap(map, "status", "pending")
        val status = when (statusStr.lowercase()) {
            "running" -> SubmissionStatus.RUNNING
            "done" -> SubmissionStatus.DONE
            "error" -> SubmissionStatus.ERROR
            else -> SubmissionStatus.PENDING
        }

        // Маппинг вердикта (может отсутствовать)
        val verdictStr = map["verdict"] as? String
        val verdict = when (verdictStr?.lowercase()) {
            "accepted" -> Verdict.ACCEPTED
            "rejected" -> Verdict.REJECTED
            else -> null
        }

        return Submission(
            id = BaseMapper.getFromMap(map, "id", ""),
            assignmentId = BaseMapper.getFromMap(map, "assignment_id", ""),
            candidateName = BaseMapper.getFromMap(map, "candidate_name", ""),
            candidateEmail = BaseMapper.getFromMap(map, "candidate_email", ""),
            submittedAt = BaseMapper.parseDate(map["submitted_at"] as? String),
            status = status,
            finalScore = (map["final_score"] as? Number)?.toFloat(),
            verdict = verdict
        )
    }

    fun toSubmissionList(dataList: List<Any>): List<Submission> = dataList.map { toSubmission(it) }
}