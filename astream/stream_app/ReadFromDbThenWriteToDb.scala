package stream_app

import akka.Done
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, SinkShape}
import akka.stream.alpakka.slick.javadsl.SlickSession
import akka.stream.alpakka.slick.scaladsl.Slick
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Sink}
import model.{Bet, PaymentMapping, UserBet}
import slick.jdbc.GetResult

import scala.concurrent.Future
import scala.io.Source

object ReadFromDbThenWriteToDb {

  def main(args: Array[String]): Unit = {

    implicit val actorSystem = ActorSystem("slickEg")
    implicit val actorMaterializer = ActorMaterializer()
    implicit val session = SlickSession.forConfig("slick-mysql")

    actorSystem.registerOnTermination(() => session.close())

    val lines = Source.fromFile("resources/mapping.conf").getLines().toSeq

    implicit val paymentMapping = PaymentMapping( lines )

    import session.profile.api._

    val getUserResult = GetResult(r => Bet(r.nextString(), r.nextString(), r.nextInt()))

    val slickQuerySource = Slick.source(sql"select source_country,country,amount from bets".as[Bet](getUserResult))

    val betToUserBetFlow = Flow[Bet].map( bet => userBetFromBet(bet) )

    def toSink(uBet : UserBet) = {
      sqlu"insert into sample VALUES(${uBet.userCountry}, ${uBet.country}, ${uBet.amount}, ${uBet.identity}, ${uBet.paymentMode})"
    }

    val sink: Sink[UserBet, Future[Done]] = Slick.sink( toSink )

    val eventualDone: Future[Done] = slickQuerySource.via( betToUserBetFlow ).log("ubet").runWith( sink )

    implicit val ec = actorSystem.dispatcher
    eventualDone.onComplete(_ => actorSystem.terminate())
  }

  def userBetFromBet(bet : Bet)(implicit paymentMapping: PaymentMapping) : UserBet = {

    val paymentSource = paymentMapping.source(bet.targetCountry)
    val userBet = UserBet( bet.fromCountry, bet.targetCountry, bet.amount, null, paymentSource )
    println(userBet)
    userBet
  }
}
