package controllers

import actors._
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import model.{ObesityRow, ObesityId, UserId}
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


object ObesityController extends Controller {

  implicit val obesityReads: Reads[ObesityRow] = (
      (JsPath \ "id").readNullable[ObesityId] and
      (JsPath \ "weight").read[Double] and
      (JsPath \ "bmi").read[Double] and
      (JsPath \ "bodyFat").read[Double] and
      (JsPath \ "waistSize").read[Int] and
      (JsPath \ "waistToHeightRatio").read[Double] and
      (JsPath \ "userId").read[UserId] and
      Reads.pure(DateTime.now()) and   // createDate
      Reads.pure(DateTime.now()) and   // modifyDate
      Reads.pure(false)   // deleted
    )(ObesityRow.apply _)


  implicit val obesityWrites: Writes[ObesityRow] = (
    (JsPath \ "id").writeNullable[ObesityId] and
      (JsPath \ "weight").write[Double] and
      (JsPath \ "bmi").write[Double] and
      (JsPath \ "bodyFat").write[Double] and
      (JsPath \ "waistSize").write[Int] and
      (JsPath \ "waistToHeightRatio").write[Double] and
      (JsPath \ "userId").write[UserId] and
      (JsPath \ "createDate").write[DateTime] and
      (JsPath \ "modifyDate").write[DateTime] and
      (JsPath \ "deleted").write[Boolean]
    )(unlift(ObesityRow.unapply))

  val obesityActor = Akka.system.actorOf(Props[actorObesities])
  val timeoutKey = "ticketoverlords.timeouts.issuer"
  val configuredTimeout = current.configuration.getInt(timeoutKey)
  val resolvedTimeout = configuredTimeout.getOrElse(5)
  implicit val timeout = Timeout(resolvedTimeout.seconds)


  def create = Action.async(BodyParsers.parse.json)  {request =>
    val obesityResult = request.body.validate[ObesityRow]
    obesityResult.fold(
      errors => {
        Future.successful(BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors))))
      },
      obesity => {
        (obesityActor ? createObesity(obesity)).mapTo[ObesityId].recover {
          case e: TimeoutException =>
            Logger.error("Timeout for obesity creating - " + Json.toJson(obesity))
          case e: Exception =>
            Logger.error("Exception for obesity creating - " + Json.toJson(obesity), e)
        }.onComplete {
          case Success(results) =>
            Logger.info("Obesity created - " + Json.toJson(obesity))
          case Failure(t) =>
            Logger.error("Exception for obesity creating - " + Json.toJson(obesity), t)
        }
        Future.successful(Ok(Json.obj("status" -> "OK", "message" -> ("Obesity creation inited (" + obesity + ")"))))
      }

    )
  }

  def get(id: ObesityId) = Action {
    val f = ask(obesityActor, getObesity(id)).mapTo[ObesityRow]
    val obesity = Await result (f, timeout.duration)
    Ok(Json.toJson(obesity))
  }

  def getAll(id: UserId) = Action {
    val f = ask(obesityActor, getAllObesity(id)).mapTo[ObesityRow]
    val obesities = Await result (f, timeout.duration)
    Ok(Json.toJson(obesities))
  }

  def update = Action.async(BodyParsers.parse.json)  {request =>
    val obesityResult = request.body.validate[ObesityRow]
    obesityResult.fold(
      errors => {
        Future.successful(BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors))))
      },
      obesity => {
        (obesityActor ? updateObesity(obesity)).mapTo[ObesityId].recover {
          case e: TimeoutException =>
            Logger.error("Timeout for obesity updating - " + Json.toJson(obesity))
          case e: Exception =>
            Logger.error("Exception for obesity updating - " + Json.toJson(obesity), e)
        }.onComplete {
          case Success(results) =>
            Logger.info("Obesity updated - " + Json.toJson(obesity))
          case Failure(t) =>
            Logger.error("Exception for obesity updating - " + Json.toJson(obesity), t)
        }
        Future.successful(Ok(Json.obj("status" -> "OK", "message" -> ("Obesity udating inited (" + obesity + ")"))))
      }
    )
  }

  def delete(id: ObesityId) = Action.async {
    (obesityActor ? deleteObesity(id)).mapTo[ObesityId].recover {
      case e: TimeoutException =>
        Logger.error("Timeout for obesity deleting - " + id)
      case e: Exception =>
        Logger.error("Exception for obesity deleting - " + id, e)
    }.onComplete {
      case Success(results) =>
        Logger.info("Obesity deleted - " + id)
      case Failure(t) =>
        Logger.error("Exception for obesity deleting - " + id, t)
    }
    Future.successful(Ok(Json.obj("status" -> "OK", "message" -> ("Obesity deleting inited (" + id + ")"))))
  }
}