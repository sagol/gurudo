package actors

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import model.{UserId, UserRow, Users}
import play.api.Play.current
import play.api.db.slick._
import repositories.UserRepository

import scala.slick.lifted.TableQuery

/**
 * Created by sgl on 21.07.15.
 */

case class createUser(user: UserRow)
case class updateUser(user: UserRow)
case class getUser(id: UserId)
case class deleteUser(id: UserId)


class actorUsers extends Actor{

  val log = Logging(context.system, this)

  def receive = {
    case createUser(user) => create(user, sender)
    case updateUser(user) => update(user, sender)
    case getUser(id) => get(id, sender)
    case deleteUser(id) => delete(id, sender)
  }

  private def create(user: UserRow, requestor: ActorRef): Unit = {
    log.info("received create - " + user.toString())
    DB.withSession { implicit session: Session =>
      val usersQuery: TableQuery[Users] = TableQuery[Users]
      val usersRepository = new UserRepository(usersQuery)
      requestor ! usersRepository.save(user)
    }
  }

  private def update(user: UserRow, requestor: ActorRef): Unit = {
    log.info("received update - " + user.toString())
    DB.withSession { implicit session: Session =>
      val usersQuery: TableQuery[Users] = TableQuery[Users]
      val usersRepository = new UserRepository(usersQuery)
      val userOld = usersRepository findExistingById user.id.get
      requestor ! usersRepository.save(user.copy(createDate = userOld.createDate))
    }
  }

  private def get(id: UserId, requestor: ActorRef): Unit = {
    log.info("received update - id = " + id.toString())
    DB.withSession { implicit session: Session =>
      val usersQuery: TableQuery[Users] = TableQuery[Users]
      val usersRepository = new UserRepository(usersQuery)
      requestor ! usersRepository.findExistingById(id)
    }
  }

  private def delete(id: UserId, requestor: ActorRef): Unit = {
    log.info("received delete - id = " + id.toString())
    DB.withSession { implicit session: Session =>
      val usersQuery: TableQuery[Users] = TableQuery[Users]
      val usersRepository = new UserRepository(usersQuery)
      val user = usersRepository findExistingById id
      requestor ! usersRepository.save(user.copy(deleted = true))
    }
  }
}
