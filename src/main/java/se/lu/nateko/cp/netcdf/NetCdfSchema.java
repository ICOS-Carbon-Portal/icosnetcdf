package se.lu.nateko.cp.netcdf;

import java.util.Map;
import java.util.List;

public class NetCdfSchema {

	public final List<NetCdfVariableInfo> variables;
	public final int dimSize;
	public final Map<String, String> globalAttr;
	
	public NetCdfSchema(List<NetCdfVariableInfo> variables, int dimSize, Map<String, String> globalAttr){
		this.variables = variables;
		this.dimSize = dimSize;
		this.globalAttr = globalAttr;
	}
	
	public NetCdfSchema(List<NetCdfVariableInfo> variables, int dimSize){
		this(variables, dimSize, null);
	}
	
	public NetCdfSchema(List<NetCdfVariableInfo> variables){
		this(variables, -1, null);
	}

}
