package com.example.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.example.config.AppConfig.corsSettings
import com.example.models.JsonFormats._
import com.example.models.Post
import com.example.models.response.PostResponse
import com.example.repositories.{LikeRepository, PictureRepository, PostRepository}
import com.example.service.PostService
import com.example.utils.AuthUtils

import java.nio.file.Paths
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}



class PostRoutes(pictureRepository: PictureRepository, postRepository: PostRepository, postService: PostService, likeRepository: LikeRepository)
                (implicit system: ActorSystem, ec: ExecutionContext) {

  val authenticate = AuthUtils.authenticateToken
  implicit val materializer: Materializer = Materializer(system)

  implicit val postFormat = jsonFormat5(Post)

  val routes: Route = cors(corsSettings) {
    pathPrefix("posts") {
      authenticate { userId =>
        post {
          fileUpload("image") { case (metadata, byteSource) =>
            val filename = s"${UUID.randomUUID()}-${metadata.fileName}"
            val filePath = s"uploads/$filename"

            println(s"Uploading file: $filename to path: $filePath")

            // 1. Save the uploaded file to disk
            onComplete(byteSource.runWith(akka.stream.scaladsl.FileIO.toPath(Paths.get(filePath)))) {
              case Success(_) =>
                // 2. Create the Picture first
                onComplete(pictureRepository.createPicture(userId, filename)) {
                  case Success(picture) =>
                    // 3. Save the Post with reference to the picture
                    onComplete(postRepository.createPost(userId, picture.id)) {
                      case Success(savedPost) =>
                        complete(StatusCodes.OK, savedPost)
                      case Failure(postEx) =>
                        complete(StatusCodes.InternalServerError, s"Failed to create post: ${postEx.getMessage}")
                    }

                  case Failure(picEx) =>
                    complete(StatusCodes.InternalServerError, s"Failed to save picture: ${picEx.getMessage}")
                }

              case Failure(ioEx) =>
                complete(StatusCodes.InternalServerError, s"Failed to upload image: ${ioEx.getMessage}")
            }
          }
        } ~
          (get & parameters("limit".as[Int].withDefault(20), "afterId".as[Long].?)) { (limit, afterId) =>
            onComplete(postRepository.getAllPosts(limit, afterId, archived = false)) {
              case Success(posts) =>
                // Convert each Post to Future[Option[PostResponse]]
                val enrichedPostsFut: Future[Seq[PostResponse]] =
                  Future.sequence(
                    posts.map(postService.toPostResponse) // Future[Option[PostResponse]]
                  ).map(_.flatten) // Filter out None values

                onComplete(enrichedPostsFut) {
                  case Success(postResponses) => complete(StatusCodes.OK, postResponses)
                  case Failure(ex) =>
                    complete(StatusCodes.InternalServerError, s"Failed to map posts: ${ex.getMessage}")
                }

              case Failure(ex) =>
                complete(StatusCodes.InternalServerError, s"Error fetching all posts: ${ex.getMessage}")
            }
          } ~
        path(LongNumber / "likes") { postId =>
          post {
            onComplete(likeRepository.likePost(userId, postId)) {
              case Success(_) => complete(StatusCodes.OK, "Post liked")
              case Failure(ex) => complete(StatusCodes.InternalServerError, s"Failed to like post: ${ex.getMessage}")
            }
          } //TODO: implement unlike functionality and make likes unique per user (no duplicate likes)
        } ~
        path(LongNumber / "comments") { postId =>
          complete(StatusCodes.MethodNotAllowed, "Commenting on posts is not implemented yet")
          // TODO
        }
      }
    }
  }
}
