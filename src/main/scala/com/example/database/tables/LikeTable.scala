package com.example.database.tables

import com.example.models.{Like, Picture, Post}
import slick.jdbc.SQLiteProfile.api._

class LikeTable(tag: Tag) extends Table[Like](tag, "likes") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def userId = column[Long]("user_id")

  def postId = column[Long]("post_id")

  def createdAt = column[java.sql.Timestamp]("created_at", O.Default(new java.sql.Timestamp(System.currentTimeMillis())))

  def * = (id, userId, postId, createdAt).mapTo[Like]

  def user = foreignKey("user_fk", userId, UserTable.users)(_.id)

  def post = foreignKey("post_fk", postId, PostTable.posts)(_.id)
}

object LikeTable {
  val likes = TableQuery[LikeTable]

  def findByPostId(postId: Long): Query[LikeTable, Like, Seq] = likes.filter(_.postId === postId)
}