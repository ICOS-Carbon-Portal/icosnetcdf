package se.lu.nateko.cp.netcdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import scala.NotImplementedError;
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
import ucar.nc2.NetcdfFileWriter.Version;
import ucar.nc2.Variable;


public class WriteNetCDF implements NetCdfWriter{

	private final NetCdfSchema schema;
	private NetcdfFileWriter writer = null;
	private int rowCounter = 0;
	
	public enum ncVersion{
		NC3(NetcdfFileWriter.Version.netcdf3),
		NC4(NetcdfFileWriter.Version.netcdf4);
		
		ncVersion(NetcdfFileWriter.Version version) {
	        this.version = version;
	    }
		
	    final NetcdfFileWriter.Version version;
	}
	
	public WriteNetCDF(NetCdfSchema schema, File file, ncVersion ver) throws IOException {
		this.schema = schema;
		
		if ((file.getName().endsWith(".nc4") && ver == ncVersion.NC3) || (file.getName().endsWith(".nc") && ver == ncVersion.NC4)){
			throw new IllegalArgumentException("File extension (" + file.getName() + ") does not match NetCDF version (" + ver.version.toString() + ")");
		}
		
		writer = NetcdfFileWriter.createNew(ver.version, file.getAbsolutePath(), null);
		
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
	
	public WriteNetCDF(NetCdfSchema schema, File file) throws IOException {
		this(schema, file, ncVersion.NC4);
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
	public void writeRow(Object[] row) throws IndexOutOfBoundsException, IOException, InvalidRangeException {
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
						throw new IOException("Could not write " + floatA.toString() + " in position " + rowCounter);
					} catch (InvalidRangeException e) {
						throw new InvalidRangeException("Could not write " + floatA.toString() + " in position " + rowCounter);
					}
				    break;
				
				case DOUBLE:
					ArrayDouble doubleA = new ArrayDouble.D1(1);
					doubleA.setDouble(0, (double)row[rowIndex]);
				    try {
						writer.write(var, new int[]{rowCounter}, doubleA);
					} catch (IOException e) {
						throw new IOException("Could not write " + doubleA.toString() + " in position " + rowCounter);
					} catch (InvalidRangeException e) {
						throw new InvalidRangeException("Could not write " + doubleA.toString() + " in position " + rowCounter);
					}
				    break;
				    
				case INT:
					ArrayInt intA = new ArrayInt.D1(1);
					intA.setInt(0, (int)row[rowIndex]);
				    try {
						writer.write(var, new int[]{rowCounter}, intA);
					} catch (IOException e) {
						throw new IOException("Could not write " + intA.toString() + " in position " + rowCounter);
					} catch (InvalidRangeException e) {
						throw new InvalidRangeException("Could not write " + intA.toString() + " in position " + rowCounter);
					}
				    break;
				    
				case LONG:
					//Cannot be used in netcdf-3
					ArrayLong longA = new ArrayLong.D1(1);
					longA.setLong(0, (long)row[rowIndex]);
				    try {
						writer.write(var, new int[]{rowCounter}, longA);
					} catch (IOException e) {
						throw new IOException("Could not write " + longA.toString() + " in position " + rowCounter);
					} catch (InvalidRangeException e) {
						throw new InvalidRangeException("Could not write " + longA.toString() + " in position " + rowCounter);
					}
				    break;
				
				default: 
					throw new UnsupportedOperationException();
			}
		}
		
		rowCounter++;
	}
	
	@Override
	public void writeArray(Array arr, int variableIndex) throws IOException, InvalidRangeException{
		Variable var = writer.findVariable(schema.variables.get(variableIndex).name);
		
		try {
			writer.write(var, arr);
		} catch (IOException e) {
			throw new IOException(e.getMessage());
		} catch (InvalidRangeException e) {
			throw new InvalidRangeException(e.getMessage());
		}
	}

	@Override
	public void close() throws IOException {
		try {
			writer.close();
		} catch (IOException e) {
			throw new IOException(e.getMessage());
		}
	}

}
