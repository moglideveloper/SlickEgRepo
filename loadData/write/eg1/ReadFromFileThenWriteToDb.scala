package write.eg1

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.slick.javadsl.SlickSession
import akka.stream.alpakka.slick.scaladsl.Slick
import akka.stream.scaladsl.{FileIO, Flow, Framing}
import akka.util.ByteString
import model.Bet

object ReadFromFileThenWriteToDb {

  def main(args: Array[String]): Unit = {

    approach1()
  }

  private def toBet(line: String) : Bet = {

    val tokens = line.split(",")

    val bet = Bet(tokens(0).trim, tokens(1).trim, tokens(2).trim.toInt)
    bet
  }

  def approach1(): Unit = {
    implicit val actorSystem = ActorSystem("slickEg")
    implicit val actorMaterializer = ActorMaterializer()
    implicit val session = SlickSession.forConfig("slick-mysql")
    actorSystem.registerOnTermination(session.close())

    val oneKbBetsFilePathString = sys.env("DATA_EXTRACT_DIR") + "/bets/oneKb/icc_wc_bets/oneKbBets.txt"

    println(s"one kb file path string is >${oneKbBetsFilePathString}<")

    val fileSource = FileIO.fromPath( Paths.get(oneKbBetsFilePathString) ).via(Framing.delimiter(ByteString("\n"), 256, true).map(_.utf8String))

    import session.profile.api._

    def toSink(bet :  Bet) = {
      sqlu"insert into bets VALUES(${bet.fromCountry}, ${bet.targetCountry}, ${bet.amount})"
    }

    val eventualResult = fileSource.map(toBet).log("bet").runWith( Slick.sink( toSink ) )

    implicit val ec = actorSystem.dispatcher
    eventualResult.onComplete(_ => actorSystem.terminate())
  }
}