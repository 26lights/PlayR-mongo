name := "playr-mongo-demo"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.2"

scalacOptions += "-language:reflectiveCalls"

libraryDependencies += "26lights"  %% "playr-swagger"  % "0.4.2"

lazy val playrMongo = RootProject(file("../.."))

lazy val root = project in file(".") dependsOn playrMongo enablePlugins PlayScala

