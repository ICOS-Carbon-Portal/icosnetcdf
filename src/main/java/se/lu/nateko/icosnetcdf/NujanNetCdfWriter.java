package se.lu.nateko.icosnetcdf;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import edu.ucar.ral.nujan.netcdf.NhDimension;
import edu.ucar.ral.nujan.netcdf.NhException;
import edu.ucar.ral.nujan.netcdf.NhFileWriter;
import edu.ucar.ral.nujan.netcdf.NhGroup;
import edu.ucar.ral.nujan.netcdf.NhVariable;
import ucar.ma2.Array;
import ucar.ma2.DataType;

public class NujanNetCdfWriter implements NetCdfWriter{

	private final NetCdfSchema schema;
	private final NhFileWriter hfile;
	private final NhVariable[] vars;
	private final int[] startIdx = new int[]{0};

	public NujanNetCdfWriter(NetCdfSchema schema, File file) throws NhException{
		this.schema = schema;
		hfile = new NhFileWriter(file.getAbsolutePath(), NhFileWriter.OPT_OVERWRITE);

		NhGroup rootGroup = hfile.getRootGroup();
		NhDimension theDim = rootGroup.addDimension("tableRowDimention", schema.dimSize);
		
		if (schema.globalAttr != null) {
			for (Map.Entry<String, String> attr: schema.globalAttr.entrySet()){
				rootGroup.addAttribute(attr.getKey(), NhVariable.TP_STRING_VAR, attr.getValue());
			}
		}

		vars = new NhVariable[schema.variables.size()];
		for(int i = 0; i < vars.length; i++){
			NetCdfVariableInfo vInfo = schema.variables.get(i);
			vars[i] = rootGroup.addVariable(
				vInfo.name,
				getNujanVariableType(vInfo.dataType),
				new NhDimension[]{theDim},
				new int[]{1}, //writing row by row, thus chunk size = 1
				null, //no fill value specified, for now
				0 //no compression
			);
			//TODO Add variable attributes from the schema here
		}
		hfile.endDefine();
	}

	@Override
	public NetCdfSchema getSchema() {
		return schema;
	}

	@Override
	public void writeRow(Object[] row) throws IOException{
		try{
			for(int i = 0; i < vars.length; i++){
				NhVariable var = vars[i];
				Object rawData = getTypedArray(var.getType(), row[i]);
				vars[i].writeData(startIdx, rawData, true);
			}
			startIdx[0]++;
		}catch(NhException e){
			throw new IOException(e.getMessage(), e);
		}
	}

	@Override
	public void writeArray(Array arr, int variableIndex) {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException{
		try{
			this.hfile.close();
		}catch (NhException e){
			throw new IOException(e.getMessage(), e);
		}
	}

	public static int getNujanVariableType(DataType dt) throws NhException{
		switch(dt){
			case INT: return NhVariable.TP_INT;
			case LONG: return NhVariable.TP_LONG;
			case FLOAT: return NhVariable.TP_FLOAT;
			case DOUBLE: return NhVariable.TP_DOUBLE;
			case STRING: return NhVariable.TP_STRING_VAR;
			default: throw new NhException("Unsupported data type " + dt.name());
		}
	}

	private static Object getTypedArray(int nhDatatype, Object cellValue) throws NhException{
		switch(nhDatatype){
		case NhVariable.TP_INT: return new int[]{(int)cellValue};
		case NhVariable.TP_LONG: return new long[]{(long)cellValue};
		case NhVariable.TP_FLOAT: return new float[]{(float)cellValue};
		case NhVariable.TP_DOUBLE: return new double[]{(double)cellValue};
		case NhVariable.TP_STRING_VAR: return new String[]{(String)cellValue};
		default: throw new NhException("Unsupported Nujan variable data type " + nhDatatype);
	}
	}

}
