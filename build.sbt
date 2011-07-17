name := "dodoop"

organization := "org.technbolts"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.9.0-1"

scalacOptions ++= Seq("-unchecked", "-deprecation")

retrieveManaged := true // remove this once plugins are working or i understand their layout

publishMavenStyle := true

publishTo := Some(Resolver.file("Local", Path.userHome / "Projects" / "arnauld.github.com" / "maven2" asFile)(Patterns(true, Resolver.mavenStyleBasePattern)))

resourceGenerators in Compile <+= resourceManaged in Compile map { dir =>
  val file = dir / "build.properties"
  IO.write(file, """buildAt="""+(new java.util.Date()))
  Seq(file)
}

javaOptions in (run) += "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

libraryDependencies ++= Seq(
  // logs
  "org.slf4j" % "slf4j-api" % "1.6.0",
  "ch.qos.logback" % "logback-classic" % "0.9.25",
  //test
  "org.scala-tools.testing" %% "specs" % "1.6.8" % "test"
)

resolvers ++= Seq(
  "Sonatype OSS" at "http://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Arnauld" at "https://github.com/Arnauld/arnauld.github.com/raw/master/maven2",
  "java net" at "http://download.java.net/maven/2/"
)