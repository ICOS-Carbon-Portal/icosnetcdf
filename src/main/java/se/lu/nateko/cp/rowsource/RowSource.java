package se.lu.nateko.cp.rowsource;

import java.io.Closeable;
import java.util.Iterator;

public interface RowSource extends Iterator<Object[]>, Closeable {
	ColumnDefinition[] getSchema();
}

