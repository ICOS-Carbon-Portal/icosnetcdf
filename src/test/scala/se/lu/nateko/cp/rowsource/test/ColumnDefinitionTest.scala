package se.lu.nateko.cp.rowsource.test

import org.scalatest.FunSpec
import se.lu.nateko.cp.rowsource.ColumnDefinition
import se.lu.nateko.cp.rowsource.DataType

class ColumnDefinitionTest extends FunSpec{

	describe("equality implementation"){

		it("gives true for equivalent instances"){
			val def1 = new ColumnDefinition("COL", DataType.CHAR)
			val def2 = new ColumnDefinition("COL", DataType.CHAR)
			assert(def1.equals(def2)) //Java value equality
			assert(def1 === def2) //ScalaTest value equality
			assert(def1 == def2) //Scala value equality (delegates to 'equals')
			assert(!def1.eq(def2)) //Scala reference equality
		}

		it("gives false for columns of different names"){
			val def1 = new ColumnDefinition("COL1", DataType.CHAR)
			val def2 = new ColumnDefinition("COL2", DataType.CHAR)
			assert(!def1.equals(def2))
			assert(def1 !== def2)
			assert(def1 != def2)
		}

		it("gives false for columns of different data types"){
			val def1 = new ColumnDefinition("COL", DataType.CHAR)
			val def2 = new ColumnDefinition("COL", DataType.INT)
			assert(!def1.equals(def2))
			assert(def1 !== def2)
			assert(def1 != def2)
		}
	}

	describe("hashCode implementation"){

		it("gives the same hash code for equal instances"){
			val def1 = new ColumnDefinition("COL", DataType.INT)
			val def2 = new ColumnDefinition("COL", DataType.INT)
			assert(def1.hashCode === def2.hashCode)
		}
	}
}