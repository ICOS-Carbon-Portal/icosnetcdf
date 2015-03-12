package se.lu.nateko.icosnetcdf.test

import org.scalatest.FunSuite
import java.io.File
import se.lu.nateko.icosnetcdf._
import ucar.ma2.DataType
import java.util.ArrayList
import ucar.nc2.NetcdfFile
import java.util.Calendar;
import java.util.Hashtable;
import scala.collection.JavaConverters._

class NetCdfWriterTest extends FunSuite {

	test("Simple NetCdf writing test"){
		
		val fileName = getClass.getResource("/").getFile + "simpleNetCdfTest.nc"
		
		val var1 = new NetCdfVariableInfo("CO2", DataType.FLOAT, "CO2", "mol/m3", "Concentration meassured in mol per cubic meters")
		val var2 = new NetCdfVariableInfo("DAY", DataType.INT, "time steps", "hours since 2010-01-01 00:00:00", "time steps (hours) since 2010-01-01 00:00:00 (UTC)")
		val vars = new ArrayList[NetCdfVariableInfo]
		vars.add(var1)
		vars.add(var2)

		val globalAttributes = Map("origin" -> "This file is generated in test", "created" -> "Created today")
		
		val schema = new NetCdfSchema(vars, 5, globalAttributes.asJava)
		//val schema = new NetCdfSchema(vars, 5)
		
		val writer = new WriteNetCDF(schema, new File(fileName))
		
		def write(co2: Float, day: Int) = writer.writeRow(Array(co2.asInstanceOf[Object], day.asInstanceOf[Object]))

		write(0.3f, 1)
		write(0.2f, 2)
		write(0.4f, 3)
		write(0.35f, 4)
		write(0.25f, 5)
		writer.close()
		
		val ncFile = NetcdfFile.open(fileName)
		val co2 = ncFile.findVariable("CO2")
		val day = ncFile.findVariable("DAY")
		
		val colCo2 = PlainColumn(co2).flatMap(_.asFloat).get
		val colDay = PlainColumn(day).flatMap(_.asInt).get
		
		assert(colCo2.values.toArray === Array(0.3f, 0.2f, 0.4f, 0.35f, 0.25f))
		assert(colDay.values.toArray === Array(1, 2, 3, 4, 5))
	}
	
	test("Test method writeRow(Object[] row: Data types)"){
		val fileName = getClass.getResource("/").getFile + "writeRowTest.nc"
		
		val var1 = new NetCdfVariableInfo("varFloat", DataType.FLOAT)
		val var2 = new NetCdfVariableInfo("varDouble", DataType.DOUBLE)
		val var3 = new NetCdfVariableInfo("varInt", DataType.INT)
		//Long is not supported in netcdf-3
		//val var4 = new NetCdfVariableInfo("varLong", DataType.LONG)
		val vars = new ArrayList[NetCdfVariableInfo]
		vars.add(var1)
		vars.add(var2)
		vars.add(var3)
		//vars.add(var4)

		val schema = new NetCdfSchema(vars, 1)
		
		val writer = new WriteNetCDF(schema, new File(fileName))
		
		def write(varFloat: Float, varDouble: Double, varInt: Int) = writer.writeRow(Array(
				varFloat.asInstanceOf[Object],
				varDouble.asInstanceOf[Object],
				varInt.asInstanceOf[Object]))

		write(0.3f, 0.4, 1)

		writer.close()
	}
	
	test("Test converting NetCDF LIMITED to UNLIMITED"){
		val ncFileNameIn = getClass.getResource("/").getFile + "simpleNetCdfTest.nc"
		val ncFileNameOut = getClass.getResource("/").getFile + "simpleNetCdfTestUNLIMITED.nc"
		
		val ncfile = NetcdfFile.open(ncFileNameIn)
		val variables = ncfile.getVariables()
		val vars = new ArrayList[NetCdfVariableInfo]
		
		for (i <- 0 to variables.size() - 1){
			val v = variables.get(i)
			val varInfo = new NetCdfVariableInfo(v.getShortName(), v.getDataType())
			vars.add(varInfo)
		}
		
		val schema = new NetCdfSchema(vars)
		
		val writer = new WriteNetCDF(schema, new File(ncFileNameOut))
		
		for (i <- 0 to variables.size() - 1){
			val v = variables.get(i)
			writer.writeArray(v.read(), i)
		}
		
		writer.close()
		
		val ncfileCopy = NetcdfFile.open(ncFileNameOut)
		
		assert(ncfile.getVariables().size() == ncfileCopy.getVariables().size())
		assert(ncfile.getDimensions().size() == ncfileCopy.getDimensions().size())
		
		for (i <- 0 to variables.size() - 1){
			assert(ncfile.getVariables().get(i).getDataType === ncfileCopy.getVariables().get(i).getDataType)
			assert(ncfile.getVariables().get(i).getShape() === ncfileCopy.getVariables().get(i).getShape())
		}
		
		ncfile.close()
		ncfileCopy.close()
	}
	
	test("Test converting NetCDF UNLIMITED to LIMITED"){
		val ncFileNameIn = getClass.getResource("/").getFile + "simpleNetCdfTestUNLIMITED.nc"
		val ncFileNameOut = getClass.getResource("/").getFile + "simpleNetCdfTestLIMITED.nc"
		
		val ncfile = NetcdfFile.open(ncFileNameIn)
		val variables = ncfile.getVariables()
		val vars = new ArrayList[NetCdfVariableInfo]
		
		for (i <- 0 to variables.size() - 1){
			val v = variables.get(i)
			val varInfo = new NetCdfVariableInfo(v.getShortName(), v.getDataType())
			vars.add(varInfo)
		}
		
		val schema = new NetCdfSchema(vars, ncfile.getDimensions().get(0).getLength())
		
		val writer = new WriteNetCDF(schema, new File(ncFileNameOut))
		
		for (i <- 0 to variables.size() - 1){
			val v = variables.get(i)
			writer.writeArray(v.read(), i)
		}
		
		writer.close()
		
		val ncfileCopy = NetcdfFile.open(ncFileNameOut)
		
		assert(ncfile.getVariables().size() == ncfileCopy.getVariables().size())
		assert(ncfile.getDimensions().size() == ncfileCopy.getDimensions().size())
		
		for (i <- 0 to variables.size() - 1){
			assert(ncfile.getVariables().get(i).getDataType === ncfileCopy.getVariables().get(i).getDataType)
			assert(ncfile.getVariables().get(i).getShape() === ncfileCopy.getVariables().get(i).getShape())
		}
		
		ncfile.close()
		ncfileCopy.close()
	}

}