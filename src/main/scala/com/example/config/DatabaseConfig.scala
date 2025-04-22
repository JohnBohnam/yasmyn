package com.example.config

import slick.jdbc.SQLiteProfile.api._

object DatabaseConfig {
  val db = Database.forConfig("sqlite")
}