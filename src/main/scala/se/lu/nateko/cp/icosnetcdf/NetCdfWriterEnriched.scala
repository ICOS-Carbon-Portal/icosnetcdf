package se.lu.nateko.cp

package object icosnetcdf{

	implicit class NetCdfWriterEnriched(inner: NetCdfWriter) {
		def write(p: Product): Unit = {
			inner.writeRow(p.productIterator.collect{case a: AnyRef => a}.toArray)
		}
	}

}

