package com.example.database.tables

import com.example.models.{Topic, User}
import slick.jdbc.SQLiteProfile.api._

import java.time.LocalDate

class TopicTable(tag: Tag) extends Table[Topic](tag, "topics") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def content = column[String]("content")
  def activeDate = column[LocalDate]("active_date", O.Unique)
  def * = (id, content, activeDate).mapTo[Topic]
}

object TopicTable {
  val topics = TableQuery[TopicTable]
}