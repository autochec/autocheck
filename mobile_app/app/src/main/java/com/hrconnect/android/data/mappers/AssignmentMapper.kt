package com.hrconnect.android.data.mappers

import com.autocheckmobile.data.mapper.BaseMapper
import com.hrconnect.android.domain.model.Assignment

object AssignmentMapper {

    fun toAssignment(data: Any): Assignment {
        @Suppress("UNCHECKED_CAST")
        val map = data as Map<String, Any>

        return Assignment(
            id = BaseMapper.getFromMap(map, "id", ""),
            title = BaseMapper.getFromMap(map, "title", ""),
            description = BaseMapper.getFromMap(map, "description", ""),
            technologies = (map["technologies"] as? List<String>) ?: emptyList(),
            activeCheckers = (map["active_checkers"] as? List<String>) ?: emptyList(),
            weights = (map["weights"] as? Map<String, Int>) ?: emptyMap(),
            instructions = BaseMapper.getFromMap(map, "instructions", ""),
            isPublished = BaseMapper.getFromMap(map, "is_published", false)
        )
    }

    fun toAssignmentList(dataList: List<Any>): List<Assignment> = dataList.map { toAssignment(it) }
}