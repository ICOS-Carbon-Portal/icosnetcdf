package se.lu.nateko.icosnetcdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NCdumpW;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.ma2.MAMath;


public class Main {
	
	public static void main(String[] args) throws InvalidRangeException{
		String filename = "/home/roger/ICOS/L3_from_LSCE/CO2_EUROPE_LSCE.nc";

		long start = System.currentTimeMillis();
		NetcdfFile ncfile = null;
		long now = System.currentTimeMillis();
	    System.out.println("Open file: " + (now - start) + " ms");
		
		try {
		    ncfile = NetcdfFile.open(filename);
		    
		    dumpNetCdfTestFiles();
		    aggregate();
		    ncdump(ncfile);
		    detailedInfo(ncfile);
		    extractPsurfValues(ncfile);
		    extractCO2Values(ncfile);
		    extractPsurfByTimeAndPos(ncfile);
		    statistics(ncfile);
		} catch (IOException ioe) {

		} finally { 
			if (null != ncfile) try {
		      ncfile.close();
		    } catch (IOException ioe) {

		    }
		}
	}
	private static void dumpNetCdfTestFiles(){
		NetcdfFile ncfile = null;
		String cldcFile = "/home/roger/ICOS/NetCDF_test/cldc.mean.nc";
		String lflxFile = "/home/roger/ICOS/NetCDF_test/lflx.mean.nc";
		
		try{
			ncfile = NetcdfFile.open(cldcFile);
			
			File file = new File ("/home/roger/ICOS/NetCDF_test/cldc_dump.txt");
		    PrintWriter printWriter = new PrintWriter(file);
		    printWriter.println (ncfile);
		    printWriter.close();
		    
		} catch (IOException ioe) {

		} finally { 
			if (null != ncfile) try {
		      ncfile.close();
		    } catch (IOException ioe) {

		    }
		}
		
		try{
			ncfile = NetcdfFile.open(lflxFile);
			
			File file = new File ("/home/roger/ICOS/NetCDF_test/lflx_dump.txt");
		    PrintWriter printWriter = new PrintWriter(file);
		    printWriter.println (ncfile);
		    printWriter.close();
		    
		} catch (IOException ioe) {

		} finally { 
			if (null != ncfile) try {
		      ncfile.close();
		    } catch (IOException ioe) {

		    }
		}
	}
	
	private static void aggregate() throws InvalidRangeException {
		NetcdfDataset ncFiles = null;
		
		try{
			ncFiles = NetcdfDataset.openDataset("/home/roger/ICOS/NetCDF_test/aggUnionSimple.ncml");
			
			File file = new File ("/home/roger/ICOS/NetCDF_test/fileInfo.txt");
		    PrintWriter printWriter = new PrintWriter(file);
		    printWriter.println (ncFiles.getDetailInfo());
		    printWriter.println (ncFiles);
		    printWriter.close ();
		    
		    String varName = "lat";
			String outFile = "/home/roger/ICOS/NetCDF_test/lat.txt";
			
			Variable v = ncFiles.findVariable(varName);
			Array data = v.read(":");
			
			NCdumpW.printArray(data, varName, new PrintWriter(outFile), null);
		    
		}catch (IOException ioe) {
			System.out.println("IOException: " + ioe.getMessage());
		} finally { 
			if (null != ncFiles) try {
				ncFiles.close();
		    } catch (IOException ioe) {
		    	System.out.println("IOException: " + ioe.getMessage());
		    }
		}
	}
	
	private static void statistics(NetcdfFile ncfile){
		String varName = "psurf";
		String outFile = "/home/roger/ICOS/L3_from_LSCE/stat.txt";
		
		try {
			long start = System.currentTimeMillis();
			
			Variable v = ncfile.findVariable(varName);
			Array data = v.read();
			double min = MAMath.getMinimum(data);
			double max = MAMath.getMaximum(data);
			
			long now = System.currentTimeMillis();
		    System.out.println("Execute time for min and max: " + (now - start) + " ms");
			
			File file = new File (outFile);
		    PrintWriter printWriter = new PrintWriter(file);
		    printWriter.println ("min: " + min);
		    printWriter.println ("max: " + max);
		    printWriter.close ();
			
		} catch (IOException ioe) {

		}
	}
	
