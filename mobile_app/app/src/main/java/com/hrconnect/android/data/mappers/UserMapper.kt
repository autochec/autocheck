package com.hrconnect.android.data.mappers

import com.autocheckmobile.data.mapper.BaseMapper
import com.hrconnect.android.domain.model.User

/**
 * Маппер для преобразования данных пользователя.
 * Дата создания: 26-05-2026
 * Автор: Team01
 */
object UserMapper {

    /**
     * Преобразует сырой объект (Map или DTO) в User.
     * @param data объект, полученный из ResponseWrapper.data
     * @return User доменная модель
     */
    fun toUser(data: Any): User {
        // Предполагаем, что data может быть Map<String, Any> или специальным DTO.
        // Здесь универсальный подход через рефлексию карты.
        @Suppress("UNCHECKED_CAST")
        val map = data as Map<String, Any>

        return User(
            id = BaseMapper.getFromMap(map, "id", ""),
            email = BaseMapper.getFromMap(map, "email", ""),
            fullName = BaseMapper.getFromMap(
                map, "full_name",
                BaseMapper.getFromMap(map, "fullName", "")
            ),
            role = BaseMapper.getFromMap(map, "role", "candidate")
        )
    }

    /**
     * Преобразует список данных в список User.
     */
    fun toUserList(dataList: List<Any>): List<User> = dataList.map { toUser(it) }
}