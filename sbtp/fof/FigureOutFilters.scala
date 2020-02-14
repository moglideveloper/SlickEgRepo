package fof

import fof._
import sbt._

object FigureOutFilters {

  def main(args: Array[String]): Unit = {


    val distDir = new File("/Users/riteshverma/gitrepos/stech/AkkaLib/BankEg/target/pack")

    val excludeFiles = Set("**/resources/*.*", "**/sqls/*.*")

    Option(distDir.listFiles)
      .getOrElse(Array.empty)
      .filterNot(f => excludeFiles.contains(rpath(distDir, f)))
      .foreach( println )
  }
}
