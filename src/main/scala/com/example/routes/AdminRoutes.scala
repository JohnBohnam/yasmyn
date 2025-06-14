package com.example.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.example.Globals
import com.example.config.AuthConfig.authenticateAdmin
import com.example.models.JsonFormats._
import com.example.repositories.TopicRepository
import spray.json.RootJsonFormat

import java.time.{LocalDate, LocalDateTime, LocalTime}
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

class AdminRoutes(topicRepository: TopicRepository)(implicit system: ActorSystem, ec: ExecutionContext) {

  final case class TopicSetRequest(content: String)

  implicit val topicSetRequestFormat: RootJsonFormat[TopicSetRequest] = jsonFormat1(TopicSetRequest)

  val adminRoutes: Route =
    pathPrefix("admin" / "topic") {
      authenticateAdmin {
        concat(
          (post & path("today")) {
            entity(as[TopicSetRequest]) { req =>
              val now = LocalDateTime.now()

              onSuccess(topicRepository.replaceTopic(req.content, now)) { _ =>
                complete(StatusCodes.OK)
              }
            }
          },

          (post & path(Segment)) { dateStr =>
            entity(as[TopicSetRequest]) { req =>
              Try(LocalDateTime.parse(dateStr)) match {
                case Success(date) =>
                  onSuccess(topicRepository.replaceTopic(req.content, date)) { _ =>
                    complete(StatusCodes.OK)
                  }
                case Failure(_) =>
                  complete(StatusCodes.BadRequest -> s"Invalid date format: $dateStr (expected yyyy-MM-dd)")
              }
            }
          }
        )
      }
    }


}
