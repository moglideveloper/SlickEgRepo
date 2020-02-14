package dbio_eg

import com.typesafe.config.ConfigFactory
import dbio_eg.DbIoPersistDemo.config
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object DbIoReadDemo extends MetaDataDbIO {

  def main(args: Array[String]): Unit = {

    println("start of main")

    val config = ConfigFactory.load("application.conf")

    val query = config.getString("metadata.readQuery")

    val db = Database.forConfig("mydb", config)

    println("start reading bets data")

    val betsPrintResult: Future[Unit] = for{
      betsData <- db.run( bets(query) )
    } yield( betsData foreach( println ) )

    Await.result( betsPrintResult, Duration.Inf)

    println("end reading bets data")

    println("end of main")
  }
}
