package se.lu.nateko.cp.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.zip.DataFormatException;

import javax.xml.bind.DatatypeConverter;

import com.opencsv.CSVReader;

public class ReadCSV {
	
	private final CsvSchema schema;
	private Iterator<String[]> iterator;
	private CSVReader reader;

	public ReadCSV(File file, char separator, CsvSchema schema) throws Exception {
		this.schema = schema;

		//int rowCount = 0;
		
		try {
			reader = new CSVReader(new FileReader(file.getAbsolutePath()), separator);
			
			String[] header, nextLine;
			
		    try {
		    	header = reader.readNext();
		    	
		    	if (isHeaderValid(header)){
		    		
		    		iterator = reader.iterator();
		    		
//					while ((nextLine = reader.readNext()) != null) {
//
//						String data = "";
//						
//						for (int i=0; i<schema.colData.length; i++){
//							data += getColName(schema.colData[i]) + ": " + nextLine[schema.colData[i]] + " ";
//						}
//						
//					    System.out.println(getDateTime(nextLine).getTime().toString() + ": " + data);
//					    
//					    rowCount++;
//					}
					
		    	} else {
		    		throw new DataFormatException("The schema header does not match the CSV header");
		    	}
				
				//System.out.println("Rows: " + rowCount);
				
			} catch (IOException e) {
				throw new IOException(e.getMessage());
			}
			
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException(e.getMessage());
		} finally {
			//reader.close();
		}
	}
	
	public Iterator<String[]> getIterator(){
		return this.iterator;
	}
	
	private boolean isHeaderValid(String[] csvHeader){
		return Arrays.equals(csvHeader, schema.header);
	}
	
	public Calendar getDateTime(String[] line){
		String dateTimeStr = null;
		
		try {
			dateTimeStr = line[schema.dateTimeInd[0]] + "-" + line[schema.dateTimeInd[1]] + "-" + line[schema.dateTimeInd[2]];
			dateTimeStr += "T" + line[schema.dateTimeInd[3]] + ":" + line[schema.dateTimeInd[4]] + ":" + line[schema.dateTimeInd[5]] + "Z";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return DatatypeConverter.parseDateTime(dateTimeStr);
	}
	
	public String getColName(int colDataIndex){
		return schema.header[colDataIndex];
	}
	
	public void close(){
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
