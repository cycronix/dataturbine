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
 * <code>SimpleISO8601DateFormat</code> is a concrete class for formatting and
 * parsing ISO 8601 format dates. It allows for formatting
 * (date -> text), parsing (text -> date), and normalization.
 *
 * @see          java.util.Calendar
 * @see          java.util.GregorianCalendar
 * @see          java.util.TimeZone
 * @see          DateFormat
 * @see          DateFormatSymbols
 * @see          DecimalFormat
 * @version      1.31 04/22/98
 * @author       Mark Davis, Chen-Lieh Huang, Alan Liu
 */
public class SimpleISO8601DateFormat extends DateFormat {

	// the official serial version ID which says cryptically
	// which version we're compatible with
	static final long serialVersionUID = 4774881970558875024L;

	// the internal serial version which says which version was written
	// - 0 (default) for version up to JDK 1.1.3
	// - 1 for version from JDK 1.1.4, which includes a new field
	static final int currentSerialVersion = 1;
	private int serialVersionOnStream = currentSerialVersion;
/**
 * Construct a SimpleDateFormat using the default pattern for the default
 * locale.  <b>Note:</b> Not all locales support SimpleDateFormat; for full
 * generality, use the factory methods in the DateFormat class.
 *
 * @see java.text.DateFormat
 */
public SimpleISO8601DateFormat() {
	initialize();
}
/**
 * Overrides Cloneable
 */
public Object clone() {
	SimpleISO8601DateFormat other = (SimpleISO8601DateFormat) super.clone();
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
	java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	df.setTimeZone(TimeZone.getTimeZone("GMT"));
	df.format(date, toAppendTo, pos);
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(date);
	int dstoff = calendar.get(Calendar.DST_OFFSET);
	int tzoff = calendar.get(Calendar.ZONE_OFFSET);
	
	tzoff += dstoff;
	tzoff /= (1000 * 60);
	if (tzoff != 0) {
		if (tzoff < 0) {
			toAppendTo.append("-");
			tzoff *= -1;
		} else {
			toAppendTo.append("+");
		}
		int hr = tzoff / 60;
		if (hr < 10)
			toAppendTo.append("0");
		toAppendTo.append("" + hr + ":");
		int mn = tzoff % 60;
		if (mn < 10)
			toAppendTo.append("0");
		toAppendTo.append(mn);
	} else {
		toAppendTo.append("Z");
	}
	//System.out.println( toAppendTo );
	/*
	   Calendar calendar = getCalendar();
	   // Initialize
	   pos.beginIndex = pos.endIndex = 0;
	   int posfield = pos.getField();
	
	// Convert input date to time field list
	calendar.setTime(date);
	
	
	int tval = calendar.get(Calendar.YEAR );
	if (posfield==DateFormat.YEAR_FIELD) {
	   pos.beginIndex = toAppendTo.length();
	   pos.endIndex = pos.beginIndex+4;
	}
	toAppendTo.append( tval );
	toAppendTo.append( '-' );
	if (posfield==DateFormat.MONTH_FIELD) {
	   pos.beginIndex = toAppendTo.length();
	   pos.endIndex = pos.beginIndex+2;
	}
	tval = calendar.get(Calendar.MONTH ) + 1 ; // apparently we need to increment these
	if (tval<10) toAppendTo.append( '0' );
	toAppendTo.append( tval );
	
	toAppendTo.append( '-' );
	if (posfield==DateFormat.DATE_FIELD) {
	   pos.beginIndex = toAppendTo.length();
	   pos.endIndex = pos.beginIndex+2;
	}
	tval = calendar.get(Calendar.DAY_OF_MONTH );
	if (tval<10) toAppendTo.append( '0' );
	toAppendTo.append( tval );
	
	toAppendTo.append( 'T' );
	if (posfield==DateFormat.HOUR_OF_DAY0_FIELD) {
	   pos.beginIndex = toAppendTo.length();
	   pos.endIndex = pos.beginIndex+2;
	}
	tval = calendar.get(Calendar.HOUR );
	if (tval<10) toAppendTo.append( '0' );
	toAppendTo.append( tval );
	
	toAppendTo.append( ':' );
	if (posfield==DateFormat.MINUTE_FIELD) {
	   pos.beginIndex = toAppendTo.length();
	   pos.endIndex = pos.beginIndex+2;
	}
	tval = calendar.get(Calendar.MINUTE );
	if (tval<10) toAppendTo.append( '0' );
	toAppendTo.append( tval );
	
	toAppendTo.append( ':' );
	if (posfield==DateFormat.SECOND_FIELD) {
	   pos.beginIndex = toAppendTo.length();
	   pos.endIndex = pos.beginIndex+2;
	}
	tval = calendar.get(Calendar.SECOND );
	if (tval<10) toAppendTo.append( '0' );
	toAppendTo.append( tval );
	
	toAppendTo.append( 'Z' );
	  */
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
/**
 * Overrides DateFormat
 * @see java.util.DateFormat
 */
public Date parse(String text, java.text.ParsePosition pos) {
	int start = pos.getIndex();
	Calendar calendar = getCalendar();
	calendar.clear(); // Clears all the time fields

	int year = Integer.parseInt(text.substring(start, start + 4));
	int month = Integer.parseInt(text.substring(start + 5, start + 7));
	int day = Integer.parseInt(text.substring(start + 8, start + 10));
	int hour = Integer.parseInt(text.substring(start + 11, start + 13));
	int minute = Integer.parseInt(text.substring(start + 14, start + 16));
	int second = Integer.parseInt(text.substring(start + 17, start + 19));
	if (text.substring(start + 19, start + 20).equals("Z")) {
		// GMT
		pos.setIndex(start + 20);
	} else {
		int offhour = Integer.parseInt(text.substring(start + 20, start + 22));
		int offmin = Integer.parseInt(text.substring(start + 23, start + 25));
		int sum = (offhour * 60 + offmin) * 60 * 1000; // in millis
		if (text.substring(start + 19, start + 20).equals("-"))
			sum *= -1; // NYC is "-04:00"
		calendar.set(Calendar.ZONE_OFFSET, sum); // NYC is ZONE_OFFSET=5 or 4
		//calendar.set( Calendar.DST_OFFSET, sum );
		pos.setIndex(start + 25);
	}

	// todo: return position.

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
