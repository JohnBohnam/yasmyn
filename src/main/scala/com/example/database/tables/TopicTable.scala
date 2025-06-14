package com.example.database.tables

import com.example.models.{Topic, User}
import slick.jdbc.SQLiteProfile.api._

import java.time.{LocalDate, LocalDateTime}

class TopicTable(tag: Tag) extends Table[Topic](tag, "topics") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def content = column[String]("content")
  def from = column[LocalDateTime]("from", O.Unique)
  def to = column[LocalDateTime]("to", O.Unique)
  def * = (id, content, from, to).mapTo[Topic]
}

object TopicTable {
  val topics = TableQuery[TopicTable]
}