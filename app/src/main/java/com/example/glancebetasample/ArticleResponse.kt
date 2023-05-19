package com.example.glancebetasample

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleResponse(
    val title: String,
    val url: String,
    val user: QiitaUser,
    @SerialName("likes_count") val likesCount: Int
)

@Serializable
data class QiitaUser(
    val name: String
)