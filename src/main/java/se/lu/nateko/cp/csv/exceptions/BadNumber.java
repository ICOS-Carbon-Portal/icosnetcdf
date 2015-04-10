package se.lu.nateko.cp.csv.exceptions;

public class BadNumber extends CsvException {

	public BadNumber(){}
	public BadNumber(String message){
		super(message);
	}
	
	private static final long serialVersionUID = -6821932977478320115L;

}
