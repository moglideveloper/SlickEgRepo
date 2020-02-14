libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.47"

//for stream style
libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "1.1.2"

//for direct
libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "org.slf4j" % "slf4j-nop" % "1.6.4"
)

lazy val slickEg = (project in file("."))

val testSourceDirectories = Seq("loadData", "astream", "direct")
unmanagedSourceDirectories in Compile ++= testSourceDirectories.map { src => (baseDirectory in Compile).value / src }

val rsrcs = List("config")
unmanagedResourceDirectories in Compile ++= rsrcs.map { res => (baseDirectory in Compile).value / res }

//sbt pack configuration starts here

packMain := Map("migration" -> "dbio_eg.DbIoPersistDemo")

packResourceDir += (baseDirectory.value / "config" -> "config")

val excludeFileRegx = """(.*?)\.(properties|props|conf|dsl|txt|xml|sql)$""".r

mappings in (Compile, packageBin) ~= { (ms: Seq[(File, String)]) =>
  ms filter {
    case (file, toPath) => {
      val shouldExclude = excludeFileRegx.pattern.matcher(file.getName).matches
      // println("===========" + file + "  " + shouldExclude)
      !shouldExclude
    }
  }
}

packJvmOpts := Map("migration" -> Seq("-Dconfig.file=${PROG_HOME}/config/application.conf -DPROG_ROOT=${PROG_HOME}"))

//sbt pack configuration ends here

enablePlugins(PackPlugin)

