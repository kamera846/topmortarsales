package com.topmortar.topmortarsales.commons.utils

import com.topmortar.topmortarsales.model.ContactModel
import com.topmortar.topmortarsales.model.HobbyModel
import com.topmortar.topmortarsales.model.UserAbsentModel

class EventBusUtils {
    data class MessageEvent(val message: String)
    data class ContactModelEvent(val data: ContactModel? = null)
    data class ListHobbyEvent(val data: List<HobbyModel> = listOf())
    data class UserAbsentModelEvent(val data: UserAbsentModel? = null)
    data class IntEvent(val data: Int? = null)
}
