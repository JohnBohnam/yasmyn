package com.example.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.FileIO
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.example.config.AuthConfig.corsSettings
import com.example.models.JsonFormats._
import com.example.models.Post
import com.example.models.DTO.PostDTO
import com.example.repositories.{LikeRepository, PictureRepository, PostRepository, TopicRepository}
import com.example.service.PostService
import com.example.utils.AuthUtils

import java.nio.file.{Files, Paths}
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


class PostRoutes(pictureRepository: PictureRepository,
                 postRepository: PostRepository,
                 postService: PostService,
                 likeRepository: LikeRepository,
                 topicRepository: TopicRepository)
                (implicit system: ActorSystem, ec: ExecutionContext) {

  val authenticate = AuthUtils.authenticateToken
  implicit val materializer: Materializer = Materializer(system)

  implicit val postFormat = jsonFormat6(Post)

  val routes: Route = cors(corsSettings) {
    pathPrefix("posts") {
      authenticate { userId =>
        post {
          fileUpload("image") { case (metadata, byteSource) =>
            val filename = s"${UUID.randomUUID()}-${metadata.fileName}"

            val uploadDir = Paths.get("uploads")
            if (!Files.exists(uploadDir)) {
              Files.createDirectories(uploadDir)
            }
            val filePath = uploadDir.resolve(filename)


            // 1. Save the uploaded file to disk
            onComplete(byteSource.runWith(FileIO.toPath(filePath))) {
              case Success(_) =>
                // 2. Create the Picture first
                println(s"Saving picture with filename: $filename")
                onComplete(pictureRepository.createPicture(userId, filename)) {
                  case Success(picture) =>
                    println(s"Picture saved with ID: ${picture.id}")
                    println("Fetching active topic...")
                    onComplete(topicRepository.getActiveTopic) {
                      case Success(Some(topic)) =>
                        // 3. Save the Post with reference to the picture and topic
                        println(s"Active topic found with ID: ${topic.id}, creating post...")
                        onComplete(postRepository.createPost(userId, picture.id, topic.id)) {
                          case Success(savedPost) =>
                            complete(StatusCodes.OK, savedPost)
                          case Failure(postEx) =>
                            complete(StatusCodes.InternalServerError, s"Failed to create post: ${postEx.getMessage}")
                        }
                      case Success(None) =>
                        complete(StatusCodes.NotFound, "No active topic found")

                      case Failure(topicEx) =>
                        complete(StatusCodes.InternalServerError, s"Failed to fetch active topic: ${topicEx.getMessage}")
                    }
                  case Failure(picEx) =>
                    complete(StatusCodes.InternalServerError, s"Failed to save picture: ${picEx.getMessage}")
                }

              case Failure(ioEx) =>
                complete(StatusCodes.InternalServerError, s"Failed to upload image: ${ioEx.getMessage}")
            }
          }
        } ~
          (get & parameters("limit".as[Int].withDefault(20), "sortByLikes".as[Boolean].withDefault(false), "afterId".as[Long].?, "topicId".as[Long].?)) { (limit, sortByLikes, afterId, topicId) =>
            onComplete(postRepository.getAllPosts(limit, sortByLikes,  afterId, topicId)) {
              case Success(posts) =>
                // Convert each Post to Future[Option[PostResponse]]
                val enrichedPostsFut: Future[Seq[PostDTO]] =
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
              onComplete(likeRepository.isPostLikedByUser(userId, postId)) {
                case Success(true) =>
                  complete(StatusCodes.Conflict, "Post already liked")
                case Success(false) =>
                  onComplete(likeRepository.likePost(userId, postId)) {
                    case Success(_) =>
                      complete(StatusCodes.OK, "Post liked")
                    case Failure(ex) =>
                      complete(StatusCodes.InternalServerError, s"Failed to like post: ${ex.getMessage}")
                  }
                case Failure(ex) =>
                  complete(StatusCodes.InternalServerError, s"Failed to check like status: ${ex.getMessage}")
              }
            } ~
              delete {
                onComplete(likeRepository.unlikePost(userId, postId)) {
                  case Success(0) =>
                    complete(StatusCodes.NotFound, "Like not found")
                  case Success(_) =>
                    complete(StatusCodes.OK, "Post unliked")
                  case Failure(ex) =>
                    complete(StatusCodes.InternalServerError, s"Failed to unlike post: ${ex.getMessage}")
                }
              }
          } ~
          path(LongNumber / "comments") { postId =>
            complete(StatusCodes.MethodNotAllowed, "Commenting on posts is not implemented yet")
            // TODO
          }
      }
    }
  }
}
