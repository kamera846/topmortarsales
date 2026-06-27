package com.topmortar.topmortarsales.model

data class HobbyModel(
    var id_hobi: String = "",
    var name_hobi: String = "",
    var id_parent_hobi: String = "",
    var path_hobi: String = "",
    var created_at: String = "",
    var updated_at: String = "",
    var label: String = "",
    var isSelected: Boolean = false,
)
