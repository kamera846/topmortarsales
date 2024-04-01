package com.topmortar.topmortarsales.commons.utils

import com.topmortar.topmortarsales.model.ContactModel
import com.topmortar.topmortarsales.model.UserAbsentModel

class EventBusUtils {
    data class MessageEvent(val message: String)
    data class ContactModelEvent(val data: ContactModel? = null)
    data class UserAbsentModelEvent(val data: UserAbsentModel? = null)
}
