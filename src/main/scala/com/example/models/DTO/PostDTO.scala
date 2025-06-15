package com.example.models.DTO

import com.example.models.{Comment, Picture, Topic}

// TODO: changge field classes to response classes
case class PostDTO(
                         id: Long,
                         user: UserDTO,
                         picture: Picture,
                         createdAt: String,
                         likes: Int,
                         comments: Seq[Comment],
                         topic: Topic,
                         isLiked: Boolean,
                       ) extends Serializable
