package com.hrconnect.android.data.mappers

import com.hrconnect.netlib.data.remote.dto.ResponseWrapper

/**
 * Утилиты для работы с ResponseWrapper.
 */
object ResponseMapper {

    /**
     * Извлекает data из ResponseWrapper и преобразует через указанный маппер.
     * @param wrapper ответ API
     * @param mapper функция преобразования (например, UserMapper::toUser)
     * @return преобразованная сущность
     * @throws Exception если wrapper.error не null или data == null
     */
    inline fun <T, R> extractData(wrapper: ResponseWrapper<T>, mapper: (T) -> R): R {
        if (wrapper.error != null) {
            throw Exception(wrapper.error)
        }
        val data = wrapper.data ?: throw Exception("Response data is null")
        return mapper(data)
    }

    /**
     * Для списков.
     */
    inline fun <T, R> extractList(wrapper: ResponseWrapper<List<T>>, mapper: (T) -> R): List<R> {
        if (wrapper.error != null) {
            throw Exception(wrapper.error)
        }
        val list = wrapper.data ?: emptyList()
        return list.map { mapper(it) }
    }
}