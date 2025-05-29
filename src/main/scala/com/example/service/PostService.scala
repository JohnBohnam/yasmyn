package com.example.service

import com.example.models._
import com.example.models.response.PostResponse
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
                   topicRepository: TopicRepository
                 )(implicit ec: ExecutionContext) {

  private val formatter = DateTimeFormatter.ISO_INSTANT

  def toPostResponse(post: Post): Future[Option[PostResponse]] = {
    val userF = userRepository.findById(post.userId)
    val pictureF = pictureRepository.findById(post.pictureId)
    val commentsF = commentRepository.findByPostId(post.id)
    val likesF = likeRepository.countByPostId(post.id)
    val topicF = topicRepository.getTopicById(post.topicId)

    for {
      userOpt <- userF
      pictureOpt <- pictureF
      comments <- commentsF
      likes <- likesF
      topicOpt <- topicF
    } yield {
      for {
        user <- userOpt
        picture <- pictureOpt
        topic <- topicOpt
      } yield PostResponse(
        id = post.id,
        user = user,
        picture = picture,
        createdAt = formatTimestamp(post.createdAt),
        likes = likes,
        comments = comments,
        topic = topic
      )
    }
  }

  def toPostResponses(posts: Seq[Post]): Future[Seq[PostResponse]] = {
    Future.sequence(posts.map(toPostResponse)).map(_.flatten)
  }

  private def formatTimestamp(ts: Timestamp): String = {
    ts.toInstant.atOffset(ZoneOffset.UTC).format(formatter)
  }
}
