package com.example.repositories

import com.example.database.tables.CommentTable
import com.example.models.Comment
import com.example.config.DatabaseConfig.db
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class CommentRepository(implicit ec: ExecutionContext) {

  def createComment(postId: Long, userId: Long, content: String): Future[Comment] = {
    val comment = Comment(-1, postId, userId, content, new java.sql.Timestamp(System.currentTimeMillis()))
    val action = (CommentTable.comments returning CommentTable.comments.map(_.id)) += comment

    db.run(action)
      .map(id => comment.copy(id = id))
  }

  def getCommentsByPostId(postId: Long, limit: Int, afterId: Option[Long]): Future[Seq[Comment]] = {
    val query = afterId match {
      case Some(id) => CommentTable.comments
        .filter(_.postId === postId)
        .filter(_.id > id)
        .sortBy(_.id.desc)
        .take(limit)
      case None => CommentTable.comments
        .filter(_.postId === postId)
        .sortBy(_.id.desc)
        .take(limit)
    }
    db.run(query.result)
  }

  def findByPostId(postId: Long): Future[Seq[Comment]] = {
    val query = CommentTable.comments.filter(_.postId === postId)
    db.run(query.result)
  }

}
