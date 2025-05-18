// com/example/database/tables/PictureTable.scala
package com.example.database.tables

import com.example.models.Picture
import slick.jdbc.SQLiteProfile.api._

class PictureTable(tag: Tag) extends Table[Picture](tag, "pictures") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userId = column[Long]("user_id")
  def filename = column[String]("filename")
  def createdAt = column[java.sql.Timestamp]("created_at", O.Default(new java.sql.Timestamp(System.currentTimeMillis())))
  def archived = column[Boolean]("archived", O.Default(false))
  def * = (id, userId, filename, createdAt, archived).mapTo[Picture]
  def user = foreignKey("user_fk", userId, UserTable.users)(_.id)
}

object PictureTable {
  val pictures = TableQuery[PictureTable]
}