package se.lu.nateko.cp.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import se.lu.nateko.cp.bintable.DataType;
import se.lu.nateko.cp.rowsource.ColumnDefinition;
import se.lu.nateko.cp.rowsource.RowSource;

import com.opencsv.CSVReader;

public class CsvRowSource implements RowSource{

	private final ColumnDefinition[] schema;
	private final CSVReader reader;
	private final Iterator<String[]> innerIter;
	private final DataType[] dtypes;
	private final int[] positions;
	private final int n;
	private boolean closed = false;

	public CsvRowSource(Reader reader, char separator, ColumnDefinition[] schema) throws IOException{
		this.schema = schema;
		this.reader = new CSVReader(reader, separator);
		String[] header = this.reader.readNext();

		n = schema.length;
		dtypes = new DataType[n];
		positions = new int[n];

		for(int i = 0; i < n; i++){
			String col = schema[i].name;
			positions[i] = indexIn(header, col);
			dtypes[i] = schema[i].dtype;
		}

		innerIter = this.reader.iterator();
	}

	@Override
	public boolean hasNext() {
		if(closed) return false;
		
		boolean hasNxt = innerIter.hasNext();

		if(!hasNxt)	try {
			close();
		} catch (IOException e) {}

		return hasNxt;
	}

	@Override
	public Object[] next() {
		Object[] row = new Object[n];
		String[] raw = innerIter.next();
		
		for(int i = 0; i < n; i++){
			row[i] = parse(raw[positions[i]], dtypes[i]);
		}
		return row;
	}

	@Override
	public void close() throws IOException {
		if(closed) return;
		closed = true;
		reader.close();
	}

	@Override
	public ColumnDefinition[] getSchema() {
		return schema;
	}

	public static Object parse(String value, DataType dtype){
		// TODO Investigate locales
		// TODO Investigate what happens when col count mismatch on different rows 
		switch(dtype){
			case INT: return Integer.parseInt(value);
			case STRING: return value;
			case FLOAT: 
				Float flt = Float.parseFloat(value);
				if (flt.isInfinite())
					throw new NumberFormatException(value + " is outside the range for Float.");
				else
					return flt;
			case DOUBLE: 
				Double dbl = Double.parseDouble(value);
				if(dbl.isInfinite())
					throw new NumberFormatException(value + " is outside the range for Double.");
				else
					return dbl;
			case LONG: return Long.parseLong(value);
			case BYTE: return Byte.parseByte(value);
			case SHORT: return Short.parseShort(value);
			case CHAR: 
				if (value.length() == 1)
					return value.charAt(0);
				else if(value.length() == 0)
					return Character.MIN_VALUE;
				else
					throw new IllegalArgumentException(value + " is too long. Only one character is allowed.");

			default: throw new RuntimeException("Unsupported datatype " + dtype); 
		}
	}

	private static int indexIn(String[] headerColumns, String columnName){
		for(int i = 0; i < headerColumns.length; i++){
			if(columnName.equals(headerColumns[i])) return i;
		}
		throw new NoSuchElementException("Column not present in CSV header: " + columnName);
	}

}
