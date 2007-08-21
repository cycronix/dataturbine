package com.ibm.webdav;

/*
 * (C) Copyright IBM Corp. 2000  All rights reserved.
 *
 * The program is provided "AS IS" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 * 
 * Portions Copyright (C) Simulacra Media Ltd, 2004.
 */

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * <code>SimpleRFC1123DateFormat</code> is a concrete class for formatting and
 * parsing RFC 1123 format dates. It allows for formatting
 * (date -> text), parsing (text -> date), and normalization.
 *
 * @see          java.util.Calendar
 * @see          java.util.GregorianCalendar
 * @see          java.util.TimeZone
 * @see          DateFormat
 * @see          DateFormatSymbols
 * @see          DecimalFormat
 * @version      1.31 04/22/98
 * @author       Mark Davis, Chen-Lieh Huang, Alan Liu, Jason Crawford
 */
public class SimpleRFC1123DateFormat extends DateFormat {

	// the official serial version ID which says cryptically
	// which version we're compatible with
	static final long serialVersionUID = 4774881970558875024L;

	// the internal serial version which says which version was written
	// - 0 (default) for version up to JDK 1.1.3
	// - 1 for version from JDK 1.1.4, which includes a new field
	static final int currentSerialVersion = 1;
	private int serialVersionOnStream = currentSerialVersion;

	static Hashtable htMonths = new Hashtable();
	static {
		htMonths.put("Jan", new Integer(1));
		htMonths.put("Feb", new Integer(2));
		htMonths.put("Mar", new Integer(3));
		htMonths.put("Apr", new Integer(4));
		htMonths.put("May", new Integer(5));
		htMonths.put("Jun", new Integer(6));
		htMonths.put("Jul", new Integer(7));
		htMonths.put("Aug", new Integer(8));
		htMonths.put("Sep", new Integer(9));
		htMonths.put("Oct", new Integer(10));
		htMonths.put("Nov", new Integer(11));
		htMonths.put("Dec", new Integer(12));
	};
	
/**
 * Construct a SimpleDateFormat using the default pattern for the default
 * locale.  <b>Note:</b> Not all locales support SimpleDateFormat; for full
 * generality, use the factory methods in the DateFormat class.
 *
 * @see java.text.DateFormat
 */
public SimpleRFC1123DateFormat() {
	initialize();
}
/**
 * Overrides Cloneable
 */
public Object clone() {
	SimpleRFC1123DateFormat other = (SimpleRFC1123DateFormat) super.clone();
//        other.formatData = (DateFormatSymbols) formatData.clone();
	return other;
}
/**
 * Override equals.
 */
public boolean equals(Object obj) {
	if (!super.equals(obj))
		return false; // super does class check

	// todo: I think we are supposed to check if they are equivalent, but for now a class check will do.  In fact I think 
	//    just being the same class is adequate.  

	return true;
}
/**
 * Overrides DateFormat
 * <p>Formats a date or time, which is the standard millis
 * since January 1, 1970, 00:00:00 GMT.
 * @param date the date-time value to be formatted into a date-time string.
 * @param toAppendTo where the new date-time text is to be appended.
 * @param pos the formatting position. On input: an alignment field,
 * if desired. On output: the offsets of the alignment field.
 * @return the formatted date-time string.
 * @see java.util.DateFormat
 */
public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
	java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'");
	df.setTimeZone(TimeZone.getTimeZone("GMT"));
	df.format(date, toAppendTo, pos);
	return toAppendTo;
}
/* Initialize calendar and numberFormat fields */

private void initialize() {
	// The format object must be constructed using the symbols for this zone.
	// However, the calendar should use the current default TimeZone.
	// If this is not contained in the locale zone strings, then the zone
	// will be formatted using generic GMT+/-H:MM nomenclature.
	TimeZone tz = TimeZone.getTimeZone("UDT");
	calendar = Calendar.getInstance(tz);
	// numberFormat isn't used by us, but we inherit behavior that we have to respect.
	NumberFormat numberFormat = NumberFormat.getInstance();
	/*
	numberFormat.setGroupingUsed(false);
	if (numberFormat instanceof DecimalFormat)
	((DecimalFormat)numberFormat).setDecimalSeparatorAlwaysShown(false);
	numberFormat.setParseIntegerOnly(true); // So that dd.mm.yy can be parsed 
	numberFormat.setMinimumFractionDigits(0); // To prevent "Jan 1.00, 1997.00"
	*/
	setNumberFormat(numberFormat);
	/*
	initializeDefaultCentury();
	*/
}
public static void main(String args[]) {
	SimpleRFC1123DateFormat sr = new SimpleRFC1123DateFormat();
	java.text.ParsePosition pp = new java.text.ParsePosition(0);
	Date dt = null;
	String arg1 = null;
	
	arg1 = "Sunday, 05-Jan-2000 08:42:03 GMT";
	dt = sr.parse( arg1, new java.text.ParsePosition(0) );  System.out.println( arg1+" gives "+dt );
	arg1 = "Sun, 05-Jan-98 08:42:03 GMT";
	dt = sr.parse( arg1, new java.text.ParsePosition(0) );  System.out.println( arg1+" gives "+dt );
	StringBuffer sbb = new StringBuffer();
	sr.format( dt, sbb, new java.text.FieldPosition(0) );
	System.out.println( sbb );
	System.exit(0);
}
/**
 * Overrides DateFormat
 * @see java.util.DateFormat
 */
public Date parse(String text, java.text.ParsePosition pos) {
	int start0 = pos.getIndex();
	Calendar calendar = getCalendar();
	calendar.clear(); // Clears all the time fields

	int start = text.indexOf( ',', start0);
	if (-1 == start) return null;

	start += 2;
	int day = Integer.parseInt(text.substring(start + 0, start + 2));
	String stMonth = text.substring(start + 3, start + 6);
	Integer itMonth = (Integer)htMonths.get(stMonth);
	if (itMonth==null) return null;
	int month = itMonth.intValue();
	
	char pivotChar = text.charAt( start+9);
	int year;
	if ( (pivotChar < '0') || (pivotChar > '9')) {
		// apparently a two digit year
		year = Integer.parseInt(text.substring(start+7, start+9));
		if (year<40) { year += 2000; } else { year += 1900; };
		start -= 2;
	} else {	
		year = Integer.parseInt(text.substring(start+7, start+11));
	}
	int hour = Integer.parseInt(text.substring(start + 12, start + 14));
	int minute = Integer.parseInt(text.substring(start + 15, start + 17));
	int second = Integer.parseInt(text.substring(start + 18, start + 20));
	String stGMT = text.substring( start + 21, start + 24);
	if (!stGMT.equals("GMT")) {
		return null;
	} else {
		calendar.set(Calendar.ZONE_OFFSET, /*GMT:*/0 ); // NYC is ZONE_OFFSET=5 or 4
		//calendar.set( Calendar.DST_OFFSET, sum );
		pos.setIndex(start + 29);
	}


	calendar.set(year, month - 1, day, hour, minute, second);
	Date retval = calendar.getTime();
	//System.out.println( "     "+ text + "******" + retval );
	return retval;
}
/**
 * Override readObject.
 */
private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
	stream.defaultReadObject();
	/*
	 if (serialVersionOnStream < 1) {
	 // didn't have defaultCenturyStart field
	 initializeDefaultCentury();
	 }
	 else {
	 // fill in dependent transient field
	 parseAmbiguousDatesAsAfter(defaultCenturyStart);
	 }
	*/
	serialVersionOnStream = currentSerialVersion;
}
}
