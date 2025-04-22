package com.example.database.tables

import com.example.models.User
import slick.jdbc.SQLiteProfile.api._

class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def username = column[String]("username", O.Unique)
  def email = column[String]("email", O.Unique)
  def passwordHash = column[String]("password_hash")
  def * = (id, username, email, passwordHash).mapTo[User]
}

object UserTable {
  val users = TableQuery[UserTable]
}