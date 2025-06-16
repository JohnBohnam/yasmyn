package com.example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.example.config.DatabaseConfig
import com.example.config.DatabaseConfig.db
import com.example.database.tables._
import com.example.repositories._
import com.example.routes._
import com.example.service.{PostService, UserService}
import com.example.time.TopicGenerator
import slick.jdbc.SQLiteProfile.api._

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.util.{Failure, Success}

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("scala-rest-api")
  implicit val materializer: Materializer = Materializer(system)
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  // do dropowania, czasem sie przydaje

  /*val dropAction = (
    UserTable.users.schema ++
    PictureTable.pictures.schema ++
   PostTable.posts.schema ++
   LikeTable.likes.schema ++
   CommentTable.comments.schema ++
  TopicTable.topics.schema ++
  ObservedTable.observed.schema
      ).dropIfExists

  DatabaseConfig.db.run(dropAction)*/


  val setup = (
    (UserTable.users.schema ++
      PictureTable.pictures.schema ++
      PostTable.posts.schema ++
      LikeTable.likes.schema ++
      CommentTable.comments.schema ++
      TopicTable.topics.schema ++
      ObservedTable.observed.schema
      ).createIfNotExists
    )

  val setupFuture = DatabaseConfig.db.run(setup)

  setupFuture.onComplete {
    case Success(_) =>
      println("Tables created (or already existed)")
    case Failure(ex) =>
      println(s"Failed to create tables: $ex")
  }

  // Initialize dependencies
  val userRepository = new UserRepository()
  val pictureRepository = new PictureRepository()
  val postRepository = new PostRepository()
  val commentRepository = new CommentRepository()
  val likeRepository = new LikeRepository()
  val topicRepository = new TopicRepository()
  val observedRepository = new ObservedRepository()

  val userService = new UserService(postRepository, pictureRepository)


  // Setup routes
  val authRoutes = new AuthRoutes(userRepository)
  val pictureRoutes = new PictureRoutes(pictureRepository)
  val topicRoutes = new TopicRoutes(topicRepository)
  val postService = new PostService(userRepository, pictureRepository, commentRepository, likeRepository, topicRepository, userService)
  val postRoutes = new PostRoutes(pictureRepository, postRepository, postService, likeRepository, topicRepository)
  val adminRoutes = new AdminRoutes(topicRepository)
  val observedRoutes = new MeRoutes(observedRepository, userRepository, userService, postRepository, postService)
  val userRoutes = new UserRoutes(userRepository, userService)
  val appRoutes = new AppRoutes(authRoutes, pictureRoutes, topicRoutes, postRoutes, adminRoutes, observedRoutes, userRoutes).routes


  // Shutdown hook to close the database connection
  sys.addShutdownHook {
    println("Shutting down...")
    db.close()
    system.terminate()
  }

  TopicGenerator.start(topicRepository)

  // Start server
  Http().newServerAt("0.0.0.0", 8080).bind(appRoutes)
  println("Server running at http://localhost:8080/")


  StdIn.readLine()  // Keeps the app alive until you press Enter
}