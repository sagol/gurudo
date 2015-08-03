package controllers

import actors._
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import model.{UserId, UserRow}
import org.joda.time.DateTime
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.{Action, BodyParsers, Controller}
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, TimeoutException}
import scala.util.{Failure, Success}

object UserController extends Controller {

  implicit val userReads: Reads[UserRow] = (
    (JsPath \ "Id").readNullable[UserId] and
      (JsPath \ "externalId").read[String] and
      (JsPath \ "email").read[String] and
      (JsPath \ "firstName").read[String] and
      (JsPath \ "lastName").read[String] and
      (JsPath \ "middleName").read[String] and
      Reads.pure(DateTime.now()) and   // createDate
      Reads.pure(DateTime.now()) and   // modifyDate
      Reads.pure(false)   // deleted
    )(UserRow.apply _)


  implicit val userWrites: Writes[UserRow] = (
    (JsPath \ "id").writeNullable[UserId] and
      (JsPath \ "externalId").write[String] and
      (JsPath \ "email").write[String] and
      (JsPath \ "firstName").write[String] and
      (JsPath \ "lastName").write[String] and
      (JsPath \ "middleName").write[String] and
      (JsPath \ "createDate").write[DateTime] and
      (JsPath \ "modifyDate").write[DateTime] and
      (JsPath \ "deleted").write[Boolean]
    )(unlift(UserRow.unapply))


  val userActor = Akka.system.actorOf(Props[actorUsers])
  val timeoutKey = "ticketoverlords.timeouts.issuer"
  val configuredTimeout = current.configuration.getInt(timeoutKey)
  val resolvedTimeout = configuredTimeout.getOrElse(5)
  implicit val timeout = Timeout(resolvedTimeout.seconds)

  def create = Action.async(BodyParsers.parse.json)  {request =>
    val userResult = request.body.validate[UserRow]
    userResult.fold(
      errors => {
        Future.successful(BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors))))
      },
      user => {
        (userActor ? createUser(user)).mapTo[UserId].recover {
          case e: TimeoutException =>
            Logger.error("Timeout for user creating - " + Json.toJson(user))
          case e: Exception =>
            Logger.error("Exception for user creating - " + Json.toJson(user), e)
        }.onComplete {
          case Success(results) =>
            Logger.info("User created - " + Json.toJson(user))
          case Failure(t) =>
            Logger.error("Exception for user creating - " + Json.toJson(user), t)
        }
        Future.successful(Ok(Json.obj("status" -> "OK", "message" -> ("User creation inited (" + user + ")"))))
      }

    )
  }

  def get(id: UserId) = Action {
    val f = ask(userActor, getUser(id)).mapTo[UserRow]
    val user = Await result (f, timeout.duration)
    Ok(Json.toJson(user))
  }

  def update = Action.async(BodyParsers.parse.json)  {request =>
    val userResult = request.body.validate[UserRow]
    userResult.fold(
      errors => {
        Future.successful(BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors))))
      },
      user => {
        (userActor ? updateUser(user)).mapTo[UserId].recover {
          case e: TimeoutException =>
            Logger.error("Timeout for user updating - " + Json.toJson(user))
          case e: Exception =>
            Logger.error("Exception for user updating - " + Json.toJson(user), e)
        }.onComplete {
          case Success(results) =>
            Logger.info("User updated - " + Json.toJson(user))
          case Failure(t) =>
            Logger.error("Exception for user updating - " + Json.toJson(user), t)
        }
        Future.successful(Ok(Json.obj("status" -> "OK", "message" -> ("User udating inited (" + user + ")"))))
      }

    )
  }

  def delete(id: UserId) = Action.async {
    (userActor ? deleteUser(id)).mapTo[UserId].recover {
      case e: TimeoutException =>
        Logger.error("Timeout for user deleting - " + id)
      case e: Exception =>
        Logger.error("Exception for user deleting - " + id, e)
    }.onComplete {
      case Success(results) =>
        Logger.info("User deleted - " + id)
      case Failure(t) =>
        Logger.error("Exception for user deleting - " + id, t)
    }
    Future.successful(Ok(Json.obj("status" -> "OK", "message" -> ("User deleting inited (" + id + ")"))))
  }
}