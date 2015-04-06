package se.lu.nateko.cp.rowsource;

import java.util.Iterator;

public abstract class RowByRowTransformer implements RowSourceTransformer {

	public abstract Object[] transformRow(Object[] row);

	@Override
	public Iterator<Object[]> transformRows(final Iterator<Object[]> rows) {
		return new Iterator<Object[]>(){
			public boolean hasNext() {
				return rows.hasNext();
			}
			public Object[] next() {
				return transformRow(rows.next());
			}
		};
	}

}
