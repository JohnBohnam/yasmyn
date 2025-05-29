package com.example.time

import akka.actor.ActorSystem

import java.time.{LocalDateTime, LocalTime}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.ExecutionContext


class TopicUpdater(targetTime: LocalTime)(implicit ec: ExecutionContext) {
  val system: ActorSystem = ActorSystem("TopicUpdaterSystem")

  private def getInitialDelay(targetTime: LocalTime): FiniteDuration = {
    val now = LocalDateTime.now()
    val todayTarget = now.toLocalDate.atTime(targetTime)
    val nextRun = if (now.isBefore(todayTarget)) todayTarget else todayTarget.plusDays(1)
    val millisUntilNextRun = java.time.Duration.between(now, nextRun).toMillis
    FiniteDuration(millisUntilNextRun, TimeUnit.MILLISECONDS)
  }

  private val initialDelay: FiniteDuration = getInitialDelay(targetTime)

  system.scheduler.scheduleAtFixedRate(
    initialDelay = initialDelay,
    interval = 24.hours
  ) { () =>
    println("Choosing new topic at " + java.time.LocalDateTime.now())
  }
}
