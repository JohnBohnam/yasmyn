package com.example.models

import java.time.LocalDate

case class Topic(
  id: Long,
  content: String,
  activeDate: LocalDate,
) extends Serializable