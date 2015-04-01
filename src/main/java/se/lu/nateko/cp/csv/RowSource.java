package se.lu.nateko.cp.csv;

import java.io.Closeable;
import java.util.Iterator;

public interface RowSource extends Iterator<Object[]>, Closeable {
	ColumnDefinition[] getSchema();
}

