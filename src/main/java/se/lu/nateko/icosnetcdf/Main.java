package se.lu.nateko.icosnetcdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.*;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.ma2.MAMath;


public class Main {
	
	public static void main(String[] args) throws InvalidRangeException, IOException{
//		String filename = "/home/roger/ICOS/L3_from_LSCE/CO2_EUROPE_LSCE.nc";

		NetcdfFile ncfile = null;
	    
	    //Test NetCDF files
	    createNetCDF("/disk/ICOS/NetCDF_test/create/newNetCDF.nc", readCSV("/disk/ICOS/InGOS/PAL-155-CH4-ingos_0.csv"));
	    getSlice();
	    //findSubset();
//	    dumpNetCdfTestFiles();
//	    aggregate();
		
//		try {
//		    ncfile = NetcdfFile.open(filename);
		    
//		    ncdump(ncfile);
//		    detailedInfo(ncfile);
//		    extractPsurfValues(ncfile);
//		    extractCO2Values(ncfile);
//		    extractPsurfByTimeAndPos(ncfile);
//		    statistics();
//		} catch (IOException ioe) {
//
//		} finally { 
//			if (null != ncfile) try {
//		      ncfile.close();
//		    } catch (IOException ioe) {
//
//		    }
//		}
	}
	
	private static class dataStruct{
		public int time;
		public float ch4, stDev;
		
		//constructor
	    public dataStruct(int _time, float _ch4, float _stdDev) {
	        time = _time;
	        ch4 = _ch4;
	        stDev = _stdDev;
	    }
	}
	
