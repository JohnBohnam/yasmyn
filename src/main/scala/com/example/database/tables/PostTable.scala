package com.example.database.tables

import com.example.models.{Picture, Post}
import slick.jdbc.SQLiteProfile.api._

class PostTable(tag: Tag) extends Table[Post](tag, "posts") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def userId = column[Long]("user_id")

  def pictureId = column[Long]("picture_id")

  def createdAt = column[java.sql.Timestamp]("created_at", O.Default(new java.sql.Timestamp(System.currentTimeMillis())))

  def archived = column[Boolean]("archived", O.Default(false))

  def topicId = column[Long]("topic_id", O.Default(0L))

  def * = (id, userId, pictureId, createdAt, archived, topicId).mapTo[Post]

  def user = foreignKey("user_fk", userId, UserTable.users)(_.id)

  def picture = foreignKey("picture_fk", pictureId, PictureTable.pictures)(_.id)

  def topic = foreignKey("topic_fk", topicId, TopicTable.topics)(_.id)
}

object PostTable {
  val posts = TableQuery[PostTable]
}