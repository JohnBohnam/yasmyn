package com.example.routes

import akka.http.scaladsl.server.Directives.{onSuccess, parameter, path, pathPrefix}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.example.config.AuthConfig.corsSettings
import com.example.repositories.UserRepository
import akka.http.scaladsl.server.Directives._
import com.example.models.JsonFormats._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.{Directive1, Route}
import com.example.utils.AuthUtils

class UserRoutes(userRepo: UserRepository) {
  val authenticate: Directive1[Long] = AuthUtils.authenticateToken

  val routes: Route = {
    cors(corsSettings) {
      pathPrefix("users") {
        authenticate { userId =>
          path("search") {
            get {
              parameter("username") { username =>
                onSuccess(userRepo.searchByUsername(username)) { users =>
                  complete(users)
                }
              }
            }
          }
        }
      }
    }
  }
}
