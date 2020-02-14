package dbio_eg

import model.{Bet, UserBet}
import slick.jdbc.GetResult
import slick.jdbc.MySQLProfile.api._

class MetaDataDbIO {
  def bets(str : String): DBIO[Seq[Bet]] = {

    println("sql string is :-")
    println(str)

    implicit val getUserResult = GetResult(r => Bet(r.nextString(), r.nextString(), r.nextInt()))

    sql"select * from #$str".as[Bet]
  }

}

class UsersBetTable(tag: Tag) extends Table[(UserBet)](tag, "sample") {
  def userCountry = column[String]("userCountry")
  def country = column[String]("country")
  def amount = column[Int]("amount")
  def identity = column[String]("identity")
  def paymentMode = column[String]("paymentMode")


  def * = (userCountry, country, amount, identity, paymentMode) <> (UserBet.tupled, UserBet.unapply)
}
