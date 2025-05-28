package com.example.models

import java.sql.Timestamp

case class Like(
                 id: Long,
                 userId: Long,
                 postId: Long,
                 createdAt: Timestamp,
               ) extends Serializable
