package com.example.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import com.example.repositories.{ObservedRepository, UserRepository}
import com.example.utils.AuthUtils
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.{Directive1, Route}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.example.config.AuthConfig.corsSettings
import com.example.models.DTO.UserDTO
import com.example.models.User
import com.example.service.UserService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ObserveRequest(observedUserId: Long)

object ObserveJsonProtocol extends DefaultJsonProtocol {
  implicit val observeRequestFormat: RootJsonFormat[ObserveRequest] = jsonFormat1(ObserveRequest)
}

class MeRoutes(observedRepo: ObservedRepository, userRepository: UserRepository, userService: UserService)(implicit ec: ExecutionContext) {

  import ObserveJsonProtocol.observeRequestFormat
  import com.example.models.JsonFormats._

  val authenticate: Directive1[Long] = AuthUtils.authenticateToken
  val test: ToEntityMarshaller[Seq[UserDTO]] = implicitly

  val routes: Route =
    cors(corsSettings) {
      pathPrefix("me") {
        authenticate { userId =>
          concat(
            pathEndOrSingleSlash {
              get {
                onSuccess(userRepository.findById(userId)) {
                  case Some(user) =>
                    onSuccess(userService.toUserDTO(user)) { userDTO =>
                      complete(userDTO)
                    }
                  case None => complete(StatusCodes.NotFound, "User not found")
                }
              }
            },
            path("observe") {
              post {
                entity(as[ObserveRequest]) { req =>
                  if (req.observedUserId == userId) {
                    complete(StatusCodes.BadRequest, "Cannot observe yourself")
                  } else {
                    onComplete(observedRepo.observe(userId, req.observedUserId)) {
                      case Success(_) =>
                        complete(StatusCodes.OK)
                      case Failure(ex: IllegalArgumentException) if ex.getMessage == "User already observed" =>
                        complete(StatusCodes.Conflict, "User already observed")
                      case Failure(ex) =>
                        complete(StatusCodes.InternalServerError, "Unexpected error")
                    }
                  }
                }
              } ~
                delete {
                  entity(as[ObserveRequest]) { req =>
                    onSuccess(observedRepo.unObserve(userId, req.observedUserId)) { _ =>
                      complete(StatusCodes.OK)
                    }
                  }
                }
            },
            path("observed") {
              get {
                onSuccess(observedRepo.getObservedUsers(userId)) { observedUsers =>
                  val usersFut: Future[Seq[User]] =
                    Future.sequence(
                      observedUsers.map(o => userRepository.findById(o.observedUserId))
                    ).map(_.flatten)
                  onSuccess(usersFut) { users =>
                    val userDTOsFut = Future.sequence(users.map(userService.toUserDTO))
                    onSuccess(userDTOsFut) { userDTOs =>
                      complete(userDTOs)
                    }
                  }
                }
              }
            },
            path("observing") {
              get {
                onSuccess(observedRepo.getObservingUsers(userId)) { observingUsers =>
                  val usersFut: Future[Seq[User]] =
                    Future.sequence(
                      observingUsers.map(o => userRepository.findById(o.userId))
                    ).map(_.flatten)
                  onSuccess(usersFut) { users =>
                    val userDTOsFut = Future.sequence(users.map(userService.toUserDTO))
                    onSuccess(userDTOsFut) { userDTOs =>
                      complete(userDTOs)
                    }
                  }
                }
              }
            }
          )
        }
      }
    }
}