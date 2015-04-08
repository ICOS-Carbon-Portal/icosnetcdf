package se.lu.nateko.cp.rowsource;

import java.io.IOException;

public class ArrayRowSource implements RowSource {

	private final ColumnDefinition[] schema;
	private final Object[][] rows;
	private Boolean hasNext;
	private int position = 0;

	public ArrayRowSource(ColumnDefinition[] schema, Object[][] rows){
		this.schema = schema;
		this.rows = rows;
		hasNext = rows.length > 0;
	}

	@Override
	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public Object[] next() {
		if(!hasNext) throw new IllegalStateException("RowSource has already ended, is empty or got closed");
		position++;
		if(position >= rows.length) hasNext = false;
		return rows[position - 1];
	}

	@Override
	public void close() throws IOException {
		hasNext = false;
	}

	@Override
	public ColumnDefinition[] getSchema() {
		return schema;
	}

}
