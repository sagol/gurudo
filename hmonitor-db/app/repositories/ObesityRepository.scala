package repositories

import org.virtuslab.unicorn.LongUnicornPlay._
import org.virtuslab.unicorn.LongUnicornPlay.driver.simple._
import scala.slick.lifted.TableQuery
import model.{ObesityId, ObesityRow, Obesities}


class ObesityRepository(query: TableQuery[Obesities])
  extends BaseIdRepository[ObesityId, ObesityRow, Obesities](query) {

  import model.UserId
  // parametrized query using UserId
  private val obesitiesForUserQuery = for {
    userId <- Parameters[UserId]
    // type-safe join, if you pass here any other id or Long it wont compile!
    obesity <- query if obesity.userId === userId
  } yield obesity

  final def findForUserId(userId: UserId)(implicit session: Session): Seq[ObesityRow] =
    obesitiesForUserQuery(userId).list

}
