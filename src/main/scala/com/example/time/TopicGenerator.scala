package com.example.time

import com.example.models.Topic
import com.example.repositories.TopicRepository

import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.util.concurrent.{Executors, TimeUnit}
import scala.util.Random



object TopicGenerator {

  private val TOPIC_TIME = 4

  def start(topicRepository: TopicRepository): Unit = {
    val scheduler = Executors.newSingleThreadScheduledExecutor()

    val task = new Runnable {
      def run(): Unit = {
        createTopic(topicRepository, now())
      }
    }

    scheduler.scheduleAtFixedRate(task, 0, TOPIC_TIME, TimeUnit.MINUTES)
  }

  private def createTopic(topicRepository:TopicRepository, startTime: LocalDateTime): Unit = {
    val endTime = startTime.plusMinutes(TOPIC_TIME)
    val topicName = generateName()

    val newTopic = Topic(-1, topicName, startTime, endTime)

    topicRepository.createTopic(newTopic.content, newTopic.from, newTopic.to)
  }

  private def generateName(): String = {
    val topics = List(
      "Goofy face",
      "Sad face",
      "Happy face",
      "Zdjęcie stóp",
      "Zdjęcie oczu",
      "Zdjęcie z kolegą",
      "Zdjęcie z koleżanką",
      "Ulubione miejsce do picia",
      "Zdjęcie ulubionej rzeczy",
      "Zdjęcie ulubionego jedzenia",
      "Zdjęcie w majestaycznej pozie",
      "Zdjęcie na którym udajesz zwierzę",
      "Zdjęcie czegoś, czego się boisz",
      "Zdjęcie czegoś pięknego",
      "Śmieszne zdjęcie",
      "Zdjęcie Tomka",
    )

    topics(Random.nextInt(topics.length))
  }
}