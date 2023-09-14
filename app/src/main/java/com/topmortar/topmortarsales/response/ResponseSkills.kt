package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.SkillModel

data class ResponseSkills(
    val status: String,
    val results: ArrayList<SkillModel>
)
