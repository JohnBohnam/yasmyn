package com.example.repositories

import com.example.config.DatabaseConfig.db
import com.example.database.tables.{LikeTable, PostTable}
import com.example.models.Post
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class PostRepository(implicit ec: ExecutionContext) {

  def createPost(userId: Long, pictureId: Long, topicId: Long): Future[Post] = {
    val post = Post(-1, userId, pictureId, new java.sql.Timestamp(System.currentTimeMillis()), archived = false, topicId)
    val action = (PostTable.posts returning PostTable.posts.map(_.id)) += post

    db.run(action)
      .map(id => post.copy(id = id))
  }

  def getPostsByUser(userId: Long, limit: Int, afterId: Option[Long]): Future[Seq[Post]] = {
    val query = afterId match {
      case Some(id) => PostTable.posts
        .filter(_.userId === userId)
        .filter(_.id > id)
        .sortBy(_.id.desc)
        .take(limit)
      case None => PostTable.posts
        .filter(_.userId === userId)
        .sortBy(_.id.desc)
        .take(limit)
    }
    db.run(query.result)
  }

  def getAllPosts(limit: Int, sortByLikes: Boolean, afterId: Option[Long], topicId: Option[Long]): Future[Seq[Post]] = {
    val baseQuery = PostTable.posts
    val filteredQuery = topicId match {
      case Some(id) =>
        baseQuery.filter(_.topicId === id).sortBy(_.id.desc)
      case None =>
        baseQuery.sortBy(_.id.desc)
    }
    if (sortByLikes) {
      val postsByLikeCount = filteredQuery
        .joinLeft(LikeTable.likes).on(_.id === _.postId)
        .groupBy { case (post, _) => post }
        .map { case (post, group) =>
          (
            post,
            group
              .filter { case (_, likeOpt) => likeOpt.isDefined }
              .length
          )
        }
        .sortBy { case (_, likeCount) => likeCount.desc }

      val action = postsByLikeCount.result

      db.run(action).map { results =>
        results.foreach { case (post, likeCount) =>
          println(s"Post ID: ${post.id}, Likes: $likeCount")
        }
        results.map(_._1)
      }
    } else {

      val finalQuery = afterId match {
        case Some(id) =>
          filteredQuery.filter(_.id > id).take(limit)
        case None =>
          filteredQuery.take(limit)
      }

      db.run(finalQuery.result).map { posts =>
        println(s"Fetched ${posts.length} posts")
        posts
      }
    }
  }
}