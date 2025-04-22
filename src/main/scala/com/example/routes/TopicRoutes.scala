package com.example.routes

import java.time.LocalDate
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.pathPrefix
import com.example.models.JsonFormats._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.Materializer
import com.example.repositories.PictureRepository
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import spray.json.RootJsonFormat

import java.sql.Timestamp
import scala.concurrent.ExecutionContext

class TopicRoutes(pictureRepository: PictureRepository)(implicit system: ActorSystem, ec: ExecutionContext) {

  case class TopicResponse(date: String, topic: String, topidId: String, expiresAt: Timestamp)

  val topics = Map(
    LocalDate.of(2025, 4, 22) -> "Topic for April 22, 2025",
    LocalDate.of(2025, 4, 23) -> "Topic for April 23, 2025",
    LocalDate.of(2025, 4, 24) -> "Topic for April 24, 2025",
    LocalDate.of(2025, 4, 25) -> "Topic for April 25, 2025",
    LocalDate.of(2025, 4, 26) -> "Topic for April 26, 2025",
    LocalDate.of(2025, 4, 27) -> "Topic for April 27, 2025",
    LocalDate.of(2025, 4, 28) -> "Topic for April 28, 2025",
    LocalDate.of(2025, 4, 29) -> "Topic for April 29, 2025",
    LocalDate.of(2025, 4, 30) -> "Topic for April 30, 2025"
  )

  implicit val materializer: Materializer = Materializer(system)

  implicit val topicResponseFormat: RootJsonFormat[TopicResponse] = jsonFormat4(TopicResponse)

  val routes: Route = cors () {
    pathPrefix("topic" / "today") {
        get {
          val today = LocalDate.now()
          val topic = topics.getOrElse(today, "Default topic for today")
          complete(
            StatusCodes.OK,
            TopicResponse(
              date = today.toString,
              topic = topic,
              topidId = today.toString,
              expiresAt = Timestamp.valueOf(today.plusDays(1).atStartOfDay())
            )
          )
        }
    }
  }

}
