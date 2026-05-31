package com.hrconnect.android.data.mappers

import com.autocheckmobile.data.mapper.BaseMapper
import com.hrconnect.android.domain.model.CheckResult
import com.hrconnect.android.domain.model.CheckStatus

object CheckResultMapper {

    fun toCheckResult(data: Any): CheckResult {
        @Suppress("UNCHECKED_CAST")
        val map = data as Map<String, Any>

        val statusStr = BaseMapper.getFromMap(map, "status", "error")
        val status = when (statusStr.lowercase()) {
            "passed" -> CheckStatus.PASSED
            "failed" -> CheckStatus.FAILED
            else -> CheckStatus.ERROR
        }

        return CheckResult(
            checker = BaseMapper.getFromMap(map, "checker", ""),
            status = status,
            score = (map["score"] as? Number)?.toFloat() ?: 0f,
            message = BaseMapper.getFromMap(map, "message", ""),
            details = BaseMapper.getFromMap(map, "details", "")
        )
    }

    fun toCheckResultList(dataList: List<Any>): List<CheckResult> =
        dataList.map { toCheckResult(it) }
}