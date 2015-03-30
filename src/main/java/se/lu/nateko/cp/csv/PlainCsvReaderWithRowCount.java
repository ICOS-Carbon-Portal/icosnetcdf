package se.lu.nateko.cp.csv;

import java.io.File;
import java.io.IOException;

public class PlainCsvReaderWithRowCount implements CsvReaderWithRowCount{

	private final int nRows;
	private final PlainCsvReader inner;
	
	public PlainCsvReaderWithRowCount(File file, char separator, ColumnDefinition[] columns) throws Exception {
		PlainCsvReader reader = new PlainCsvReader(file, separator, new ColumnDefinition[0]);
		int n = 0;
		while(reader.hasNext()){
			n++;
			reader.next();
		}
		nRows = n;
		reader.close();
		inner = new PlainCsvReader(file, separator, columns);
	}

	@Override
	public boolean hasNext() {
		return inner.hasNext();
	}

	@Override
	public Object[] next() {
		return inner.next();
	}

	@Override
	public void close() throws IOException {
		inner.close();
	}

	@Override
	public int getRowCount() {
		return nRows;
	}

}
