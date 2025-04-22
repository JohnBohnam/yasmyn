package com.example.models

import java.util.Date

case class User(
                 id: Long,
                 username: String,
                 email: String,
                 passwordHash: String
               ) extends Serializable

case class UserRegistrationRequest(
                                    username: String,
                                    email: String,
                                    password: String
                                  )

case class LoginRequest(
                         usernameOrEmail: String,
                         password: String
                       )