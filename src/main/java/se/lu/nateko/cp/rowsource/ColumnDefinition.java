package se.lu.nateko.cp.rowsource;

import se.lu.nateko.cp.bintable.DataType;

public class ColumnDefinition {
	
	public final String name;
	public final DataType dtype;
	
	public ColumnDefinition(String name, DataType dtype){
		this.name = name;
		this.dtype = dtype;
	}
	
	@Override
	public boolean equals(Object other){
		if(other == null || !(other instanceof ColumnDefinition)) return false;
		ColumnDefinition o = (ColumnDefinition)other;
		return name.equals(o.name) && dtype.equals(o.dtype);
	}
	
	@Override
	public int hashCode(){
		return 37 * name.hashCode() + dtype.hashCode();
	}
}
