package se.lu.nateko.icosnetcdf

import ucar.nc2.Variable
import ucar.ma2.{Array => CdmArray, DataType}
import scala.util.Try

sealed trait PlainColumn{
	type V
	def values: Iterator[V]
}

trait IntColumn extends PlainColumn{ type V = Int }
trait FloatColumn extends PlainColumn{ type V = Float }

object PlainColumn{

	def apply(variable: Variable): Try[PlainColumn] = Try{

		assert(variable.getRank == 1, "Expecting NetCDF variable rank to be 1 to convert to a plain column")

		val n = variable.getShape(0)

		def getValues[T](accessor: CdmArray => Int => T): Iterator[T] = {
			Range(0, n).iterator.map(n => {
				val arr = variable.read(Array(n), Array(1))
				accessor(arr)(0)
			})
		}

		variable.getDataType match{
			case DataType.INT => new IntColumn{ def values = getValues(_.getInt) }
			case DataType.FLOAT => new FloatColumn{ def values = getValues(_.getFloat) }
			case dt @ _ => throw new Exception("Unsupported NetCDF variable data type: " + dt.name)
		}

	}

}