package com.example.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.HttpOriginRange.*
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.example.Main.system
import com.example.models.{LoginRequest, UserRegistrationRequest}
import com.example.repositories.UserRepository
import com.example.utils.AuthUtils
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import java.util.UUID

// Import CORS directives
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import akka.http.scaladsl.model.headers.{HttpOrigin, HttpOriginRange}
import akka.http.scaladsl.model.HttpMethods
import com.typesafe.config.ConfigFactory
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors

class AuthRoutes(userRepository: UserRepository)(implicit ec: ExecutionContext) {

  import com.example.models.JsonFormats._

  val routes: Route = {
    pathPrefix("auth") {
      cors () {
        path("register") {
          post {
            entity(as[UserRegistrationRequest]) { request =>
              onComplete(userRepository.createUser(request)) {
                case Success(Right(user)) =>
                  complete(201, Map("message" -> "User created", "userId" -> user.id.toString))
                case Success(Left(error)) =>
                  complete(409, Map("error" -> error))
                case Failure(ex) =>
                  complete(500, Map("error" -> s"Internal server error: ${ex.getMessage}"))
              }
            }
          }
        }
      } ~
        cors() {
          path("login") {
            post {
              entity(as[LoginRequest]) { request =>
                onComplete(userRepository.authenticateUser(request)) {
                  case Success(Some(user)) =>
                    val token = AuthUtils.generateToken(user.id)
                    complete(200, Map("token" -> token, "userId" -> user.id.toString))
                  case Success(None) =>
                    complete(401, Map("error" -> "Invalid username or password"))
                  case Failure(ex) =>
                    complete(500, Map("error" -> s"Authentication failed: ${ex.getMessage}"))
                }
              }
            }
          }
        }
    }
  }
}