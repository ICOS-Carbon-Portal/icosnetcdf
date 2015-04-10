package se.lu.nateko.cp.csv.test

import org.scalatest.FunSpec
import java.io.StringReader
import se.lu.nateko.cp.bintable.DataType
import se.lu.nateko.cp.csv.CsvRowSource
import se.lu.nateko.cp.rowsource.ColumnDefinition
import se.lu.nateko.cp.rowsource.RowSource
import scala.collection.JavaConverters._
import se.lu.nateko.cp.csv.exceptions._

class CsvRowSourceTest extends FunSpec{
	
	def makeRows(csvContent: String, schema: Seq[ColumnDefinition], separator: Char = ';', locale: java.util.Locale = java.util.Locale.ENGLISH): Array[Array[Object]] = {
		val reader = new StringReader(csvContent)
		val rs: RowSource = new CsvRowSource(reader, separator, schema.toArray, locale)
		val rows = rs.asScala.toArray
		rs.close()
		rows
	}

	def getColumn[T](index: Int)(implicit rows: Array[Array[Object]]): Seq[T] = rows.map(row => row(index).asInstanceOf[T])

	describe("Eight-column CsvRowSource with semi-colon as separator - Unix and Windows new line"){
		val header = "INT;LONG;FLOAT;DOUBLE;SHORT;CHAR;BYTE;STRING"
		
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
			val content = header + "\n" + "2147483647;9223372036854775807;3.4028235E38;1.7976931348623157E308;32767;c;5;foo"
	
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
			val content = header + "\r\n" + "2147483647;9223372036854775807;3.4028235E38;1.7976931348623157E308;32767;c;5;foo"
			
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
		
		val head = "header"
		it("should be able to handle ',' as decimal character for FLOAT"){
			val content = head + "\n" + "1,2"
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.FLOAT)
			)
			
			implicit val rows = makeRows(content, colDefs, ';', java.util.Locale.FRANCE)

			assert(getColumn(0) === Seq(1.2f))
		}
		
		it("should be able to handle ',' as decimal character for DOUBLE"){
			val content = head + "\n" + "1,2"
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.DOUBLE)
			)
			
			implicit val rows = makeRows(content, colDefs, ';', java.util.Locale.FRANCE)

			assert(getColumn(0) === Seq(1.2))
		}
	}
	
	
	describe("Parse error handling"){
		val header = "header"

		it("should produce a NumberFormatException for type INT when value cannot be parsed"){
			val content = header + "\n" + "2147483648"
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.INT)
			)
			
			val myThrowable = intercept[NumberFormatException] {
				makeRows(content, colDefs)				
			}
			
			println(myThrowable.getMessage)
			assert(myThrowable.getMessage.startsWith("Could not parse"))
		}
		
		it("should produce a NumberFormatException for type FLOAT when exceeding max value"){
			val content = header + "\n" + "3.4028236E38"
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.FLOAT)
			)
			
			val myThrowable = intercept[NumberFormatException] {
				makeRows(content, colDefs)				
			}
			
			println(myThrowable.getMessage)
			assert(myThrowable.getMessage.endsWith("is outside the range for Float."))
		}
		
		it("should produce a BadNumber exception for type FLOAT when value cannot be parsed"){
			val content = header + "\n" + "3.D"
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.FLOAT)
			)
			
			val myThrowable = intercept[BadNumber] {
				makeRows(content, colDefs)				
			}
			
			println(myThrowable.getMessage)
			assert(myThrowable.getMessage.startsWith("Could not parse"))
		}
		
		it("should produce a NumberFormatException for type DOUBLE when exceeding max value"){
			val content = header + "\n" + "2.7976931348623157E308"
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.DOUBLE)
			)
			
			val myThrowable = intercept[NumberFormatException] {
				makeRows(content, colDefs)				
			}
			
			println(myThrowable.getMessage)
			assert(myThrowable.getMessage.endsWith("is outside the range for Double."))
		}
		
		it("should produce a BadNumber exception for type DOUBLE when value cannot be parsed"){
			val content = header + "\n" + "5.A3"
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.DOUBLE)
			)
			
			val myThrowable = intercept[BadNumber] {
				makeRows(content, colDefs)				
			}
			
			println(myThrowable.getMessage)
			assert(myThrowable.getMessage.startsWith("Could not parse"))
		}
		
		it("should produce a NumberFormatException for type LONG when value cannot be parsed"){
			val content = header + "\n" + "9223372036854775808"
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.LONG)
			)
			
			val myThrowable = intercept[NumberFormatException] {
				makeRows(content, colDefs)				
			}
			
			println(myThrowable.getMessage)
			assert(myThrowable.getMessage.startsWith("Could not parse"))
		}
		
		it("should produce a NumberFormatException for type BYTE when illegal value is provided"){
			val content = header + "\n" + "d"
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.BYTE)
			)
			
			val myThrowable = intercept[NumberFormatException] {
				makeRows(content, colDefs)				
			}
			
			println(myThrowable.getMessage)
			assert(myThrowable.getMessage.startsWith("Could not parse"))
		}
		
		it("should produce a NumberFormatException for type SHORT value cannot be parsed"){
			val content = header + "\n" + "32768"
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.SHORT)
			)
			
			val myThrowable = intercept[NumberFormatException] {
				makeRows(content, colDefs)				
			}
			
			println(myThrowable.getMessage)
			assert(myThrowable.getMessage.startsWith("Could not parse"))
		}
		
		it("should produce a IllegalArgumentException for type CHAR when exceeding one character"){
			val content = header + "\n" + "vv"
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.CHAR)
			)
			
			val myThrowable = intercept[IllegalArgumentException] {
				makeRows(content, colDefs)				
			}
			
			println(myThrowable.getMessage)
			assert(myThrowable.getMessage.endsWith("Only one character is allowed."))
		}
		
		it("should produce a NoSuchElementException when header mismatch"){
			val content = header + "\n" + "1"
			
			val colDefs = Array(
				new ColumnDefinition("wrong_header", DataType.INT)
			)
			
			val myThrowable = intercept[NoSuchElementException] {
				makeRows(content, colDefs)				
			}
			
			println(myThrowable.getMessage)
			assert(myThrowable.getMessage.startsWith("Column not present in input header"))
		}
		
		it("should produce a BadNumber exception for type FLOAT when characters are included"){
			val content = header + "\n" + "1.2abc"
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.FLOAT)
			)
			
			val myThrowable = intercept[BadNumber] {
				makeRows(content, colDefs)				
			}
			
			println(myThrowable.getMessage)
			assert(myThrowable.getMessage.startsWith("Could not parse"))
		}
		
		it("should produce a BadNumber exception for type FLOAT when decimal character is ',' and locale != FRENCH"){
			val content = header + "\n" + "1,2"
			
			val colDefs = Array(
				new ColumnDefinition("header", DataType.FLOAT)
			)
			
			val myThrowable = intercept[BadNumber] {
				makeRows(content, colDefs)				
			}
			
			println(myThrowable.getMessage)
			assert(myThrowable.getMessage.startsWith("Bad decimal character in"))
		}
		
		it("should produce a CsvException when number of columns varies between rows"){
			val content = "one;two" + "\n" + "1;2" + "\n" + "1"
			
			val colDefs = Array(
				new ColumnDefinition("one", DataType.INT),
				new ColumnDefinition("two", DataType.INT)
			)
			
			val myThrowable = intercept[CsvException] {
				makeRows(content, colDefs)				
			}
			
			println(myThrowable.getMessage)
			assert(myThrowable.getMessage.startsWith("This rows length (#"))
		}
	}
}