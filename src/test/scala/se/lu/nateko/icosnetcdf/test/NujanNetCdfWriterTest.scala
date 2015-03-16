package se.lu.nateko.icosnetcdf.test

import org.scalatest.FunSuite
import se.lu.nateko.icosnetcdf.NetCdfVariableInfo
import ucar.ma2.DataType
import java.util.ArrayList
import se.lu.nateko.icosnetcdf.NetCdfSchema
import se.lu.nateko.icosnetcdf.NujanNetCdfWriter
import java.io.File
import ucar.nc2.NetcdfFile
import se.lu.nateko.icosnetcdf.PlainColumn

class NujanNetCdfWriterTest extends FunSuite{

	test("Simple NetCdf writing test"){

		val fileName = getClass.getResource("/").getFile + "simpleNujanNetCdfTest.nc"

		val var1 = new NetCdfVariableInfo("CO2", DataType.FLOAT)
		val var2 = new NetCdfVariableInfo("COMMENT", DataType.STRING)
		val vars = new ArrayList[NetCdfVariableInfo]
		vars.add(var1)
		vars.add(var2)

		val schema = new NetCdfSchema(vars, 5)

		val writer = new NujanNetCdfWriter(schema, new File(fileName))

		writer.write(0.3f, "first")
		writer.write(0.2f, "second")
		writer.write(0.4f, "third")
		writer.write(0.35f, "fourth")
		writer.write(0.25f, "fifth")
		writer.close()

		val ncFile = NetcdfFile.open(fileName)
		val co2 = ncFile.findVariable("CO2")
		val comment = ncFile.findVariable("COMMENT")
		
		val colCo2 = PlainColumn(co2).flatMap(_.asFloat).get
		val colComment = PlainColumn(comment).flatMap(_.asString).get

		assert(colCo2.values.toArray === Array(0.3f, 0.2f, 0.4f, 0.35f, 0.25f))
		assert(colComment.values.toArray === Array("first", "second", "third", "fourth", "fifth"))
	}
	
}