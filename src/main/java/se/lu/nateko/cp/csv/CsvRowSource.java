package se.lu.nateko.cp.csv;

import java.io.IOException;
import java.io.Reader;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;

import se.lu.nateko.cp.bintable.DataType;
import se.lu.nateko.cp.csv.exceptions.BadNumber;
import se.lu.nateko.cp.csv.exceptions.CsvException;
import se.lu.nateko.cp.rowsource.ColumnDefinition;
import se.lu.nateko.cp.rowsource.RowSource;

import com.opencsv.CSVReader;

public class CsvRowSource implements RowSource{

	private final ColumnDefinition[] schema;
	private static Locale locale;
	private final CSVReader reader;
	private final Iterator<String[]> innerIter;
	private final DataType[] dtypes;
	private final int[] positions;
	private final int n;
	private boolean closed = false;
	private int rowNumber = 1;

	public CsvRowSource(Reader reader, char separator, ColumnDefinition[] schema, Locale locale) throws IOException{
		// If locale is not English ("." decimal character) it is likely that "," is used as decimal character 
		if (separator == ',' && locale != Locale.ENGLISH){
			throw new IllegalArgumentException("Current locale (" + locale.toString() + " might conflict with separator (" + separator + ")");
		}
		
		this.schema = schema;
		this.locale = locale;
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
	
	public CsvRowSource(Reader reader, char separator, ColumnDefinition[] schema) throws IOException{
		// Default to using "." as decimal character
		this(reader, separator, schema, Locale.ENGLISH);
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
		rowNumber++;
		
		if (raw.length != n){
			throw new CsvException("This rows length (#" + rowNumber + ") does not match the length of the header");
		}
		
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

	public static Number parseDecimal(String value, Locale loc){
		NumberFormat numberFormat = NumberFormat.getNumberInstance(loc);
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(loc);
		char decimalSeparator = dfs.getDecimalSeparator();
		
		if ((decimalSeparator == ',' && value.contains(".")) || (decimalSeparator == '.' && value.contains(","))){
			throw new BadNumber("Bad decimal character in " + value + ". Expected '" + decimalSeparator + "'");
		}
		
		ParsePosition parsePosition = new ParsePosition(0);
		Number number = numberFormat.parse(value, parsePosition);

		if(parsePosition.getIndex() != value.length()){
			throw new BadNumber("Could not parse " + value + " to Number");
		}

		return number;
	}

	public static Number parseDecimal(String value){
		return parseDecimal(value, locale);
	}
	
	public static Object parse(String value, DataType dtype){
		switch(dtype){
			case INT:
				try {
					return Integer.parseInt(value);
				} catch (NumberFormatException e) {
					throw new NumberFormatException("Could not parse " + value + " to INT");
				}
			case STRING: return value;
			case FLOAT:
				Float flt = parseDecimal(value).floatValue();
				
				if (flt.isInfinite())
					throw new NumberFormatException(value + " is outside the range for Float.");
				else
					return flt;
			case DOUBLE: 
				Double dbl = parseDecimal(value).doubleValue();
				
				if(dbl.isInfinite())
					throw new NumberFormatException(value + " is outside the range for Double.");
				else
					return dbl;
			case LONG:
				try {
					return Long.parseLong(value);
				} catch (NumberFormatException e) {
					throw new NumberFormatException("Could not parse " + value + " to LONG");
				}
			case BYTE:
				try {
					return Byte.parseByte(value);
				} catch (NumberFormatException e) {
					throw new NumberFormatException("Could not parse " + value + " to BYTE");
				}
			case SHORT:
				try {
					return Short.parseShort(value);
				} catch (NumberFormatException e) {
					throw new NumberFormatException("Could not parse " + value + " to SHORT");
				}
			case CHAR: 
				if (value.length() == 1)
					return value.charAt(0);
				else if(value.length() == 0)
					return Character.MIN_VALUE;
				else
					throw new IllegalArgumentException("Value '" + value + "' is too long. Only one character is allowed.");

			default: throw new RuntimeException("Unsupported datatype " + dtype); 
		}
	}

	private static int indexIn(String[] headerColumns, String columnName){
		for(int i = 0; i < headerColumns.length; i++){
			if(columnName.equals(headerColumns[i])) return i;
		}
		throw new NoSuchElementException("Column not present in input header: " + columnName);
	}

}
