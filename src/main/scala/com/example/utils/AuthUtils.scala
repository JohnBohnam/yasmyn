package com.example.utils

import akka.http.javadsl.server.Directives.reject
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{headerValueByName, provide}
import com.auth0.jwt.JWT
import com.example.config.AppConfig

import java.util.Date

object AuthUtils {
  def generateToken(userId: Long): String = {
    JWT.create()
      .withSubject(userId.toString)
      .withExpiresAt(new Date(System.currentTimeMillis() + 86400000))
      .sign(AppConfig.algorithm)
  }

  def authenticateToken: Directive1[Long] = {
    headerValueByName("Authorization").flatMap { authHeader =>
      val token = authHeader.replace("Bearer ", "")
      try {
        val verifier = JWT.require(AppConfig.algorithm).build()
        val decoded = verifier.verify(token)
        val userId = decoded.getSubject.toLong
        provide(userId)
      } catch {
        case _: Exception => reject.asInstanceOf[Directive1[Long]]
      }
    }
  }
}