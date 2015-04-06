package se.lu.nateko.cp.rowsource;

import java.io.IOException;
import java.util.Iterator;

public class TransformedRowSource implements RowSource {

	private final ColumnDefinition[] schema;
	private final Iterator<Object[]> rows;
	private final RowSource original;

	public TransformedRowSource(RowSource original, RowSourceTransformer transformer){
		this.original = original;
		schema = transformer.transformSchema(original.getSchema());
		rows = transformer.transformRows(original);
	}

	@Override
	public boolean hasNext() {
		return rows.hasNext();
	}

	@Override
	public Object[] next() {
		return rows.next();
	}

	@Override
	public void close() throws IOException {
		original.close();
	}

	@Override
	public ColumnDefinition[] getSchema() {
		return schema;
	}

}
