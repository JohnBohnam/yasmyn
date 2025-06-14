package com.example.models

import java.time.{LocalDate, LocalDateTime}

case class Topic(
  id: Long,
  content: String,
  from: LocalDateTime,
  to: LocalDateTime,
) extends Serializable