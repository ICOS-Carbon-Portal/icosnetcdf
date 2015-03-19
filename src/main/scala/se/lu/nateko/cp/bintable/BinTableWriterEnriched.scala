package se.lu.nateko.cp

package object bintable{

	implicit class BinTableWriterEnriched(inner: BinTableWriter) {
		def write(p: Product): Unit = {
			inner.writeRow(p.productIterator.collect{case a: AnyRef => a}.toArray)
		}
	}

}
