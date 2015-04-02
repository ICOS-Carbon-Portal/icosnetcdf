package se.lu.nateko.cp.csv.test

import org.scalatest.FunSpec
import java.io.StringReader
import se.lu.nateko.cp.csv.CsvRowSource
import se.lu.nateko.cp.csv.ColumnDefinition
import se.lu.nateko.cp.bintable.DataType
import se.lu.nateko.cp.csv.RowSource
import scala.collection.JavaConverters._

class CsvRowSourceTest extends FunSpec{
	
	def makeRows(csvContent: String, schema: Seq[ColumnDefinition], separator: Char = ';'): Array[Array[Object]] = {
		val reader = new StringReader(csvContent)
		val rs: RowSource = new CsvRowSource(reader, separator, schema.toArray)
		val rows = rs.asScala.toArray
		rs.close()
		rows
	}

	def getColumn[T](index: Int)(implicit rows: Array[Array[Object]]): Seq[T] = rows.map(row => row(index).asInstanceOf[T])

	describe("Eight-column CsvRowSource with semi-colon as separator - Unix and Windows new line"){
		val header = """INT;LONG;FLOAT;DOUBLE;SHORT;CHAR;BYTE;STRING"""
		
		val colDefs = Array(
			new ColumnDefinition("INT", DataType.INT),
			new ColumnDefinition("LONG", DataType.LONG),
			new ColumnDefinition("FLOAT", DataType.FLOAT),
			new ColumnDefinition("DOUBLE", DataType.DOUBLE),
			new ColumnDefinition("SHORT", DataType.SHORT),
			new ColumnDefinition("CHAR", DataType.CHAR),
			new ColumnDefinition("BYTE", DataType.BYTE),
			new ColumnDefinition("STRING", DataType.STRING)
		)

		it("performed reading columns with Unix new line correctly"){
			val content = header + "\n" + """2147483647;9223372036854775807;3.4028235E38;1.7976931348623157E308;32767;c;5;foo"""
	
			implicit val rows = makeRows(content, colDefs)

			assert(getColumn(0) === Seq(Int.MaxValue))
			assert(getColumn(1) === Seq(Long.MaxValue))
			assert(getColumn(2) === Seq(Float.MaxValue))
			assert(getColumn(3) === Seq(Double.MaxValue))
			assert(getColumn(4) === Seq(Short.MaxValue))
			assert(getColumn(5) === Seq('c'))
			assert(getColumn(6) === Seq(Byte.box(5)))
			assert(getColumn(7) === Seq("foo"))
		}
		
		
		it("performed reading columns with Windows new line correctly"){
			val content = header + "\r\n" + """2147483647;9223372036854775807;3.4028235E38;1.7976931348623157E308;32767;c;5;foo"""
			
			implicit val rows = makeRows(content, colDefs)
			
			assert(getColumn(0) === Seq(Int.MaxValue))
			assert(getColumn(1) === Seq(Long.MaxValue))
			assert(getColumn(2) === Seq(Float.MaxValue))
			assert(getColumn(3) === Seq(Double.MaxValue))
			assert(getColumn(4) === Seq(Short.MaxValue))
			assert(getColumn(5) === Seq('c'))
			assert(getColumn(6) === Seq(Byte.box(5)))
			assert(getColumn(7) === Seq("foo"))
		}
	}
	
	
	describe("Malformed csv scenario"){
		val header = """header"""

		it("should produce a NumberFormatException for type INT when exceeding max value"){
			val content = header + "\n" + """2147483648"""
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.INT)
			)
			
			intercept[NumberFormatException] {
				implicit val rows = makeRows(content, colDefs)				
			}
		}
		
		it("should produce a NumberFormatException for type FLOAT when exceeding max value"){
			val content = header + "\n" + """3.4028236E38"""
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.FLOAT)
			)
			
			intercept[NumberFormatException] {
				implicit val rows = makeRows(content, colDefs)				
			}
		}
		
		it("should produce a NumberFormatException for type DOUBLE when exceeding max value"){
			val content = header + "\n" + """2.7976931348623157E308"""
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.DOUBLE)
			)
			
			intercept[NumberFormatException] {
				implicit val rows = makeRows(content, colDefs)				
			}
		}
		
		it("should produce a NumberFormatException for type LONG when exceeding max value"){
			val content = header + "\n" + """9223372036854775808"""
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.LONG)
			)
			
			intercept[NumberFormatException] {
				implicit val rows = makeRows(content, colDefs)				
			}
		}
		
		it("should produce a NumberFormatException for type BYTE when illegal value is provided"){
			val content = header + "\n" + """d"""
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.BYTE)
			)
			
			intercept[NumberFormatException] {
				implicit val rows = makeRows(content, colDefs)				
			}
		}
		
		it("should produce a NumberFormatException for type SHORT when exceeding max value"){
			val content = header + "\n" + """32768"""
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.SHORT)
			)
			
			intercept[NumberFormatException] {
				implicit val rows = makeRows(content, colDefs)				
			}
		}
		
		it("should produce a IllegalArgumentException for type CHAR when exceeding one character"){
			val content = header + "\n" + """vv"""
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.CHAR)
			)
			
			intercept[IllegalArgumentException] {
				implicit val rows = makeRows(content, colDefs)				
			}
		}
		
		it("should produce a NoSuchElementException when header mismatch"){
			val content = header + "\n" + """1"""
			
			val colDefs = Array(
				new ColumnDefinition("wrong_header", DataType.INT)
			)
			
			intercept[NoSuchElementException] {
				implicit val rows = makeRows(content, colDefs)				
			}
		}
	}
}