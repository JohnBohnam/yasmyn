package com.example.database.tables

import slick.jdbc.SQLiteProfile.api._

case class Observed(id: Long = 0L, userId: Long, observedUserId: Long)

class ObservedTable(tag: Tag) extends Table[Observed](tag, "observed") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def userId = column[Long]("user_id")

  def observedUserId = column[Long]("observed_user_id")

  def * = (id, userId, observedUserId) <> (Observed
    .tupled, Observed
    .unapply)

  def user = foreignKey("fk_user", userId, UserTable.users)(_.id, onDelete = ForeignKeyAction.Cascade)

  def observedUser = foreignKey("fk_observed_user", observedUserId, UserTable.users)(_.id, onDelete = ForeignKeyAction.Cascade)

  def userIndex = index("idx_user", userId)

  def observedUserIndex = index("idx_observed_user", observedUserId)

  def uniqueObserved = index("idx_unique_observed", (userId, observedUserId), unique = true)
}

object ObservedTable {
  val observed = TableQuery[ObservedTable]
}