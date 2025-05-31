package com.example.repositories

import com.example.database.tables.{Observed, ObservedTable}
import slick.jdbc.SQLiteProfile.api._
import scala.concurrent.{ExecutionContext, Future}
import com.example.config.DatabaseConfig.db


class ObservedRepository(implicit ec: ExecutionContext) {

  def observe(userId: Long, observedUserId: Long): Future[Int] = {
    val insertAction = ObservedTable.observed += Observed(0L, userId, observedUserId)

    db.run(insertAction).recoverWith {
      case ex: java.sql.SQLException if ex.getMessage.contains("UNIQUE constraint") =>
        Future.failed(new IllegalArgumentException("User already observed"))
      case other => Future.failed(other)
    }
  }

  def unObserve(userId: Long, observedUserId: Long): Future[Int] = {
    db.run(
      ObservedTable.observed
        .filter(f => f.userId === userId && f.observedUserId === observedUserId)
        .delete
    )
  }

  def getObservedUsers(userId: Long): Future[Seq[Observed]] = {
    db.run(
      ObservedTable.observed
        .filter(_.userId === userId)
        .result
    )
  }

  def getObservingUsers(userId: Long): Future[Seq[Observed]] = {
    db.run(
      ObservedTable.observed
        .filter(_.observedUserId === userId)
        .result
    )
  }
}