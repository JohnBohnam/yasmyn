package com.example.database.tables

import com.example.models.Comment
import slick.jdbc.SQLiteProfile.api._

class CommentTable(tag: Tag) extends Table[Comment](tag, "comments") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def userId = column[Long]("user_id")

  def postId = column[Long]("post_id")

  def content = column[String]("content")

  def dateCreated = column[java.sql.Timestamp]("date_created", O.Default(new java.sql.Timestamp(System.currentTimeMillis())))

  def * = (id, userId, postId, content, dateCreated).mapTo[Comment]

  def user = foreignKey("user_fk", userId, UserTable.users)(_.id)

  def post = foreignKey("post_fk", postId, PostTable.posts)(_.id)
}

object CommentTable {
  val comments = TableQuery[CommentTable]

  def findByPostId(postId: Long): Query[CommentTable, Comment, Seq] = comments.filter(_.postId === postId)

  def findByUserId(userId: Long): Query[CommentTable, Comment, Seq] = comments.filter(_.userId === userId)
}
