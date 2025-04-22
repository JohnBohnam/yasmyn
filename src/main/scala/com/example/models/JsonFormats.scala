package com.example.models

import spray.json._

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime}
import java.util.UUID

object JsonFormats extends DefaultJsonProtocol {

  // Core model formats
  implicit val userRegistrationFormat: RootJsonFormat[UserRegistrationRequest] = jsonFormat3(UserRegistrationRequest)
  implicit val loginFormat: RootJsonFormat[LoginRequest] = jsonFormat2(LoginRequest)
  implicit val userFormat: RootJsonFormat[User] = jsonFormat4(User)

  // Timestamp format (java.sql.Timestamp)
  implicit object TimestampFormat extends RootJsonFormat[Timestamp] {
    override def write(ts: Timestamp): JsValue = JsString(ts.toString)
    override def read(json: JsValue): Timestamp = json match {
      case JsString(s) => Timestamp.valueOf(s)
      case JsNumber(n) => new Timestamp(n.longValue)
      case _ => throw DeserializationException("Expected timestamp string or epoch number")
    }
  }

  // Java Time formats (alternative to Timestamp)
  implicit val instantFormat: RootJsonFormat[Instant] = new RootJsonFormat[Instant] {
    override def write(i: Instant): JsValue = JsNumber(i.toEpochMilli)
    override def read(json: JsValue): Instant = json match {
      case JsNumber(n) => Instant.ofEpochMilli(n.longValue)
      case JsString(s) => Instant.parse(s)
      case _ => throw DeserializationException("Expected epoch millis or ISO-8601 string")
    }
  }

  implicit val localDateTimeFormat: RootJsonFormat[LocalDateTime] = new RootJsonFormat[LocalDateTime] {
    override def write(ldt: LocalDateTime): JsValue = JsString(ldt.toString)
    override def read(json: JsValue): LocalDateTime = json match {
      case JsString(s) => LocalDateTime.parse(s)
      case _ => throw DeserializationException("Expected ISO-8601 datetime string")
    }
  }

  // UUID format
  implicit val uuidFormat: RootJsonFormat[UUID] = new RootJsonFormat[UUID] {
    override def write(uuid: UUID): JsValue = JsString(uuid.toString)
    override def read(json: JsValue): UUID = json match {
      case JsString(s) => UUID.fromString(s)
      case _ => throw DeserializationException("Expected UUID string")
    }
  }

  // Now the Picture format — placed after Timestamp & UUID implicits
  implicit val pictureFormat: RootJsonFormat[Picture] = jsonFormat4(Picture)

  // Error response format
  implicit val errorFormat: RootJsonFormat[ErrorResponse] = jsonFormat2(ErrorResponse)


}

case class ErrorResponse(error: String, details: Option[String] = None)
