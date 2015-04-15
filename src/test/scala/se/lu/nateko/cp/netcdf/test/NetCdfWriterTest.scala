package se.lu.nateko.cp.netcdf.test

import org.scalatest.FunSuite
import java.io.File
import se.lu.nateko.cp.netcdf._
import ucar.ma2.DataType
import ucar.nc2.NetcdfFileWriter
import java.util.ArrayList
import ucar.nc2.NetcdfFile
import java.util.Calendar
import java.util.Hashtable
import scala.collection.JavaConverters._
import se.lu.nateko.cp.netcdf.WriteNetCDF.ncVersion
import se.lu.nateko.cp.test.TestUtils._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class NetCdfWriterTest extends FunSuite {

	ignore("Simple NetCdf writing test"){
		
		val fileName = getClass.getResource("/").getFile + "simpleNetCdfTest.nc"
		
		val vars = List(
			new NetCdfVariableInfo("CO2", DataType.INT, "CO2", "mol/m3", "Concentration meassured in mol per cubic meters"),
			new NetCdfVariableInfo("DAY", DataType.INT, "time steps", "hours since 2010-01-01 00:00:00", "time steps (hours) since 2010-01-01 00:00:00 (UTC)")
		)

		val globalAttributes = Map("origin" -> "This file is generated in test", "created" -> "Created today")

		val schema = new NetCdfSchema(vars.asJava)//, 5, globalAttributes.asJava)

		val writer = new WriteNetCDF(schema, new File(fileName), ncVersion.NC3)

		writer.write(17, 34)
		writer.write(17, 34)
		writer.write(17, 34)
		writer.write(17, 34)
		writer.write(17, 34)
		writer.close()

		val ncFile = NetcdfFile.open(fileName)
		val co2 = ncFile.findVariable("CO2")
		val day = ncFile.findVariable("DAY")
		
		val colCo2 = PlainColumn(co2).flatMap(_.asInt).get
		val colDay = PlainColumn(day).flatMap(_.asInt).get

		assert(colCo2.values.toArray === Array(17, 17, 17, 17, 17))
		assert(colDay.values.toArray === Array(34, 34, 34, 34, 34))
	}
	
	ignore("Test method writeRow(Object[] row: Data types)"){
		val fileName = getClass.getResource("/").getFile + "writeRowTest.nc"

		val vars = List(new NetCdfVariableInfo("varFloat", DataType.FLOAT),
			new NetCdfVariableInfo("varDouble", DataType.DOUBLE),
			new NetCdfVariableInfo("varInt", DataType.INT)
			//Long is not supported in netcdf-3
			//new NetCdfVariableInfo("varLong", DataType.LONG)
		)

		val schema = new NetCdfSchema(vars.asJava, 1)

		val writer = new WriteNetCDF(schema, new File(fileName), ncVersion.NC3)

		writer.write(0.3f, 0.4, 1)

		writer.close()
	}
	
	ignore("Test converting NetCDF LIMITED to UNLIMITED"){
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
		
		val writer = new WriteNetCDF(schema, new File(ncFileNameOut), ncVersion.NC3)
		
		for (i <- 0 to variables.size - 1){
			val v = variables.get(i)
			writer.writeArray(v.read(), i)
		}
		
		writer.close()
		
		val ncfileCopy = NetcdfFile.open(ncFileNameOut)

		assert(ncfile.getVariables.size == ncfileCopy.getVariables.size)
		assert(ncfile.getDimensions.size == ncfileCopy.getDimensions.size)

		for (i <- 0 to variables.size - 1){
			assert(ncfile.getVariables.get(i).getDataType === ncfileCopy.getVariables.get(i).getDataType)
			assert(ncfile.getVariables.get(i).getShape === ncfileCopy.getVariables.get(i).getShape)
		}

		ncfile.close()
		ncfileCopy.close()
	}
	
	ignore("Test converting NetCDF UNLIMITED to LIMITED"){
		val ncFileNameIn = getClass.getResource("/").getFile + "simpleNetCdfTestUNLIMITED.nc"
		val ncFileNameOut = getClass.getResource("/").getFile + "simpleNetCdfTestLIMITED.nc"
		
		val ncfile = NetcdfFile.open(ncFileNameIn)
		val variables = ncfile.getVariables()
		
		val vars = variables.asScala.map(v => new NetCdfVariableInfo(v.getShortName, v.getDataType)).asJava
		
		val schema = new NetCdfSchema(vars, ncfile.getDimensions.get(0).getLength)
		
		val writer = new WriteNetCDF(schema, new File(ncFileNameOut), ncVersion.NC3)
		
		for (i <- 0 to variables.size - 1){
			val v = variables.get(i)
			writer.writeArray(v.read(), i)
		}
		
		writer.close()
		
		val ncfileCopy = NetcdfFile.open(ncFileNameOut)
		
		assert(ncfile.getVariables.size == ncfileCopy.getVariables.size)
		assert(ncfile.getDimensions.size == ncfileCopy.getDimensions.size)
		
		for (i <- 0 to variables.size() - 1){
			assert(ncfile.getVariables().get(i).getDataType === ncfileCopy.getVariables().get(i).getDataType)
			assert(ncfile.getVariables().get(i).getShape() === ncfileCopy.getVariables().get(i).getShape())
		}
		
		ncfile.close()
		ncfileCopy.close()
	}
	
	ignore("Generate large test files"){
		val fileName = "/disk/ICOS/NetCDF_test/create/largeTestDataLconst.nc4"
		val limit = 10000000
		
		val vars = List(
			new NetCdfVariableInfo("var1", DataType.FLOAT),
			new NetCdfVariableInfo("var2", DataType.DOUBLE),
			new NetCdfVariableInfo("var3", DataType.INT)
		).asJava
		
		val schema = new NetCdfSchema(vars, limit)
		
		val writer = new WriteNetCDF(schema, new File(fileName))
		
		val rnd = new java.util.Random
		
		for (i <- 1 to limit){
			writer.write(1.0f, 2.0, 3)
		}

		writer.close()
		
		val ncFileNameOut = "/disk/ICOS/NetCDF_test/create/largeTestDataLrnd.nc4"
		
		val schema2 = new NetCdfSchema(vars, limit)
		
		val writer2 = new WriteNetCDF(schema2, new File(ncFileNameOut))
		
		for (i <- 1 to limit){
			writer2.write(rnd.nextFloat, rnd.nextDouble, rnd.nextInt)
		}

		writer2.close()
	}
	
	ignore("Multithredding"){
		
		val varName = "var2"
		val slice = "1000000:1500000"
		
		val fileName1 = "/disk/ICOS/NetCDF_test/create/largeTestDataLrnd.nc"
		val fileName2 = "/disk/ICOS/NetCDF_test/create/largeTestDataLrnd.nc4"
		
		def readNetcdf(fileName: String): Unit = {
			val ncfile = NetcdfFile.open(fileName)
	
			val v = ncfile.findVariable(varName)
			val data = v.read(slice)
			
			ncfile.close()
		}
		
		def loanNetcdf[T](fileName: String)(calc: NetcdfFile => T): T = {
			val ncfile = NetcdfFile.open(fileName)
			val res: T = calc(ncfile)
			ncfile.close()
			res
		}
		
		val data = loanNetcdf(fileName1){ncfile =>
			val v = ncfile.findVariable(varName)
			v.read(slice)
		}
		
		val readConstantData = time {readNetcdf(fileName1)}
		println(s"$fileName1: $readConstantData milliseconds")
		
		val readRndData = time {readNetcdf(fileName2)}
		println(s"$fileName2: $readRndData milliseconds")
		
		val readTwoFiles = time{
			val fileConst: Future[Unit] = Future{
				readNetcdf(fileName1)
			}
			
			val fileRnd: Future[Unit] = Future{
				readNetcdf(fileName2)
			}
			
			val twoFilesRead = for(
				number2 <- fileRnd;
				number1 <- fileConst
			)yield number1 
		
			Await.result(twoFilesRead, Duration.Inf)
		}
		
		println(s"twoFilesRead: $readTwoFiles milliseconds")
		
	}

}