/*
Copyright 2011 Erigo Technologies LLC

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/

/*****************************************************************************
 * 
 * Class to store IWG1 data.
 * <p>
 * 
 * @author Stephen Carlson, USRP intern, NASA DFRC
 * @author John Wilson, Erigo Technologies LLC
 * 
 * @version 03/23/2010
 * 
 */

/*
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/01/2010  SC	Created
 * 03/23/2010  JPW	Integrate the content of the Parser class into this class.
 *                      Add toString()
 */

import java.lang.NumberFormatException;
import java.lang.StringIndexOutOfBoundsException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class IWG1 {
    
    private static SimpleDateFormat sdf = null;
    
    public boolean isValid = false;
    public String errorStr = "";
    
    // Tag for the head of the IWG1 string
    public final static String magic_Tag = "IWG1";
    
    // Data components for the IWG1 string
    // These fields are static so they can be used across all instances of IWG1
    // Formatted time string
    public static String time_str = "";
    // Time in seconds since epoch
    public static double time_double = 0;
    public static String coord_Lat= "";
    public static String coord_Lon= "";
    public static String alt_MSL= "";
    public static String alt_WGS84= "";
    public static String alt_Pressure= "";
    public static String alt_Radar= "";
    public static String vel_GS= "";
    public static String vel_TAS= "";
    public static String vel_IAS= "";
    public static String vel_Mach= "";
    public static String vel_VV= "";
    public static String deg_TrueHdg= "";
    public static String deg_Track= "";
    public static String deg_Drift= "";
    public static String deg_Pitch= "";
    public static String deg_Roll= "";
    public static String deg_Yaw= "";
    public static String deg_AoA= "";
    public static String temp_OAT= "";
    public static String temp_Dew= "";
    public static String temp_Total= "";
    public static String pres_Static= "";
    public static String pres_Dynamic= "";
    public static String pres_Cabin= "";
    public static String wind_Speed= "";
    public static String wind_Dir= "";
    public static String wind_VertSpeed= "";
    public static String sun_Zenith= "";
    public static String sun_ElevAcrft= "";
    public static String sun_AzGnd= "";
    public static String sun_AzAcrft= "";
    
    // The date fields (year, month, day) come from the $GPRMC string
    // These fields are static so they can be used across all instances of IWG1
    public static int year = -1;
    public static int month = -1;
    public static int day_of_month = -1;
    
    public IWG1() {
	if (IWG1.sdf == null) {
	    // Initialize the class variable if it hasn't been done yet
	    IWG1.sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	    IWG1.sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
    }
    
    public IWG1(String strI) {
    	if (IWG1.sdf == null) {
	    // Initialize the class variable if it hasn't been done yet
	    IWG1.sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	    IWG1.sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	parse(strI, true);
    }
    
    public void parse(String strI, boolean bDoChecksumI) {
	
	// If string doesn't start with "$GP", then it isn't a complete NMEA string we are interested in
	if (!strI.startsWith("$GP")) {
	    voidIWG("Unsupported or incomplete NMEA string");
	    return;
	}
	
	// Only parse GGA strings
	// if (!strI.startsWith("$GPGGA")) {
	//     voidIWG("");
	//     return;
	// }
	
	String[] strArray = strI.split(",");
	// We know that the length of this array must be at least 2; check that here
	int arrayLen = strArray.length;
	if (arrayLen < 2) {
	    voidIWG("Incomplete NMEA string");
	    return;
	}
	
	int chkSumCalc = 0x00; //Our calculation 
	int chkSumReal = 0x00; //The value in the sentence
	
	isValid = true;
	
	// Verify the checksum in the NMEA string
	// NMEA calculates the checksum based on the data from but not including the '$' all
	// the way to but not including the '*'. The data is XOR'd. The result is the byte after
	// the '*' in hexidecimal format.
	if (bDoChecksumI) {
	    try {
		for (int i=1; i<strI.length();i++) {
		    if (strI.substring(i,i+1).equals("*")) {
			chkSumReal = Integer.parseInt(strI.substring(i+1,i+3),16);
			break;
		    } else {	
			chkSumCalc ^= strI.charAt(i);
		    }

		}
	    }
	    catch(StringIndexOutOfBoundsException e) {
		voidIWG("Error calculating checksum");
		return;
	    }
	    catch(NumberFormatException e) {
		voidIWG("Error calculating checksum");
		return;
	    }

	    if(chkSumReal != chkSumCalc){
		voidIWG("Checksum mismatch: Calc'd value = " + chkSumCalc + ", actual value = " + chkSumReal);
		return;
	    }
	}
	
	// Save local data as it is parsed
	String date_ddmmyy_L;
	String time_str_L;
	double time_double_L;
	String coord_Lat_L;
	String coord_Lon_L;
	String alt_MSL_L;
	String alt_WGS84_L;
	String vel_GS_L;
	String deg_Track_L;
	
	int arrayIdx = -1;
	String token = strArray[++arrayIdx];
	if (token.equals("$GPGGA")) {
	    if (arrayLen < 15) {
		voidIWG("Incomplete $GPGGA string");
		return;
	    }
	    //$GPGGA,002237.8,3457.01415,N,11753.73771,W,2,08,1.0,715.6,M,-32.3,M,,*62
	    Calendar tempCal = getCalendar(year, month, day_of_month, strArray[++arrayIdx]); // Time field is formatted as HHMMSS.S
	    if (tempCal == null) {
		voidIWG("Time could not be established (probably because date fields are not available)");
		return;
	    }
	    Date tempDate = tempCal.getTime();
            time_str_L = sdf.format(tempDate);
            time_double_L = (tempCal.getTimeInMillis())/1000.0;
	    coord_Lat_L = coordToDouble(strArray[++arrayIdx],strArray[++arrayIdx]); //DDMM.MMMMM
	    coord_Lon_L = coordToDouble(strArray[++arrayIdx],strArray[++arrayIdx]); //DDMM.MMMMM
	    // Check the fix quality
	    if(strArray[++arrayIdx].equals("0")){
		// We'll keep this as a valid IWG1 string, only containing the timestamp
		time_str = time_str_L;
		isValid = true;
		errorStr = "Error: GGA fix is 0";
		return;
	    }
	    ++arrayIdx; //SKIP: 08, Satellites in view
	    ++arrayIdx; //SKIP: 1.0, DOP
	    alt_MSL_L = strArray[++arrayIdx]; //MSL
	    ++arrayIdx; // SKIP: M, Meters unit
	    alt_WGS84_L = strArray[++arrayIdx]; //Geoid
	    ++arrayIdx; //SKIP: M, Meters unit
	    ++arrayIdx; //SKIP: Usually blank, Time since last DGPS fix
	    ++arrayIdx; //SKIP: Usually blank, DGPS station ID number
	    // Data parsed OK, save it
	    time_str = time_str_L;
	    time_double = time_double_L;
	    coord_Lat = coord_Lat_L;
	    coord_Lon = coord_Lon_L;
	    alt_MSL = alt_MSL_L;
	    alt_WGS84 = alt_WGS84_L;
	} else if (token.equals("$GPRMC")) {
	    if (arrayLen < 12) {
		voidIWG("Incomplete $GPRMC string");
		return;
	    }
	    //$GPRMC,002237.8,A,3457.01415,N,11753.73771,W,000.00,000.0,020310,013.6,E*42
	    // Wait until we parse the date before making the time string
	    String tempTimeStr = strArray[++arrayIdx]; // Time field is formatted as HHMMSS.S
	    // Check status: A for Active, V for Void
	    if (strArray[++arrayIdx].equalsIgnoreCase("V")) {
		voidIWG("Error: RMC status is void");
		return;
	    }
	    coord_Lat_L = coordToDouble(strArray[++arrayIdx],strArray[++arrayIdx]); //DDMM.MMMMM
	    coord_Lon_L = coordToDouble(strArray[++arrayIdx],strArray[++arrayIdx]); //DDMM.MMMMM
	    vel_GS_L = knotsToMeters(strArray[++arrayIdx]); 	//GndSpeed in Knots
	    deg_Track_L = strArray[++arrayIdx]; 				//True Track
	    date_ddmmyy_L = strArray[++arrayIdx]; // Date in ddmmyy format
	    day_of_month = Integer.parseInt(date_ddmmyy_L.substring(0,2));
	    month = Integer.parseInt(date_ddmmyy_L.substring(2,4));
	    // Don't use the 2-digit year field from this string;
	    // get the 4-digit year from Calendar
	    // String tempYearStr = date_ddmmyy_L.substring(4,6);
	    // year = new String("20" + tempYearStr);
	    Calendar tempCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	    year = tempCal.get(Calendar.YEAR);
	    // Now that we have the date, get the whole time string
	    tempCal = getCalendar(year, month, day_of_month, tempTimeStr);
	    if (tempCal == null) {
		voidIWG("Time could not be established (probably because date fields are not available)");
		return;
	    }
	    Date tempDate = tempCal.getTime();
            time_str_L = sdf.format(tempDate);
            time_double_L = (tempCal.getTimeInMillis())/1000.0;
	    ++arrayIdx; // SKIP: 013.6, Magnetic variance	
	    ++arrayIdx; // SKIP: E Magnetic variance direction
	    // Data parsed OK, save it
	    time_str = time_str_L;
	    time_double = time_double_L;
	    coord_Lat = coord_Lat_L;
	    coord_Lon = coord_Lon_L;
	    vel_GS = vel_GS_L;
	    deg_Track = deg_Track_L;
	} else if (token.equals("$GPVTG")) {
	    if (arrayLen < 9) {
		voidIWG("Incomplete $GPVTG string");
		return;
	    }
	    //$GPVTG,000.0,T,346.4,M,000.00,N,0000.00,K*7B
	    deg_Track_L = strArray[++arrayIdx]; //True Track Made Good in Degrees
	    ++arrayIdx; // SKIP: Designator unit
	    ++arrayIdx; // SKIP: Magnetic Track in Degrees
	    ++arrayIdx; // SKIP: Designator unit
	    vel_GS_L = knotsToMeters(strArray[++arrayIdx]); //GndSpeed in Knots
	    ++arrayIdx; // SKIP: Designator unit
	    ++arrayIdx; // SKIP: GndSpeed in Kph
	    ++arrayIdx; // SKIP: Designator unit				
	    // Data parsed OK, save it
	    deg_Track = deg_Track_L;
	    vel_GS = vel_GS_L;
	} else {
	    // Not a supported NMEA format
	    voidIWG("Unsupported NMEA format: " + token);
	    return;
	}
	
    }
    
    private void voidIWG(String errorStrI){
	isValid = false;
	errorStr = errorStrI;
    }
    
    private String knotsToMeters(String k){
	return Double.toString(Double.parseDouble(k)*463F/900F);
    }
    
    // Create a Calendar object using the given date and time fields.
    // timeStrI comes in as a string in the format hhmmss.s; this is UTC time
    private Calendar getCalendar(int yearI, int monthI, int dayI, String timeStrI) {
    	if ( (yearI == -1) || (monthI == -1) || (dayI == -1) || (timeStrI == null) || (timeStrI.isEmpty()) ) {
	    return null;
    	}
	Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	cal.set(yearI, monthI, dayI); 
	cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStrI.substring(0,2)));
	cal.set(Calendar.MINUTE, Integer.parseInt(timeStrI.substring(2,4)));
	int dotIdx = timeStrI.indexOf('.');
	if (dotIdx == -1) {
	    // there is no '.' in the string
	    cal.set(Calendar.SECOND,Integer.parseInt(timeStrI.substring(4)));
	} else if (dotIdx == 6) {
	    cal.set(Calendar.SECOND,Integer.parseInt(timeStrI.substring(4,6)));
	} else {
	    System.err.println("ERROR: Unknown format of time string: " + timeStrI);
	    return null;
	}
	return cal;
    }
    
    private String coordToDouble(String k, String s){
	
	Double result = new Double(0.0);
	
	try{
	    if(k.indexOf('.')<=4){
		result = (double)Integer.parseInt(k.substring(0,2)) + (Double.parseDouble(k.substring(2))/60.0);
		if(s.equalsIgnoreCase("S")) result *= -1.0;
		return result.toString();
	    } else{
		result = (double)Integer.parseInt(k.substring(0,3)) + (Double.parseDouble(k.substring(3))/60.0);
		if(s.equalsIgnoreCase("W")) result *= -1.0;
		return result.toString();
	    }
	}
	catch(StringIndexOutOfBoundsException e){return "";}
    }
    
    public String toString() {
	String returnStr = errorStr; 
	if(isValid) {
	    returnStr =
		new String(
		    magic_Tag + "," +
		    time_str + "," +
		    coord_Lat + "," +
		    coord_Lon + "," +
		    alt_MSL + "," +
		    alt_WGS84 + "," +
		    alt_Pressure + "," +
		    alt_Radar + "," +
		    vel_GS + "," +
		    vel_TAS + "," +
		    vel_IAS + "," +
		    vel_Mach + "," +
		    vel_VV + "," +
		    deg_TrueHdg + "," +
		    deg_Track + "," +
		    deg_Drift + "," +
		    deg_Pitch + "," +
		    deg_Roll + "," +
		    deg_Yaw + "," +
		    deg_AoA + "," +
		    temp_OAT + "," +
		    temp_Dew + "," +
		    temp_Total + "," +
		    pres_Static + "," +
		    pres_Dynamic + "," +
		    pres_Cabin + "," +
		    wind_Speed + "," +
		    wind_Dir + "," +
		    wind_VertSpeed + "," +
		    sun_Zenith + "," +
		    sun_ElevAcrft + "," +
		    sun_AzGnd + "," +
		    sun_AzAcrft +
		    "\r\n");
	}
	return returnStr;
    }
    
}
