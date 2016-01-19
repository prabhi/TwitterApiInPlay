name := "TwitterAPI"

version := "1.0"

lazy val `twitterapi` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++=
  Seq(
    jdbc
    ,cache
    ,ws
    ,"io.spray"                   %%  "spray-can"              % "1.3.3"
    , "io.spray"                   %%  "spray-routing"          % "1.3.3"
    , "io.spray"                   %%  "spray-client"           % "1.3.3"
    , "io.spray"                   %%  "spray-json"             % "1.3.1"
    , "io.spray"                   %%  "spray-testkit"          % "1.3.3"   % "test"
    , "ch.qos.logback"              %  "logback-classic"        % "1.1.3"
    , "com.typesafe.scala-logging" %%  "scala-logging"          % "3.1.0"
    , "com.typesafe.akka"          %%  "akka-actor"             % "2.3.12"
    , "com.typesafe.akka"          %%  "akka-slf4j"             % "2.3.12"
    , "com.typesafe.akka"          %%  "akka-testkit"           % "2.3.12"    % "test"
    , "com.typesafe.slick"         %%  "slick"                  % "3.0.0"
     ,"mysql"                       % "mysql-connector-java"                % "5.1.24"
  )


routesGenerator := InjectedRoutesGenerator


unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

resolvers ++= Seq(
  "Maven Releases" at "http://repo.typesafe.com/typesafe/maven-releases"
  , "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
)