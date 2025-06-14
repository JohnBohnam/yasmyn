package com.example.repositories

import com.example.Globals
import com.example.config.DatabaseConfig.db
import com.example.database.tables.TopicTable
import com.example.models.Topic
import slick.jdbc.SQLiteProfile.api._

import java.time.{LocalDate, LocalDateTime, LocalTime}
import scala.concurrent.{ExecutionContext, Future}

class TopicRepository(implicit ec: ExecutionContext) {

  def createTopic(content: String, from: LocalDateTime, to: LocalDateTime): Future[Topic] = {
    val topic = Topic(-1, content, from, to)
    val action = (TopicTable.topics returning TopicTable.topics.map(_.id)) += topic

    db.run(action).map(id => topic.copy(id = id))
  }

  def getTopicById(id: Long): Future[Option[Topic]] = {
    val query = TopicTable.topics.filter(_.id === id).result.headOption
    db.run(query)
  }

  def getActiveTopic: Future[Option[Topic]] = {
    val now = LocalDateTime.now()

    val query = TopicTable.topics
      .filter(topic => topic.from <= now && topic.to >= now)
      .result
      .headOption

    db.run(query)
  }

  def replaceTopic(content: String, activeDate: LocalDateTime): Future[Topic] = {
    val query = TopicTable.topics.filter(topic => topic.from <= activeDate && topic.to >= activeDate)

    val action = for {
      existingOpt <- query.result.headOption
      result <- existingOpt match {
        case Some(existing) =>
          val updated = existing.copy(content = content)
          query.map(_.content).update(content).map(_ => updated)
        case None =>
          val newTopic = Topic(-1, content, activeDate, activeDate.plusMinutes(1)) // 5 min na potrzeby prezentacji
          (TopicTable.topics returning TopicTable.topics.map(_.id))
            .into((topic, id) => topic.copy(id = id)) += newTopic
      }
    } yield result

    db.run(action.transactionally)
  }

}
