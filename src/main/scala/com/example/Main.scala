package com.example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.example.config.DatabaseConfig
import com.example.config.DatabaseConfig.db
import com.example.database.tables._
import com.example.repositories._
import com.example.routes._
import com.example.service.PostService
import com.example.time.TimeKeeper
import slick.jdbc.SQLiteProfile.api._

import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.ExecutionContextExecutor

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("scala-rest-api")
  implicit val materializer: Materializer = Materializer(system)
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  // Initialize dependencies
  val userRepository = new UserRepository()
  val pictureRepository = new PictureRepository()
  val postRepository = new PostRepository()
  val commentRepository = new CommentRepository()
  val likeRepository = new LikeRepository()

  // Create tables
  DatabaseConfig.db.run(
    (UserTable.users.schema ++
      PictureTable.pictures.schema ++
      PostTable.posts.schema ++
      LikeTable.likes.schema ++
      CommentTable.comments.schema
      ).createIfNotExists
  )

  // Setup routes
  val authRoutes = new AuthRoutes(userRepository)
  val pictureRoutes = new PictureRoutes(pictureRepository)
  val topicRoutes = new TopicRoutes(pictureRepository)
  val postService = new PostService(userRepository, pictureRepository, commentRepository, likeRepository)
  val postRoutes = new PostRoutes(pictureRepository, postRepository, postService, likeRepository)
  val appRoutes = new AppRoutes(authRoutes, pictureRoutes, topicRoutes, postRoutes).routes


  // Shutdown hook to close the database connection
  val keeper = new TimeKeeper(db)
  keeper.startScheduler(60, 60) // !!!! AHTUNG !!!!! - pierwsza wartosc ile czasu ludzie mają na uploadowanie, po tym czasie jest zmiana
  // i druga wartość ile czasu mają na ocenianie, po tym czasie jest zmiana znowu i zaczyna się od nowa (w sekundach)
  val timeUpdater = Executors.newSingleThreadScheduledExecutor()
  timeUpdater.scheduleAtFixedRate(() => {
    if (Globals.timeLeft > 0) {
      Globals.timeLeft -= 1
    }
  }, 0, 1, TimeUnit.SECONDS)


  // Start server
  Http().newServerAt("localhost", 8080).bind(appRoutes)
  println("Server running at http://localhost:8080/")
}