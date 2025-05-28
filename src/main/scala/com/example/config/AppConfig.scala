package com.example.config

import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.headers.HttpOrigin
import ch.megard.akka.http.cors.scaladsl.model.HttpOriginMatcher
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.auth0.jwt.algorithms.Algorithm

object AppConfig {
  val jwtSecret = "secret" // change before production
  val algorithm: Algorithm = Algorithm.HMAC256(jwtSecret)

  val corsSettings: CorsSettings = CorsSettings.defaultSettings.withAllowedOrigins(
    HttpOriginMatcher(
      HttpOrigin("http://localhost:3000"),
      HttpOrigin("http://localhost:5173"),
      HttpOrigin("http://localhost:8081")
    )
  ).withAllowedMethods(Seq(
    HttpMethods.GET,
    HttpMethods.POST,
    HttpMethods.PUT,
    HttpMethods.DELETE,
    HttpMethods.OPTIONS
  ))
}