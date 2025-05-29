package com.example.repositories

import com.example.Globals
import com.example.config.DatabaseConfig.db
import com.example.database.tables.TopicTable
import com.example.models.Topic
import slick.jdbc.SQLiteProfile.api._

import java.time.{LocalDate, LocalTime}
import scala.concurrent.{ExecutionContext, Future}

class TopicRepository(implicit ec: ExecutionContext) {

  def createTopic(content: String, activeDate: LocalDate): Future[Topic] = {
    val topic = Topic(-1, content, activeDate)
    val action = (TopicTable.topics returning TopicTable.topics.map(_.id)) += topic

    db.run(action).map(id => topic.copy(id = id))
  }

  def getTopicById(id: Long): Future[Option[Topic]] = {
    val query = TopicTable.topics.filter(_.id === id).result.headOption
    db.run(query)
  }

  def getActiveTopic: Future[Option[Topic]] = {
    val now = LocalTime.now()
    val today = LocalDate.now()

    val effectiveDate =
      if (now.isBefore(Globals.topicChangeTime)) today.minusDays(1)
      else today

    val query = TopicTable.topics
      .filter(_.activeDate === effectiveDate)
      .result
      .headOption

    db.run(query)
  }

  def replaceTopic(content: String, activeDate: LocalDate): Future[Topic] = {
    val query = TopicTable.topics.filter(_.activeDate === activeDate)

    val action = for {
      existingOpt <- query.result.headOption
      result <- existingOpt match {
        case Some(existing) =>
          val updated = existing.copy(content = content)
          query.map(_.content).update(content).map(_ => updated)
        case None =>
          val newTopic = Topic(-1, content, activeDate)
          (TopicTable.topics returning TopicTable.topics.map(_.id))
            .into((topic, id) => topic.copy(id = id)) += newTopic
      }
    } yield result

    db.run(action.transactionally)
  }

}
