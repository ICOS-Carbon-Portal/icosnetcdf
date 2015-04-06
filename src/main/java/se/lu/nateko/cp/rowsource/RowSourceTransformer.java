package se.lu.nateko.cp.rowsource;

import java.util.Iterator;

public interface RowSourceTransformer {

	ColumnDefinition[] transformSchema(ColumnDefinition[] schema);
	Iterator<Object[]> transformRows(Iterator<Object[]> rows);

}
