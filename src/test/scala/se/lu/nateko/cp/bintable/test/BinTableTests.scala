package se.lu.nateko.cp.bintable.test

import org.scalatest.FunSuite
import se.lu.nateko.cp.bintable._
import java.io.File
import se.lu.nateko.cp.test.TestUtils
import se.lu.nateko.icosnetcdf.PlainColumn
import scala.util.Success

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

		val first = PlainColumn(reader.read(0, 0, 1000)).flatMap(_.asInt).get.values
		val second = PlainColumn(reader.read(1, 0, 1000)).flatMap(_.asLong).get.values

		val size = first.zip(second).size
		reader.close()

		assert(size === 1000)
	}
	
	test("Write with a string column, then read"){
		val file = TestUtils.getFileInTarget("binTableWriterStringTest.cpb")

		val schema = new Schema(Array(DataType.INT, DataType.STRING), 4)

		val writer = new BinTableWriter(file, schema)

		writer.write(1, "bla")
		writer.write(2, "bla")
		writer.write(3, "meme")
		writer.write(4, "meme")

		writer.close()

		val reader = new BinTableReader(file, schema)

		val tbl = for(
			plain1 <- PlainColumn(reader.read(0));
			int1 <- plain1.asInt;
			plain2 <- PlainColumn(reader.read(1));
			int2 <- plain2.asInt
		) yield{
			val stringCol = int2.map(reader.getStringForIndex)
			int1.values.zip(stringCol.values).toArray
		}

		reader.close()

		assert(tbl.get === Array((1, "bla"), (2, "bla"), (3, "meme"), (4, "meme")))
	}


}
