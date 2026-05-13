package com.agrotech.ai.data.local

import com.agrotech.ai.data.model.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotificationManager {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount = _unreadCount.asStateFlow()

    fun addNotification(title: String, message: String) {
        val newItem = NotificationItem(title = title, message = message)
        _notifications.value = listOf(newItem) + _notifications.value
        _unreadCount.value += 1
    }

    fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        _unreadCount.value = 0
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
        _unreadCount.value = 0
    }
}
