package se.lu.nateko.icosnetcdf;

import ucar.ma2.Array;

public interface NetCdfWriter {

	NetCdfSchema getSchema();
	void writeRow(Object[] row);
	void writeArray(Array arr, int variableIndex);
	void close();
	
}

