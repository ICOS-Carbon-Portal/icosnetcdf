name := "icosnetcdf"

version := "0.1"

scalaVersion := "2.11.5"

resolvers += "UNIDATA Releases" at "https://artifacts.unidata.ucar.edu/content/repositories/unidata-releases/"

libraryDependencies ++= Seq(
	"edu.ucar" % "cdm" % "4.5.2",
	"ch.qos.logback" % "logback-classic" % "1.1.2"
)