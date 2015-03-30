package se.lu.nateko.cp.csv;

import java.io.File;
import java.util.Arrays;

public class CsvSchema {

	public final String[] header;
	public final int[] dateTimeInd;
	public final int[] colData;
	
	public CsvSchema(String[] header, int[] dateTimeInd, int[] colData){
		this.header = header;
		this.dateTimeInd = dateTimeInd;
		this.colData = colData;
	}

}
