package com.example.repositories

import com.example.config.DatabaseConfig.db
import com.example.database.tables.PostTable
import com.example.models.Post
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class PostRepository(implicit ec: ExecutionContext) {

  def createPost(userId: Long, pictureId: Long): Future[Post] = {
    val post = Post(-1, userId, pictureId, new java.sql.Timestamp(System.currentTimeMillis()), archived = false)
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

  def getAllPosts(limit: Int, afterId: Option[Long], archived: Boolean): Future[Seq[Post]] = {
    val baseQuery = PostTable.posts.filter(_.archived === archived)

    val filteredQuery = afterId match {
      case Some(id) =>
        baseQuery.filter(_.id > id).sortBy(_.id.desc).take(limit)
      case None =>
        baseQuery.sortBy(_.id.desc).take(limit)
    }

    db.run(filteredQuery.result)

    db.run(filteredQuery.result).map { posts =>
      println(s"Fetched ${posts.length} posts")
      posts
    }
  }


}