package com.example.routes

import akka.http.scaladsl.server.Directives.{onSuccess, parameter, path, pathPrefix}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.example.config.AuthConfig.corsSettings
import com.example.repositories.UserRepository
import akka.http.scaladsl.server.Directives._
import com.example.models.JsonFormats._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directive1, Route}
import com.example.models.DTO.UserDTO
import com.example.models.User
import com.example.service.UserService
import com.example.utils.AuthUtils

import scala.concurrent.{ExecutionContext, Future}

class UserRoutes(userRepo: UserRepository,
                 userService: UserService)(implicit ec: ExecutionContext) {
  val authenticate: Directive1[Long] = AuthUtils.authenticateToken

  val routes: Route = {
    cors(corsSettings) {
      pathPrefix("users") {
        authenticate { userId =>
          path("search") {
            get {
              parameter("username") { username =>
                onSuccess(userRepo.searchByUsername(username)) { users =>
                  if (users.isEmpty) {
                    complete(StatusCodes.NotFound, "No users found")
                  } else {
                    // jest error, ale sie kompiluje lol
                    val userDTOsF: Future[Seq[UserDTO]] = Future.sequence(users.map((user: User) => userService.toUserDTO(user) : Future[UserDTO]))
                    onSuccess(userDTOsF) { userDTOs =>
                      complete(userDTOs)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
