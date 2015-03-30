package se.lu.nateko.cp.icosnetcdf;

import java.io.IOException;

import ucar.ma2.Array;

public interface NetCdfWriter {

	NetCdfSchema getSchema();
	void writeRow(Object[] row) throws IOException;
	void writeArray(Array arr, int variableIndex);
	void close() throws IOException;
	
}

