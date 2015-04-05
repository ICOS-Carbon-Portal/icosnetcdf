package se.lu.nateko.cp.rowsource;

import java.io.IOException;

public interface TableReader {
	int nRows() throws IOException;
	RowSource readRowSource(ColumnDefinition[] schema) throws IOException;
}
