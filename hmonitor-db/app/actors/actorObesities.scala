package actors

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import model._
import play.api.Play.current
import play.api.db.slick._
import repositories.{ObesityRepository}

import scala.slick.lifted.{Tag, TableQuery}

/**
 * Created by sgl on 21.07.15.
 */

case class createObesity(obesity: ObesityRow)
case class updateObesity(obesity: ObesityRow)
case class getObesity(id: ObesityId)
case class getAllObesity(id: UserId)
case class deleteObesity(id: ObesityId)

class actorObesities extends Actor{

  val log = Logging(context.system, this)

  def receive = {
    case createObesity(obesity) => create(obesity, sender)
    case updateObesity(obesity) => update(obesity, sender)
    case getObesity(id) => get(id, sender)
    case getAllObesity(id) => getAll(id, sender)
    case deleteObesity(id) => delete(id, sender)
  }

  private def create(obesity: ObesityRow, requestor: ActorRef): Unit = {
    log.info("received create Obesity - " + obesity.toString())
    DB.withSession { implicit session: Session =>
      val userQuery = TableQuery[Users]
      val obesityQuery = TableQuery[Obesities] { tag: Tag =>
        new Obesities(userQuery, tag)
      }
      val obesityRepository = new ObesityRepository(obesityQuery)
      val obesityId = obesityRepository.save(obesity)
      requestor ! obesityRepository.save(obesity)
    }
  }

  private def update(obesity: ObesityRow, requestor: ActorRef): Unit = {
    log.info("received update Obesity - " + obesity.toString())
    DB.withSession { implicit session: Session =>
      val userQuery = TableQuery[Users]
      val obesityQuery = TableQuery[Obesities] { tag: Tag =>
        new Obesities(userQuery, tag)
      }
      val obesityRepository = new ObesityRepository(obesityQuery)
      val obesityOld = obesityRepository.findExistingById(obesity.id.get)
      requestor ! obesityRepository.save(obesity.copy(createDate = obesityOld.createDate))
    }
  }

  private def get(id: ObesityId, requestor: ActorRef): Unit = {
    log.info("received get Obesity - id = " + id.toString())
    DB.withSession { implicit session: Session =>
      val userQuery = TableQuery[Users]
      val obesityQuery = TableQuery[Obesities] { tag: Tag =>
        new Obesities(userQuery, tag)
      }
      val obesityRepository = new ObesityRepository(obesityQuery)
      requestor ! obesityRepository.findExistingById(id)
    }
  }

  private def getAll(id: UserId, requestor: ActorRef): Unit = {
    log.info("received getAll Obesity for user - id = " + id.toString())
    DB.withSession { implicit session: Session =>
      val userQuery = TableQuery[Users]
      val obesityQuery = TableQuery[Obesities] { tag: Tag =>
        new Obesities(userQuery, tag)
      }
      val obesityRepository = new ObesityRepository(obesityQuery)
      requestor ! obesityRepository.findForUserId(id)
    }
  }

  private def delete(id: ObesityId, requestor: ActorRef): Unit = {
    log.info("received delete Obesity - id = " + id.toString())
    DB.withSession { implicit session: Session =>
      val userQuery = TableQuery[Users]
      val obesityQuery = TableQuery[Obesities] { tag: Tag =>
        new Obesities(userQuery, tag)
      }
      val obesityRepository = new ObesityRepository(obesityQuery)
      val obesity = obesityRepository.findExistingById(id)
      requestor ! obesityRepository.save(obesity.copy(deleted = true))
    }
  }
}
