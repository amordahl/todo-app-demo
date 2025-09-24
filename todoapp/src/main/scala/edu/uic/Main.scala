package edu.uic

import cask.main.MainRoutes
import redis.RedisClient
import org.apache.pekko.actor.ActorSystem

import slick.jdbc.PostgresProfile.api.*
import scala.concurrent.Future
import ujson.Obj
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object Main extends MainRoutes:
  override def host: String = "0.0.0.0"

  // Postgres db info
  val dbUrl = "jdbc:postgresql://localhost:5432/todos"
  val dbUser = "postgres"
  val dbPass = "postgres"
  val db = Database.forURL(
    dbUrl,
    user = dbUser,
    password = dbPass,
    driver = "org.postgresql.Driver"
  )

  val todos = TableQuery[TodoTable]

  @cask.get("/todos")
  def getTodos() =
    val query = todos.result
    val resultFut = db.run(query).flatMap: result =>
      val json = result.map(t =>
        Obj("id" -> t.id, "task" -> t.task, "done" -> t.done)
      ).toSeq
      val serialized = ujson.write(json)
      Future.successful(cask.Response(
        serialized,
        headers = Seq("Content-Type" -> "application/json")
      ))
    Await.result(resultFut, 5.seconds)
  end getTodos

  @cask.postJson("/todos")
  def addTodo(task: ujson.Value) =
    val insert = (todos returning todos.map(x => x.id)) += Todo(
      None,
      task.str,
      done = false
    )
    Await.result(db.run(insert), 5.seconds)
  end addTodo

  initialize()

end Main
