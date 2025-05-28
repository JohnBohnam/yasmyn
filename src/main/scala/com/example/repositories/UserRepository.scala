package com.example.repositories

import com.example.config.DatabaseConfig.db
import com.example.database.tables.UserTable
import com.example.models.{User, UserRegistrationRequest, LoginRequest}
import com.github.t3hnar.bcrypt._
import slick.jdbc.SQLiteProfile.api._
import scala.concurrent.{ExecutionContext, Future}

class UserRepository(implicit ec: ExecutionContext) {

  def createUser(request: UserRegistrationRequest): Future[Either[String, User]] = {
    val hashedPassword = request.password.bcrypt
    val user = User(-1, request.username, request.email, hashedPassword)

    val action = (UserTable.users returning UserTable.users.map(_.id)) += user

    db.run(action)
      .map(id => Right(user.copy(id = id)))
      .recover {
        case ex: Exception if ex.getMessage.contains("UNIQUE") =>
          Left("Username or email already exists")
        case ex =>
          Left("Database error: " + ex.getMessage) // optional: handle other errors too
      }
  }

  def authenticateUser(credentials: LoginRequest): Future[Option[User]] = {
    val query = UserTable.users.filter { user =>
      user.username === credentials.usernameOrEmail ||
        user.email === credentials.usernameOrEmail
    }

    db.run(query.result.headOption).flatMap {
      case Some(user) if credentials.password.isBcrypted(user.passwordHash) =>
        Future.successful(Some(user))
      case _ => Future.successful(None)
    }
  }

  def findById(id: Long): Future[Option[User]] = {
    val query = UserTable.users.filter(_.id === id)
    db.run(query.result.headOption).map {
      case Some(user) => Some(user)
      case None => None
    }
  }
}