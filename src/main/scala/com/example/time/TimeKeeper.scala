package com.example.time

import com.example.Globals.{isUploading, timeLeft}
import com.example.database.tables.PictureTable
import slick.jdbc.SQLiteProfile.api._

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import scala.concurrent.{ExecutionContext, Future}


class TimeKeeper(db: Database)(implicit ec: ExecutionContext) {
  // Example periodic task: update pictures older than a week to archived = true
  private def runTask(): Future[Unit] = {
    if (isUploading) {
      println("[TimeKeeper] Uploading phase: cleaning and archiving pictures")

      val deleteAction = PictureTable.pictures
        .filter(_.archived === true)
        .delete

      val archiveAction = PictureTable.pictures
        .filter(_.archived === false)
        .map(_.archived)
        .update(true)

      val combined = deleteAction.andThen(archiveAction)

      db.run(combined.transactionally).map { _ =>
        println("[TimeKeeper] Deleted archived pictures and archived the rest")
      }.recover {
        case ex =>
          println(s"[TimeKeeper] Error during uploading phase: ${ex.getMessage}")
      }

    } else {
      println("[TimeKeeper] Voting phase: nothing to do")
      Future.successful(())
    }
  }

  // Starts the periodic scheduler
  def startScheduler(uploadingTime: Long, votingTime: Long): Unit = {
    val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    val intervalSeconds = if (isUploading) uploadingTime else votingTime
    val task = new Runnable {
      def run(): Unit = {
        timeLeft = intervalSeconds
        runTask() // Run the DB operation
        isUploading = !isUploading
      }
    }
    scheduler.scheduleAtFixedRate(task, 0, intervalSeconds, TimeUnit.SECONDS)
  }
}
