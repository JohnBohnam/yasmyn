package com.example

import slick.jdbc.SQLiteProfile.api._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.example.config.{AppConfig, DatabaseConfig}
import com.example.database.tables.{PictureTable, UserTable}
import com.example.repositories.{PictureRepository, UserRepository}
import com.example.routes.{AppRoutes, AuthRoutes, PictureRoutes, TopicRoutes}

import scala.concurrent.ExecutionContextExecutor

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("scala-rest-api")
  implicit val materializer: Materializer = Materializer(system)
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  // Initialize dependencies
  val userRepository = new UserRepository()
  val pictureRepository = new PictureRepository()

  // Create tables
  DatabaseConfig.db.run(
    (UserTable.users.schema ++ PictureTable.pictures.schema).createIfNotExists
  )

  // Setup routes
  val authRoutes = new AuthRoutes(userRepository)
  val pictureRoutes = new PictureRoutes(pictureRepository)
  val topicRoutes = new TopicRoutes(pictureRepository)
  val appRoutes = new AppRoutes(authRoutes, pictureRoutes, topicRoutes).routes

  // Start server
  Http().newServerAt("localhost", 8080).bind(appRoutes)
  println("Server running at http://localhost:8080/")
}