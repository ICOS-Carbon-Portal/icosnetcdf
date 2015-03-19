package se.lu.nateko.cp.bintable.test

import org.scalatest.FunSuite
import se.lu.nateko.cp.bintable._
import java.io.File
import se.lu.nateko.cp.test.TestUtils
import se.lu.nateko.icosnetcdf.PlainColumn

class BinTableTest extends FunSuite{

	test("Simple write, then read test"){
		val file = TestUtils.getFileInTarget("binTableWriterTest.cpb")

		val n = 100000

		val schema = new Schema(Array(DataType.INT, DataType.LONG), n)

		val writer = new BinTableWriter(file, schema)

		for(i <- 1 to n){
			writer.write(i, i.toLong << 16)
		}

		writer.close()

		val reader = new BinTableReader(file, schema)

		val first = PlainColumn(reader.read(0)).flatMap(_.asInt).get.values
		val second = PlainColumn(reader.read(1)).flatMap(_.asLong).get.values

		val size = first.zip(second).size
		reader.close()

		assert(size === n)
	}

}