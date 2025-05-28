package com.example.models

import java.sql.Timestamp

case class Post(
               id: Long,
               userId: Long,
               pictureId: Long,
               createdAt: Timestamp,
               archived: Boolean,
               ) extends Serializable
