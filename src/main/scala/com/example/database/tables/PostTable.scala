package com.example.database.tables

import com.example.models.{Picture, Post}
import slick.jdbc.SQLiteProfile.api._

class PostTable(tag: Tag) extends Table[Post](tag, "posts") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def userId = column[Long]("user_id")

  def pictureId = column[Long]("picture_id")

  def createdAt = column[java.sql.Timestamp]("created_at", O.Default(new java.sql.Timestamp(System.currentTimeMillis())))

  def archived = column[Boolean]("archived", O.Default(false))

  def * = (id, userId, pictureId, createdAt, archived).mapTo[Post]

  def user = foreignKey("user_fk", userId, UserTable.users)(_.id)

  def picture = foreignKey("picture_fk", pictureId, PictureTable.pictures)(_.id)
}

object PostTable {
  val posts = TableQuery[PostTable]
}