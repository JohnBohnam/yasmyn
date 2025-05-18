package com.example

object Globals {
  @volatile var isUploading: Boolean = true
  @volatile var timeLeft: Long = 0 // time left in seconds
}
