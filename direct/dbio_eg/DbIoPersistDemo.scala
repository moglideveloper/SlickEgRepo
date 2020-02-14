package dbio_eg

import com.typesafe.config.ConfigFactory
import model.{Bet, PaymentMapping, UserBet}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.util.{Success, Try}

object DbIoPersistDemo extends MetaDataDbIO {

  val config = ConfigFactory.load()
  val rootDir = Option(System.getProperty("PROG_ROOT")).getOrElse( System.getProperty("user.dir") )

  println(s"root directory is >$rootDir<")

  val db = Database.forConfig("mydb", config)

  def main(args: Array[String]): Unit = {

    println("start of main")

    for(arg <- args){
      arg.trim.toLowerCase match {
        case "t" => truncateTableSync()
        case "d" =>
          val _@(dbOperationStatus, dbOperationReturnValue) = unmappedOnDbWire()
          (dbOperationStatus, dbOperationReturnValue)match {
            case (true, Success( Some( (dbWriteStatus, unmapped) ) ) ) => unmapped.foreach( e => println(s"e is $e") )
            case _ => println(dbOperationReturnValue)
          }

        case "c" =>
          val query = config.getString("metadata.readQuery")
          println("query is :-")
          println(query)

        case unsupportedArg => println(s"unsupported argument >$unsupportedArg<. supported args are >t, d, c<")
      }
    }

    //TODO : send mail asynchronously here

    println("end reading and writing bets data")

    println("end of main")
  }

  private def unmappedOnDbWire()  = {
    println("start reading and writing bets data")

    val mappingFile = rootDir + "/" + config.getString("mappingFile")

    println(s"mapping file is >$mappingFile<")

    val lines = Source.fromFile(mappingFile).getLines().toSeq

    implicit val paymentMapping = PaymentMapping(lines)

    val userBetsTable = TableQuery[UsersBetTable]

    val tableStr = config.getString("metadata.readQuery")

    val op: Future[(Seq[Int], Seq[UserBet])] = for {

      betsData <- db.run(bets(tableStr))

      userBetsData = betsData.map(userBetFromBet)

      listOfDbios = userBetsData map { ub => userBetsTable += ub }

      dbioOfList = DBIO.sequence(listOfDbios)

      result <- db.run(dbioOfList)

      unmappedPaymentRecords = userBetsData.filter(ubet => ubet.paymentMode.trim.isEmpty)

    } yield (result, unmappedPaymentRecords)

    Await.result(op, Duration.Inf)

    println("op status is : " + op.isCompleted)
    println("op unmapped entries are :-")
    (op.isCompleted, Try { op.value.map(_.get) })
  }

  def truncateTableSync() : Unit = {

    val tableName = config.getString("metadata.truncateQuery")

    Await.result( db.run( sqlu"truncate table #$tableName"), Duration.Inf)
  }

  def userBetFromBet(bet : Bet)(implicit paymentMapping: PaymentMapping) : UserBet = {

    val paymentSource = paymentMapping.source(bet.targetCountry)
    val userBet = UserBet( bet.fromCountry, bet.targetCountry, bet.amount, null, paymentSource )
    userBet
  }
}

