package se.lu.nateko.cp.rowsource;

import java.io.Closeable;
import java.util.Iterator;

import java.util.Arrays;

public interface RowSource extends Iterator<Object[]>, Closeable {
	ColumnDefinition[] getSchema();

	default RowSource transform(RowSourceTransformer transformer){
		return new TransformedRowSource(this, transformer);
	}

	default RowSource drop(String... columns){
		return transform(new DropColumnTransformer(getSchema(), Arrays.asList(columns)));
	}
}

