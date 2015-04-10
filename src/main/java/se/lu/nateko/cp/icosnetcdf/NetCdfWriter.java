package se.lu.nateko.cp.icosnetcdf;

import java.io.IOException;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

public interface NetCdfWriter {

	NetCdfSchema getSchema();
	void writeRow(Object[] row) throws IOException, IndexOutOfBoundsException, InvalidRangeException;
	void writeArray(Array arr, int variableIndex) throws IOException, InvalidRangeException;
	void close() throws IOException;
	
}

