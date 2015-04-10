package se.lu.nateko.cp.csv.exceptions;

public class CsvException extends RuntimeException {

	private static final long serialVersionUID = 1861019312797784961L;
	
	public CsvException(){}
	public CsvException(String message){
		super(message);
	}

}
