package model

case class PaymentMapping(map : Map[String, String]){
  def source(key : String) : String = map.getOrElse(key, "")
}

object PaymentMapping{
  def apply(lines : Seq[String]) : PaymentMapping = {
    val map = lines map { line => line.split(",") } map { arr => ( arr(0).trim -> arr(1).trim ) } toMap
    val paymentMapping =  new PaymentMapping(map)
    paymentMapping
  }
}

case class UserBet(userCountry : String, country : String, amount : Int, identity : String, paymentMode : String)

case class Bet(fromCountry : String, targetCountry : String, amount : Int){
  override def toString: String = {
    s"$fromCountry, $targetCountry, $amount"
  }
}

