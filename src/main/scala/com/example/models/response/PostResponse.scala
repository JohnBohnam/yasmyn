package com.example.models.response

import com.example.models.{Comment, Picture, Topic, User}

// TODO: changge field classes to response classes
case class PostResponse(
                         id: Long,
                         user: User,
                         picture: Picture,
                         createdAt: String,
                         likes: Int,
                         comments: Seq[Comment],
                         topic: Topic,
                       ) extends Serializable
