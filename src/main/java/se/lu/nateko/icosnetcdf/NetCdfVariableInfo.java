package se.lu.nateko.icosnetcdf;

import ucar.ma2.DataType;

public class NetCdfVariableInfo {

	public final String name;
	public final DataType dataType;
	public final String attrTitle, attrUnits, attrLongName;

	public NetCdfVariableInfo(String name, DataType dataType, String attrTitle, String attrUnits, String attrLongName){
		this.name = name;
		this.dataType = dataType;
		this.attrTitle = attrTitle;
		this.attrUnits = attrUnits;
		this.attrLongName = attrLongName;
	}
	
	public NetCdfVariableInfo(String name, DataType dataType, String attrTitle, String attrUnits){
		this(name, dataType, attrTitle, attrUnits, null);
	}
	
	public NetCdfVariableInfo(String name, DataType dataType, String attrTitle){
		this(name, dataType, attrTitle, null, null);
	}
	
	public NetCdfVariableInfo(String name, DataType dataType){
		this(name, dataType, null, null, null);
	}
}
