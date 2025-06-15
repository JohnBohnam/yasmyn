package com.example.models.DTO

import java.util.Date

case class UserDTO(
                 id: Long,
                 username: String,
                 email: String,
                 imageUrl: String,
               ) extends Serializable
