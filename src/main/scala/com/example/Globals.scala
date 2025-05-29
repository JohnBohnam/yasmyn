package com.example

import java.sql.Timestamp
import java.time.{LocalDateTime, LocalTime}

object Globals {
  val topicChangeTime: LocalTime = LocalTime.of(8, 0) // 8 AM

  def getNextTopicChangeTime: Timestamp = {
    val now = LocalDateTime.now()
    val todayTarget = now.toLocalDate.atTime(topicChangeTime)
    val target = if (now.toLocalTime.isBefore(topicChangeTime)) todayTarget else todayTarget.plusDays(1)
    Timestamp.valueOf(target)
  }
}
