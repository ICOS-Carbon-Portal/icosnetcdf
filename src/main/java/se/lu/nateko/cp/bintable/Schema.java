package se.lu.nateko.cp.bintable;

public class Schema {

	public final DataType[] columns;
	public final long size;

	public Schema(DataType[] columns, long size){
		this.columns = columns;
		this.size = size;
	}
}
