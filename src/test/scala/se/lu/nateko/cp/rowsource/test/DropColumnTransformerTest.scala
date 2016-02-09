package se.lu.nateko.cp.rowsource.test

import org.scalatest.FunSpec
import se.lu.nateko.cp.rowsource.ColumnDefinition
import se.lu.nateko.cp.rowsource.DataType
import se.lu.nateko.cp.rowsource.ArrayRowSource
import scala.collection.JavaConversions._

class DropColumnTransformerTest extends FunSpec{

	val rows: Array[Array[Object]] = Array(
		Array("a1", Int.box(34), Double.box(math.Pi)),
		Array("a2", Int.box(42), Double.box(math.E))
	)
	
	val schema = Array(
		new ColumnDefinition("A", DataType.STRING),
		new ColumnDefinition("B", DataType.INT),
		new ColumnDefinition("C", DataType.DOUBLE)
	)
	
	val rowSource = new ArrayRowSource(schema, rows)

	describe("Dropping a single existing column"){
		it("correctly removes middle column from rows"){
			val actualRows = rowSource.drop("B").toArray
			val expectedRows = rows.map{
				case Array(a, _, c) => Array(a, c)
			}
			assert(actualRows === expectedRows)
		}
		
		it("correctly removes middle column from the schema"){
			val actualSchema = rowSource.drop("B").getSchema
			val expectedSchema = schema match{
				case Array(a, _, c) => Array(a, c)
			}
			assert(actualSchema === expectedSchema)
		}
	}
}