package com.topmortar.topmortarsales.commons.utils

import com.topmortar.topmortarsales.model.ContactModel

class EventBusUtils {
    data class MessageEvent(val message: String)
    data class ContactModelEvent(val data: ContactModel? = null)
}
