name := "playr-mongo"

organization := "26lights"

scalaVersion := "2.11.8"

resolvers += "26Lights snapshots" at "http://build.26source.org/nexus/content/repositories/public-snapshots"

resolvers += "26Lights releases" at "http://build.26source.org/nexus/content/repositories/public-releases"

libraryDependencies ++= Seq (
  "26lights"           %%  "playr"                % "0.8.1",
  "org.reactivemongo"  %%  "play2-reactivemongo"  % "0.11.11",
  "org.reactivemongo"  %%  "reactivemongo"        % "0.11.11"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

