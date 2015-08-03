package model

import org.joda.time.DateTime
import org.virtuslab.unicorn.LongUnicornPlay._
import org.virtuslab.unicorn.LongUnicornPlay.driver.simple._

/** Id class for type-safe joins and queries. */
case class UserId(id: Long) extends AnyVal with BaseId

/** Companion object for id class, extends IdCompanion
  * and brings all required implicits to scope when needed.
  */

object UserId extends IdCompanion[UserId]

/** User entity.
  *
  * @param id user id
  * @param externalid external user id
  * @param email user email address
  * @param lastName lastName
  * @param firstName firstName
  * @param middleName middleName
  * @param createDate creation date
  * @param modifyDate modification date
  * @param deleted flag of the deletion
  */

case class UserRow(id: Option[UserId],
                   externalid: String,
                   email: String,
                   firstName: String,
                   lastName: String,
                   middleName: String,
                   createDate: DateTime,
                   modifyDate: DateTime,
                   deleted: Boolean = false) extends WithId[UserId]

/** Table definition for users. */
class Users(tag: Tag) extends IdTable[UserId, UserRow](tag, "USERS") {

  /** By definition id column is inserted as lowercase 'id',
    * if you want to change it, here is your setting.
    */
  protected override val idColumnName = "ID"
  def externalid = column[String]("EXTERNALID")
  def email = column[String]("EMAIL")
  def firstName = column[String]("FIRST_NAME")
  def lastName = column[String]("LAST_NAME")
  def middleName = column[String]("MIDDLE_NAME")
  def createDate = column[DateTime]("CREATE_DATE")
  def modifyDate = column[DateTime]("MODIFY_DATE")
  def deleted = column[Boolean]("DELETED")

  override def * = (id.?, externalid, email, firstName, lastName,
    middleName, createDate, modifyDate, deleted) <>(UserRow.tupled, UserRow.unapply)

}
