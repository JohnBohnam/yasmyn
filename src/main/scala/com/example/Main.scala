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
import slick.jdbc.SQLiteProfile.api._

import java.time.LocalDate
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
  val topicRepository = new TopicRepository()

  // Create tables
  DatabaseConfig.db.run(
    (UserTable.users.schema ++
      PictureTable.pictures.schema ++
      PostTable.posts.schema ++
      LikeTable.likes.schema ++
      CommentTable.comments.schema ++
      TopicTable.topics.schema
      ).createIfNotExists
  )

  // Setup routes
  val authRoutes = new AuthRoutes(userRepository)
  val pictureRoutes = new PictureRoutes(pictureRepository)
  val topicRoutes = new TopicRoutes(topicRepository)
  val postService = new PostService(userRepository, pictureRepository, commentRepository, likeRepository)
  val postRoutes = new PostRoutes(pictureRepository, postRepository, postService, likeRepository)
  val adminRoutes = new AdminRoutes(topicRepository)
  val appRoutes = new AppRoutes(authRoutes, pictureRoutes, topicRoutes, postRoutes, adminRoutes).routes


  // Shutdown hook to close the database connection
  sys.addShutdownHook {
    println("Shutting down...")
    db.close()
    system.terminate()
  }
  // Start server
  Http().newServerAt("localhost", 8080).bind(appRoutes)
  println("Server running at http://localhost:8080/")
}