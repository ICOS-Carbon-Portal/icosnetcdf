package se.lu.nateko.cp.csv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.lang3.NotImplementedException;

import com.opencsv.CSVReader;

import se.lu.nateko.cp.bintable.DataType;

public class PlainCsvReader implements CsvReader{

	private final CSVReader reader;
	private final Iterator<String[]> innerIter;
	private final DataType[] dtypes;
	private final int[] positions;
	private final int n;
	
	public PlainCsvReader(File file, char separator, ColumnDefinition[] columns) throws Exception {

		reader = new CSVReader(new FileReader(file.getAbsolutePath()), separator);

		String[] header = reader.readNext();
    	
    	n = columns.length;
    	dtypes = new DataType[n];
    	positions = new int[n];
    	
    	for(int i = 0; i < n; i++){
    		String col = columns[i].name;
    		positions[i] = indexIn(header, col);
    		dtypes[i] = columns[i].dtype;
    	}
    	
    	innerIter = reader.iterator();
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

	@Override
	public boolean hasNext() {
		return innerIter.hasNext();
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
	
	public static Object parse(String value, DataType dtype){
		throw new NotImplementedException("");
	}
	
	public static int indexIn(String[] arr, String str){
		throw new NotImplementedException("");
	}
	
	
}
