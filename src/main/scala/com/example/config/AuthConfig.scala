package com.example.config

import akka.http.scaladsl.model.{HttpMethods, StatusCodes}
import akka.http.scaladsl.model.headers.HttpOrigin
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives.{complete, optionalHeaderValueByName, pass}
import ch.megard.akka.http.cors.scaladsl.model.HttpOriginMatcher
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

object AuthConfig {
  val adminToken: String = "kokoko"

  val corsSettings: CorsSettings = CorsSettings.defaultSettings.withAllowedOrigins(
    HttpOriginMatcher.*
  ).withAllowedMethods(Seq(
    HttpMethods.GET,
    HttpMethods.POST,
    HttpMethods.PUT,
    HttpMethods.DELETE,
    HttpMethods.OPTIONS
  ))

  def authenticateAdmin: Directive0 = {
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(token) if token == s"Bearer ${AuthConfig.adminToken}" =>
        pass
      case _ =>
        complete(StatusCodes.Unauthorized -> "Admin token is missing or invalid")
    }
  }
}