	private static List<dataStruct> readCSV(String fileName){
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";
		Calendar startDateTime = DatatypeConverter.parseDateTime("2010-01-01T00:00:00Z");
		LocalDateTime startDate = new LocalDateTime(startDateTime.getTimeInMillis());
		List<dataStruct> fileData = new ArrayList<dataStruct>();

		try {
			br = new BufferedReader(new FileReader(fileName));
			//Read past the header
			line = br.readLine();
			
			while ((line = br.readLine()) != null) {
				String[] record = line.split(cvsSplitBy);
				String dateTimeStr = record[1] + "-" + record[2] + "-" + record[3] + "T" + record[4] + ":" + record[5] + ":" + record[6] + "Z";
				//System.out.println(dateTimeStr);
				Calendar cal = DatatypeConverter.parseDateTime(dateTimeStr);
				int hoursDelta = Hours.hoursBetween(startDate, new LocalDateTime(cal.getTimeInMillis())).getHours();
				
				dataStruct ds = new dataStruct(hoursDelta, Float.parseFloat(record[10]), Float.parseFloat(record[11]));
				fileData.add(ds);
				
//				System.out.println("record [Site=" + record[0]
//						+ "  Year=" + record[1]
//						+ "  Month=" + record[2]
//						+ "  Day=" + record[3]
//						+ "  Hour=" + record[4]
//						+ "  Minute=" + record[5]
//						+ "  Second=" + record[6]
//						+ "  hoursDelta=" + ds.time
//						+ "  ch4=" + ds.ch4
//						+ "  Stdev=" + ds.stDev
//						//+ "  DateTime=" + cal.getTime().toString()
//						+ "  hoursDelta=" + hoursDelta  
//						//+ "  ch4=" + record[10]
//						//+ "  Stdev=" + record[11]
//						+ "]");
			}
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		System.out.println("Done");
		
		return fileData;
	}
	
	private static void createNetCDF(String fileName, List<dataStruct> fileData){
		try {
			NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, fileName, null);
			
			// add dimensions
			Dimension tstep = writer.addDimension(null, "tstep", fileData.size());
			List<Dimension> dims = new ArrayList<Dimension>();
		    dims.add(tstep);
			
			// add Variable int timestp(tstep)
		    Variable timeV = writer.addVariable(null, "timestp", DataType.INT, dims);
		    timeV.addAttribute(new Attribute("title", "time steps"));
		    timeV.addAttribute(new Attribute("units", "time steps (hours) since 2010-01-01T00:00:00Z"));
		    timeV.addAttribute(new Attribute("long_name", "time steps (hours) since 2010-01-01T00:00:00Z"));
		    
		    // add Variable float ch4(tstep)
		    Variable ch4V = writer.addVariable(null, "ch4", DataType.FLOAT, dims);
		    ch4V.addAttribute(new Attribute("title", "CH4"));
		    ch4V.addAttribute(new Attribute("units", "Squirrels per bucket"));
		    ch4V.addAttribute(new Attribute("long_name", "CH4 concentration meassured in squirrels/bucket"));
		    
		    // add Variable float Stdev(tstep)
		    Variable stdDevV = writer.addVariable(null, "Stdev", DataType.FLOAT, dims);
		    stdDevV.addAttribute(new Attribute("title", "Stdev"));
		    stdDevV.addAttribute(new Attribute("units", "None"));
		    stdDevV.addAttribute(new Attribute("long_name", "Standard deviation"));
		    
		    // add global attributes
		    writer.addGroupAttribute(null, new Attribute("yo", "face"));
		    writer.addGroupAttribute(null, new Attribute("versionD", 1.2));
		    writer.addGroupAttribute(null, new Attribute("versionF", (float) 1.2));
		    writer.addGroupAttribute(null, new Attribute("versionI", 1));
		    writer.addGroupAttribute(null, new Attribute("versionS", (short) 2));
		    writer.addGroupAttribute(null, new Attribute("versionB", (byte) 3));

		    // create the file
		    writer.create();
		    
		 // write data from fileData
		    int[] shapeTime = timeV.getShape();
		    ArrayInt timeA = new ArrayInt(shapeTime);
		    int i;
		    Index imaTime = timeA.getIndex();
		    for (i = 0; i < shapeTime[0]; i++) {
		    	timeA.setInt(imaTime.set(i), fileData.get(i).time);
		    }
		    
		    writer.write(timeV, timeA);
		    
		    // write data from fileData
		    int[] shapeCH4 = ch4V.getShape();
		    ArrayFloat ch4A = new ArrayFloat(shapeCH4);
		    Index imaCH4 = ch4A.getIndex();
		    for (i = 0; i < shapeCH4[0]; i++) {
		    	ch4A.setFloat(imaCH4.set(i), fileData.get(i).ch4);
		    }
		    
		    writer.write(ch4V, ch4A);
		    
		    // write data from fileData
		    int[] shapeStdDev = stdDevV.getShape();
		    ArrayFloat stdDevA = new ArrayFloat(shapeStdDev);
		    Index imaStdDev = stdDevA.getIndex();
		    for (i = 0; i < shapeStdDev[0]; i++) {
		    	stdDevA.setFloat(imaStdDev.set(i), fileData.get(i).stDev);
		    }
		    
		    writer.write(stdDevV, stdDevA);
		    
		    writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
	}
	
	private static void getSlice(){
		String varName = "Tair";
		String outFile = "/home/roger/ICOS/LWdown_daily_WFDEI/txt/Tair_slice.txt";
		NetcdfFile ncfile = null;
		
		try {
			ncfile = NetcdfDataset.open("/home/roger/ICOS/LWdown_daily_WFDEI/Tair_daily_WFDEI_joined.nc" );
			//ncfile = NetcdfDataset.open("/disk/Tair_daily_WFDEI_joined.nc" );
			Variable v = ncfile.findVariable(varName);
			
			long start = System.currentTimeMillis();
			
			Array data = v.read("10000:10100, :, :");
			
			long now = System.currentTimeMillis();
		    System.out.println("Execute time for getSlice: " + (now - start) + " ms");
		    
			NCdumpW.printArray(data, varName, new PrintWriter(outFile), null);
			
		} catch (IOException ioe) {
			System.out.println("IOException: " + ioe.getMessage());
		} catch (InvalidRangeException e) {
			System.out.println("InvalidRangeException: " + e.getMessage());
		}
	}
	
	private static void findSubset() throws InvalidRangeException, IOException {
		GridDataset gridDataset = GridDataset.open("/home/roger/ICOS/LWdown_daily_WFDEI/Tair_daily_WFDEI_90.nc");
		GeoGrid grid = gridDataset.findGridByName("Tair");
		
		System.out.println("Grid= "+grid+" section="+ new Section(grid.getShape()));
		System.out.println(" coordSys= "+grid.getCoordinateSystem());
		
		//GeoGrid subset = (GeoGrid) grid.makeSubset(new Range(0, 0), null, new Range(1,1), null, null, null);
		// Pint at Lat=2, Lon=2
		LatLonPoint p = new LatLonPointImpl(2, 2);
		// Bounding box with start at p and then delta 5 for lat and lon
		LatLonRect bBx = new LatLonRect(p, 5, 5);
		// Slice out a subset for all x, y, z inside bounding box
		GeoGrid subset = grid.subset(null, null, bBx, 1, 1, 1);
		System.out.println("subset= "+subset+" section="+ new Section(subset.getShape()));
		System.out.println(" coordSys= "+subset.getCoordinateSystem());
		 
		gridDataset.close();
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
	
	private static void statistics(){
		String varName = "Tair";
		String outFile = "/home/roger/ICOS/LWdown_daily_WFDEI/txt/stat.txt";
		NetcdfFile ncfile = null;
		
		try {
			long start = System.currentTimeMillis();
			ncfile = NetcdfFile.open("/home/roger/ICOS/LWdown_daily_WFDEI/Tair_daily_WFDEI_joined.nc");
			
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