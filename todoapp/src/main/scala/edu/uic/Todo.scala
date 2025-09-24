package edu.uic

import slick.jdbc.PostgresProfile.api.*

final case class Todo(id: Option[Int], task: String, done: Boolean)

class TodoTable(tag: Tag) extends Table[Todo](tag, "todos"):
  def id   = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def task = column[String]("task")
  def done = column[Boolean]("done")
  def *    = (id.?, task, done).mapTo[Todo]
end TodoTable