	private static void extractPsurfByTimeAndPos(NetcdfFile ncfile){
		//float psurf(time, degrees_north, degrees_east)
				
		String varName = "psurf";
		String outFile3D = "/home/roger/ICOS/L3_from_LSCE/psurf3D.txt";
		String outFile2D = "/home/roger/ICOS/L3_from_LSCE/psurf2D.txt";
		
		try {
			int[] origin = new int[] {2, 0, 0};
			int[] size = new int[] {1, 71, 101};
			
			long start = System.currentTimeMillis();
			Variable v = ncfile.findVariable(varName);
			
			Array data3D = v.read(origin, size);
			Array data2D = data3D.reduce();
			
			long now = System.currentTimeMillis();
		    System.out.println("Execute time for extractPsurfByTimeAndPos: " + (now - start) + " ms");
		    
			NCdumpW.printArray(data3D, varName, new PrintWriter(outFile3D), null);
			NCdumpW.printArray(data2D, varName, new PrintWriter(outFile2D), null);
			
		} catch (IOException ioe) {

		} catch (InvalidRangeException e) {

		}
	}
	
	private static void extractPsurfValues(NetcdfFile ncfile){
		String varName = "psurf";
		String outFile = "/home/roger/ICOS/L3_from_LSCE/psurf.txt";
		
		try {
			long start = System.currentTimeMillis();
			Variable v = ncfile.findVariable(varName);
			Array data = v.read("118:120, 68:70, 98:100");
			
			long now = System.currentTimeMillis();
		    System.out.println("Execute time for extractPsurfValues: " + (now - start) + " ms");
		    
			NCdumpW.printArray(data, varName, new PrintWriter(outFile), null);
			
		} catch (IOException ioe) {
			System.out.println("IOException: " + ioe.getMessage());
		} catch (InvalidRangeException e) {
			System.out.println("InvalidRangeException: " + e.getMessage());
		}
	}
	
	private static void extractCO2Values(NetcdfFile ncfile){
		String varName = "CO2";
		String outFile = "/home/roger/ICOS/L3_from_LSCE/CO2.txt";
		
		try {
			long start = System.currentTimeMillis();
			Variable v = ncfile.findVariable(varName);
			Array data = v.read("0:2, 0:2, 0:2, 0:2");
			
			long now = System.currentTimeMillis();
		    System.out.println("Execute time for extractCO2Values: " + (now - start) + " ms");
		    
			NCdumpW.printArray(data, varName, new PrintWriter(outFile), null);
			
		} catch (IOException ioe) {

		} catch (InvalidRangeException e) {

		}
	}
	
	private static void detailedInfo(NetcdfFile ncfile) throws FileNotFoundException {
		long start = System.currentTimeMillis();
		
		File file = new File ("/home/roger/ICOS/L3_from_LSCE/fileInfo.txt");
	    PrintWriter printWriter = new PrintWriter(file);
	    printWriter.println (ncfile.getDetailInfo());
	    printWriter.close ();
		
		long now = System.currentTimeMillis();
	    System.out.println("Execute time for detailedInfo: " + (now - start) + " ms");
	}
	
	private static void ncdump(NetcdfFile ncfile) throws FileNotFoundException {
		long start = System.currentTimeMillis();
		
		File file = new File ("/home/roger/ICOS/L3_from_LSCE/ncdump.txt");
	    PrintWriter printWriter = new PrintWriter(file);
	    printWriter.println (ncfile);
	    printWriter.close ();
		
		long now = System.currentTimeMillis();
	    System.out.println("Execute time for ncdump: " + (now - start) + " ms");
	}

}