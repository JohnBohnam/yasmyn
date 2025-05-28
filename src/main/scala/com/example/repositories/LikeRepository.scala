package com.example.repositories

import com.example.config.DatabaseConfig.db
import com.example.database.tables.LikeTable
import com.example.models.Like
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class LikeRepository(implicit ec: ExecutionContext) {

  def likePost(userId: Long, postId: Long): Future[Like] = {
    val like = Like(-1, userId, postId, new java.sql.Timestamp(System.currentTimeMillis()))
    val action = (LikeTable.likes returning LikeTable.likes.map(_.id)) += like

    db.run(action)
      .map(id => like.copy(id = id))
  }

  def getByPostId(postId: Long, limit: Int, afterId: Option[Long]): Future[Seq[Like]] = {
    val query = afterId match {
      case Some(id) => LikeTable.likes
        .filter(_.postId === postId)
        .filter(_.id > id)
        .sortBy(_.id.desc)
        .take(limit)
      case None => LikeTable.likes
        .filter(_.postId === postId)
        .sortBy(_.id.desc)
        .take(limit)
    }
    db.run(query.result)
  }

  def countByPostId(postId: Long): Future[Int] = {
    val query = LikeTable.likes.filter(_.postId === postId)
      .length

    db.run(query.result)
  }
}
