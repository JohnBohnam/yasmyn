package com.example.repositories

import com.example.config.DatabaseConfig.db
import com.example.database.tables.PictureTable
import com.example.models.Picture
import slick.jdbc.SQLiteProfile.api._
import scala.concurrent.{ExecutionContext, Future}

class PictureRepository(implicit ec: ExecutionContext) {

  def createPicture(userId: Long, filename: String): Future[Picture] = {
    val picture = Picture(-1, userId, filename, new java.sql.Timestamp(System.currentTimeMillis()), archived = false)
    val action = (PictureTable.pictures returning PictureTable.pictures.map(_.id)) += picture

    db.run(action)
      .map(id => picture.copy(id = id))
      .recoverWith {
        case ex: Exception if ex.getMessage.contains("FOREIGN KEY") =>
          Future.failed(new IllegalArgumentException("User does not exist"))
      }
  }

  def getPicturesByUser(userId: Long, limit: Int, afterId: Option[Long]): Future[Seq[Picture]] = {
    val query = afterId match {
      case Some(id) => PictureTable.pictures
        .filter(_.userId === userId)
        .filter(_.id > id)
        .sortBy(_.id.desc)
        .take(limit)
      case None => PictureTable.pictures
        .filter(_.userId === userId)
        .sortBy(_.id.desc)
        .take(limit)
    }
    db.run(query.result)
  }

  def getAllPictures(limit: Int, afterId: Option[Long], archived: Boolean): Future[Seq[Picture]] = {
    val baseQuery = PictureTable.pictures.filter(_.archived === archived)

    val filteredQuery = afterId match {
      case Some(id) =>
        baseQuery.filter(_.id > id).sortBy(_.id.desc).take(limit)
      case None =>
        baseQuery.sortBy(_.id.desc).take(limit)
    }

    db.run(filteredQuery.result)
  }


}