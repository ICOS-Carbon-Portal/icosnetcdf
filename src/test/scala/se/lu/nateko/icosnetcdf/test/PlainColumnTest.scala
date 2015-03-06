package se.lu.nateko.icosnetcdf.test

import org.scalatest.FunSuite
import ucar.nc2.NetcdfFile
import se.lu.nateko.icosnetcdf.PlainColumn
import se.lu.nateko.icosnetcdf.FloatColumn

class PlainColumnTest extends FunSuite {

	test("It is possible to read PlainColumn from file"){
		val url = getClass.getResource("/newNetCDF.nc")
		val file = NetcdfFile.open(url.getFile)

		val ch4 = file.findVariable("ch4")
		
		val columnTry = PlainColumn(ch4).flatMap(_.asFloat)
		
		assert(columnTry.isSuccess)
		
		val column = columnTry.get
		
		//println(column.values.sum)
		
		assert(column.values.length == 7660)
		
		file.close()
	}
}