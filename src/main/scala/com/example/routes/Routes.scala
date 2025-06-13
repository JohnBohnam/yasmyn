package com.example.routes

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContext

class AppRoutes(authRoutes: AuthRoutes,
                pictureRoutes: PictureRoutes,
                topicRoutes: TopicRoutes,
                postRoutes: PostRoutes,
                adminRoutes: AdminRoutes,
                observedRoutes: MeRoutes,
                userRoutes: UserRoutes)
               (implicit ec: ExecutionContext) {
  val routes: Route =
    authRoutes.routes ~
      pictureRoutes.routes ~
      topicRoutes.routes ~
      postRoutes.routes ~
      adminRoutes.adminRoutes ~
      observedRoutes.routes ~
      userRoutes.routes
}