name := "icosnetcdf"

version := "0.1"

scalaVersion := "2.11.5"

resolvers += "UNIDATA Releases" at "https://artifacts.unidata.ucar.edu/content/repositories/unidata-releases/"

libraryDependencies ++= Seq(
	"edu.ucar" % "cdm" % "4.5.4" intransitive(),
	"ch.qos.logback" % "logback-classic" % "1.1.2",
	"joda-time" % "joda-time" % "2.7",
	"net.jcip"           %  "jcip-annotations" % "1.0",
	"org.scalatest"      %  "scalatest_2.11"   % "2.2.1" % "test"	
)
