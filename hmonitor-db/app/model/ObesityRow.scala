package model

import org.virtuslab.unicorn.LongUnicornPlay._
import org.virtuslab.unicorn.LongUnicornPlay.driver.simple._
import org.joda.time.DateTime

case class ObesityId(id: Long) extends AnyVal with BaseId

object ObesityId extends IdCompanion[ObesityId]


case class ObesityRow(id: Option[ObesityId],
                      weight: Double,
                      bmi: Double,
                      bodyFat: Double,
                      waistSize: Int,
                      waistToHeightRatio: Double,
                      userId: UserId,
                      createDate: DateTime,
                      modifyDate: DateTime,
                      deleted: Boolean = false) extends WithId[ObesityId]

class Obesities(users: TableQuery[Users], tag: Tag) extends IdTable[ObesityId, ObesityRow](tag, "OBESITIES") {

  protected override val idColumnName = "ID"
  def weight = column[Double]("WEIGHT")
  def bmi = column[Double]("BMI")
  def bodyFat = column[Double]("BODY_FAT")
  def waistSize = column[Int]("WAIST_SIZE")
  def waistToHeightRatio = column[Double]("WAIST_TO_HIGHT_RATIO")
  def userId = column[UserId]("USER")
  def user = foreignKey("DATA_USER_FK", userId, users)(_.id)
  def createDate = column[DateTime]("CREATE_DATE")
  def modifyDate = column[DateTime]("MODIFY_DATE")
  def deleted = column[Boolean]("DELETED")

  override def * = (id.?, weight, bmi, bodyFat, waistSize, waistToHeightRatio, userId, createDate,
    modifyDate, deleted) <> (ObesityRow.tupled, ObesityRow.unapply)
}