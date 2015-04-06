package se.lu.nateko.cp.rowsource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DropColumnTransformer extends RowByRowTransformer{

	private final Set<Integer> columnIndexes;

	public DropColumnTransformer(Collection<Integer> columnIndexes){
		this.columnIndexes = new HashSet<Integer>(columnIndexes);
	}

	public DropColumnTransformer(ColumnDefinition[] schema, Collection<String> colsToDrop){
		this.columnIndexes = IntStream.range(0, schema.length)
			.filter(ind -> colsToDrop.contains(schema[ind].name))
			.boxed()
			.collect(Collectors.toSet());
	}

	@Override
	public ColumnDefinition[] transformSchema(ColumnDefinition[] schema) {
		return IntStream.range(0, schema.length)
			.filter(ind -> !columnIndexes.contains(ind))
			.mapToObj(ind -> schema[ind])
			.toArray(i -> new ColumnDefinition[i]);
	}

	@Override
	public Object[] transformRow(Object[] row) {
		return IntStream.range(0, row.length)
			.filter(ind -> !columnIndexes.contains(ind))
			.mapToObj(ind -> row[ind])
			.toArray();
	}

}
