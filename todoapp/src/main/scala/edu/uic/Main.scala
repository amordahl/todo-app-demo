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

  // Redis client information
  given ActorSystem = ActorSystem()
  val redisHost     = sys.env.getOrElse("REDIS_HOST", "localhost")
  val redis         = RedisClient(redisHost, 6379)

  // Postgres db info
  val dbUrl =
    sys.env.getOrElse("DATABASE_URL", "jdbc:postgresql://localhost:5432/todos")
  val dbUser = sys.env.getOrElse("DATABASE_USER", "user")
  val dbPass = sys.env.getOrElse("DATABSE_PASSWORD", "password")
  val db = Database.forURL(
    dbUrl,
    user = dbUser,
    password = dbPass,
    driver = "org.postgresql.Driver"
  )

  val todos = TableQuery[TodoTable]

  @cask.get("/todos")
  def getTodos() =
    val cachedFut = redis.get[String]("todos")
    val resultFut = cachedFut.flatMap:
      // Already have the cached result
      case Some(cached) =>
        Future.successful(cask.Response(
          cached,
          headers = Seq("Content-Type" -> "application/json")
        ))
      // No cached result
      case None =>
        val query = todos.result
        db.run(query).flatMap: result =>
          val json = result.map(t =>
            Obj("id" -> t.id, "task" -> t.task, "done" -> t.done)
          ).toSeq
          val serialized = ujson.write(json)
          redis.set("todos", serialized).map: _ =>
            cask.Response(
              serialized,
              headers = Seq("Content-Type" -> "application/json")
            )
    Await.result(resultFut, 5.seconds)
  end getTodos

  @cask.postJson("/todos")
  def addTodo(task: ujson.Value) =
    val insert = (todos returning todos.map(x => x.id)) += Todo(
      None,
      task.str,
      done = false
    )
    val insertFut = db.run(insert).flatMap: newId =>
      redis.del("todos").map: _ =>
        Obj("id" -> newId, "task" -> task.str, "done" -> false)
    Await.result(insertFut, 5.seconds)
  end addTodo

  initialize()

end Main
