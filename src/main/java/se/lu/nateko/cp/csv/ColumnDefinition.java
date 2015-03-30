package se.lu.nateko.cp.csv;

import se.lu.nateko.cp.bintable.DataType;

public class ColumnDefinition {
	
	public final String name;
	public final DataType dtype;
	
	public ColumnDefinition(String name, DataType dtype){
		this.name = name;
		this.dtype = dtype;
	}
}
