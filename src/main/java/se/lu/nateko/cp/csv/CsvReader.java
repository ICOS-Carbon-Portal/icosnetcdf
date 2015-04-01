package se.lu.nateko.cp.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class CsvReader implements TableReader{

	private final File file;
	private final char separator;
	private int nRows = -1;
	
	public CsvReader(File file, char separator){
		this.file = file;
		this.separator = separator;

	}

	@Override
	public int nRows() throws IOException {
		if(nRows > -1) return nRows;
		
		RowSource rs = readRowSource(new ColumnDefinition[0]);
		int n = 0;
		try{
			while(rs.hasNext()){
				n++;
				rs.next();
			}
		}finally{
			rs.close();
		}
		nRows = n;
		return n;
	}

	@Override
	public RowSource readRowSource(ColumnDefinition[] schema) throws IOException {
		Reader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
		return new CsvRowSource(reader, separator, schema);
	}
	
	
}
