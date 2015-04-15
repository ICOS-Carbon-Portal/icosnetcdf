package se.lu.nateko.cp.netcdf.test

import org.scalatest.FunSuite
import ucar.nc2.NetcdfFile
import se.lu.nateko.cp.netcdf.PlainColumn
import se.lu.nateko.cp.netcdf.FloatColumn
import java.util.ArrayList

class PlainColumnTest extends FunSuite {

	test("It is possible to read PlainColumn from file"){
    
		val url = getClass.getResource("/newNetCDF.nc")
		val file = NetcdfFile.open(url.getFile)

		val ch4 = file.findVariable("ch4")
		
		val columnTry = PlainColumn(ch4).flatMap(_.asFloat)
		
		assert(columnTry.isSuccess)
		
		val column = columnTry.get
		
    //column.values.foreach(println)
    
		//println(column.values.sum)
		
		assert(column.values.length == 7660)
		
		file.close()
	}
  
}

