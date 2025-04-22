package com.example.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import com.example.models.JsonFormats._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import scala.util.{Failure, Success}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.example.models.Picture
import com.example.repositories.PictureRepository
import com.example.utils.AuthUtils

import java.util.UUID
import scala.concurrent.ExecutionContext
import java.nio.file.Paths

class PictureRoutes(pictureRepository: PictureRepository)(implicit system: ActorSystem, ec: ExecutionContext) {

  // Import your authentication directive
  val authenticate = AuthUtils.authenticateToken  // This must match your AuthUtils implementation

  // Make sure you have a Materializer available implicitly
  implicit val materializer: Materializer = Materializer(system)

  implicit val pictureFormat = jsonFormat4(Picture)

  val routes: Route = cors() {
    pathPrefix("pictures") {
      authenticate { userId =>
        post {
          fileUpload("image") { case (metadata, byteSource) =>
            val filename = s"${UUID.randomUUID()}-${metadata.fileName}"
            val filePath = s"uploads/$filename"

            onComplete(byteSource.runWith(akka.stream.scaladsl.FileIO.toPath(Paths.get(filePath)))) { _ =>
              onComplete(pictureRepository.createPicture(userId, filename)) { _ =>
                complete("Image uploaded")
              }
            }
          }
        } ~
          (get & parameters("limit".as[Int].withDefault(20), "afterId".as[Long].?)) { (limit, afterId) =>
            onComplete(pictureRepository.getAllPictures(limit, afterId)) {
              case Success(pictures) => complete(StatusCodes.OK, pictures)
              case Failure(ex) =>
                complete(StatusCodes.InternalServerError, s"Error fetching all pictures: ${ex.getMessage}")
            }
          } ~
          path(Segment) { targetUserId =>
            get {
              parameters("limit".as[Int].withDefault(20), "afterId".as[Long].?) { (limit, afterId) =>
                targetUserId.toLongOption match {
                  case Some(validUserId) =>
                    onComplete(pictureRepository.getPicturesByUser(validUserId, limit, afterId)) {
                      case Success(pictures) => complete(StatusCodes.OK, pictures)
                      case Failure(ex) =>
                        complete(StatusCodes.InternalServerError, s"Error fetching user pictures: ${ex.getMessage}")
                    }
                  case None =>
                    complete(StatusCodes.BadRequest, "Invalid userId format")
                }
              }
            }
          }
      }
    } ~
      pathPrefix("uploads" / Remaining) { fileName =>
        get {
          val filePath = Paths.get("uploads", fileName)
          if (filePath.toFile.exists()) {
            getFromFile(filePath.toFile)
          } else {
            complete(StatusCodes.NotFound, "File not found")
          }
        }
      }
  }
}
