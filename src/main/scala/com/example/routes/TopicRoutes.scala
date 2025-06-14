package com.example.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.example.Globals
import com.example.models.JsonFormats._
import com.example.repositories.TopicRepository
import spray.json.RootJsonFormat

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext
import scala.util.Success

class TopicRoutes(topicRepository: TopicRepository)(implicit system: ActorSystem, ec: ExecutionContext) {

  case class TopicResponse(date: String, topic: String, topicId: String, expiresAt: LocalDateTime)

  implicit val materializer: Materializer = Materializer(system)

  implicit val topicResponseFormat: RootJsonFormat[TopicResponse] = jsonFormat4(TopicResponse)

  val routes: Route = cors () {
    pathPrefix("topic" / "today") {
        get {
          val today = LocalDate.now()
          onComplete(topicRepository.getActiveTopic) {
            case Success(Some(topic)) =>
              complete(
                StatusCodes.OK,
                TopicResponse(
                  date = today.toString,
                  topic = topic.content,
                  topicId = topic.id.toString,
                  expiresAt = topic.to
                )
              )
            case _ =>
              complete(
                StatusCodes.NotFound,
                TopicResponse(
                  date = today.toString,
                  topic = "No topic available for today",
                  topicId = "N/A",
                  expiresAt = LocalDateTime.now().plusDays(9999)
                )
              )
          }
        }
    }
  }

}
