package se.lu.nateko.icosnetcdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayLong;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;


public class WriteNetCDF implements NetCdfWriter{

	private final NetCdfSchema schema;
	private NetcdfFileWriter writer = null;
	private int rowCounter = 0;
	
	public WriteNetCDF(NetCdfSchema schema, File file) throws IOException {
		this.schema = schema;
		
		writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, file.getAbsolutePath(), null);
		
		// add dimensions
		Dimension tstep;
		if (schema.dimSize == -1) {
			tstep = writer.addUnlimitedDimension("tstep");
		} else {
			tstep = writer.addDimension(null, "tstep", schema.dimSize);
		}
		List<Dimension> dims = new ArrayList<Dimension>();
	    dims.add(tstep);
	    
	    // add Variables
	    for (NetCdfVariableInfo varInfo : schema.variables){
		    Variable var = writer.addVariable(null, varInfo.name, varInfo.dataType, dims);
		    
		    // add attributes if there are any
		    if (varInfo.attrTitle != null) {
		    	var.addAttribute(new Attribute("title", varInfo.attrTitle));
		    }
		    
		    if (varInfo.attrUnits != null) {
		    	var.addAttribute(new Attribute("units", varInfo.attrUnits));
		    }
		    
		    if (varInfo.attrLongName != null) {
		    	var.addAttribute(new Attribute("long_name", varInfo.attrLongName));
		    }
	    }
	    
	    // add global attributes if there are any
	    if (schema.globalAttr != null) {
		    for (Map.Entry<String, String> attrGlobal: schema.globalAttr.entrySet()){
		    	writer.addGroupAttribute(null, new Attribute(attrGlobal.getKey(), attrGlobal.getValue()));
		    }
	    }
	    
	    writer.create();
	}

	@Override
	public NetCdfSchema getSchema() {
		return schema;
	}
	
	private int getRowIndex(String varName){
		for (int i = 0; i < schema.variables.size(); i++){
			if (schema.variables.get(i).name == varName){
				return i;
			}
		}
		
		return -1;
	}

	@Override
	public void writeRow(Object[] row) throws IndexOutOfBoundsException {
		int size = schema.variables.size(); 
		if(size != row.length){
			throw new IndexOutOfBoundsException("Wrong number of elements in a row!");
		}
		
		for(int i = 0; i < size; i++){
			NetCdfVariableInfo varInfo = schema.variables.get(i);
			Variable var = writer.findVariable(schema.variables.get(i).name);
			// Index of this variable in the schema
			int rowIndex = getRowIndex(schema.variables.get(i).name);
			
			switch(varInfo.dataType){
				case FLOAT:
					ArrayFloat floatA = new ArrayFloat.D1(1);
					floatA.setFloat(0, (float)row[rowIndex]);
				    try {
						writer.write(var, new int[]{rowCounter}, floatA);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidRangeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				    break;
				
				case DOUBLE:
					ArrayDouble doubleA = new ArrayDouble.D1(1);
					doubleA.setDouble(0, (double)row[rowIndex]);
				    try {
						writer.write(var, new int[]{rowCounter}, doubleA);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidRangeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				    break;
				    
				case INT:
					ArrayInt intA = new ArrayInt.D1(1);
					intA.setInt(0, (int)row[rowIndex]);
				    try {
						writer.write(var, new int[]{rowCounter}, intA);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidRangeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				    break;
				    
				case LONG:
					//Cannot be used in netcdf-3
					ArrayLong longA = new ArrayLong.D1(1);
					longA.setLong(0, (long)row[rowIndex]);
				    try {
						writer.write(var, new int[]{rowCounter}, longA);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidRangeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				    break;
				
				default: 
					throw new UnsupportedOperationException();
			}
		}
		
		rowCounter++;
	}
	
	@Override
	public void writeArray(Array arr, int variableIndex){
		Variable var = writer.findVariable(schema.variables.get(variableIndex).name);
		
		try {
			writer.write(var, arr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidRangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
