package com.example.service

import com.example.models._
import com.example.models.DTO.PostDTO
import com.example.repositories._

import java.sql.Timestamp
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}

class PostService(
                   userRepository: UserRepository,
                   pictureRepository: PictureRepository,
                   commentRepository: CommentRepository,
                   likeRepository: LikeRepository,
                   topicRepository: TopicRepository,
                   userService: UserService
                 )(implicit ec: ExecutionContext) {

  private val formatter = DateTimeFormatter.ISO_INSTANT

  def toPostResponse(post: Post): Future[Option[PostDTO]] = {
    val userF = userRepository.findById(post.userId)
    val pictureF = pictureRepository.findById(post.pictureId)
    val commentsF = commentRepository.findByPostId(post.id)
    val likesF = likeRepository.countByPostId(post.id)
    val topicF = topicRepository.getTopicById(post.topicId)
    val isLikedF = likeRepository.isPostLikedByUser(post.id, post.userId)

    for {
      userOpt <- userF
      pictureOpt <- pictureF
      comments <- commentsF
      likes <- likesF
      topicOpt <- topicF
      isLiked <- isLikedF
      postDTOOpt <- userOpt match {
        case Some(user) =>
          userService.toUserDTO(user).map { userDTO =>
            for {
              picture <- pictureOpt
              topic <- topicOpt
            } yield PostDTO(
              id = post.id,
              user = userDTO,
              picture = picture,
              createdAt = formatTimestamp(post.createdAt),
              likes = likes,
              comments = comments,
              topic = topic,
              isLiked = isLiked
            )
          }
        case None => Future.successful(None)
      }
    } yield postDTOOpt
  }


  def toPostResponses(posts: Seq[Post]): Future[Seq[PostDTO]] = {
    Future.sequence(posts.map(toPostResponse)).map(_.flatten)
  }

  private def formatTimestamp(ts: Timestamp): String = {
    ts.toInstant.atOffset(ZoneOffset.UTC).format(formatter)
  }
}
