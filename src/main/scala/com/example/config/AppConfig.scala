package com.example.config

import com.auth0.jwt.algorithms.Algorithm

object AppConfig {
  val jwtSecret = "secret" // change before production
  val algorithm: Algorithm = Algorithm.HMAC256(jwtSecret)
}