package com.example.models

import java.sql.Timestamp

case class Comment(
                    id: Long,
                    userId: Long,
                    postId: Long,
                    content: String,
                    dateCreated: Timestamp,
                  ) extends Serializable
