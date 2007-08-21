/*
Copyright 2007 Creare Inc.

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

/*
  *********************************************************************
  ***                                                               ***
  ***   Name :  ToString.java       (Extended "toString" functions) ***
  ***   By   :  Ian Brown           (Creare Inc., Hanover, NH)      ***
  ***   For  :  FlyScan                                             ***
  ***   Date :  April, 1998 - February, 1999			    ***
  ***                                                               ***
  ***   Copyright 1998, 1999, 2002 Creare Inc.			    ***
  ***   All Rights Reserved                                         ***
  ***                                                               ***
  ***   Description :                                               ***
  ***      This class contains a set of "toString" methods that     ***
  ***   provide much more control over the output format used. The  ***
  ***   methods use a slightly modified subset of the standard "C"  ***
  ***   printf-style format strings to describe the output.         ***
  ***                                                               ***
  ***      The format field consists of:                            ***
  ***                                                               ***
  ***      o - The % character. This is optional, but is included   ***
  ***          for the similarity with the "C" printf formats.      ***
  ***                                                               ***
  ***      o - Zero or more of the following flags:                 ***
  ***                                                               ***
  ***          *  (asterisk) indicates that the field width is      ***
  ***             "fixed". If the value to be displayed exceeds the ***
  ***             field width, the value is replaced with *'s as in ***
  ***             FORTRAN.                                          ***
  ***                                                               ***
  ***          -  (a negative field width flag) indicates that the  ***
  ***             converted value is to be left rather than right   ***
  ***             justified.                                        ***
  ***                                                               ***
  ***         ' ' (a space) indicates that a positive value is      ***
  ***             preceeded by a blank to align the value with the  ***
  ***             digits of a negative value.                       ***
  ***                                                               ***
  ***          +  indicates that the sign of the value is always    ***
  ***             shown.                                            ***
  ***                                                               ***
  ***      o - An optional decimal digit string specifying a        ***
  ***          minimum field width. If the value has fewer          ***
  ***          characters than the field width, it will be padded   ***
  ***          with spaces on the left (or right gived the '-' flag ***
  ***          is specified). If the value has more characters than ***
  ***          the field width, the width is extended (unless the   ***
  ***          '*' flag is specified).                              ***
  ***                                                               ***
  ***      o - An optional precision, in the form of a period ('.') ***
  ***          followed by an option digit string. If the digit     ***
  ***          string is omitted, the precision is taken as zero.   ***
  ***          This gives the minimum number of digits to appear    ***
  ***          for d, i, o, and x conversions, the number of digits ***
  ***          to appear after the decimal-point for e and f        ***
  ***          conversions, the maximum number of significant       ***
  ***          digits for the g conversions, and the maximum number ***
  ***          of characters to be printed from a string for s      ***
  ***          conversions.                                         ***
  ***                                                               ***
  ***      The conversion specifiers are:                           ***
  ***                                                               ***
  ***      i   A decimal integer.                                   ***
  ***                                                               ***
  ***      o   An octal integer.                                    ***
  ***                                                               ***
  ***      x   A hexadecimal integer.                               ***
  ***                                                               ***
  ***      e   Exponential floating point format.                   ***
  ***                                                               ***
  ***      f   Fixed point format.                                  ***
  ***                                                               ***
  ***      g   Selects either e or f format. The f format is used   ***
  ***          unless the exponent is less than -4 or greater than  ***
  ***          or equal to the precision (the default precision is  ***
  ***          6 or the field width for fixed width).               ***
  ***                                                               ***
  ***      s   A string.                                            ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/06/2002 - INB					    ***
  ***		Fixed a rounding problem in fixedConversion.	    ***
  ***	   02/11/2002 - INB					    ***
  ***		Fixed a similar problem in exponentialConversion.   ***
  ***                                                               ***
  *********************************************************************
*/
package com.rbnb.utility;

public class ToString {

    /* Public constants: */
    public final static int   Unknown = 0,  // Unknown conversion type.
			      format_i = 1, // 'i' conversion format.
			      format_o = 2, // 'o' conversion format.
			      format_x = 3, // 'x' conversion format.
			      format_e = 4, // 'e' conversion format.
			      format_f = 5, // 'f' conversion format.
			      format_g = 6, // 'g' conversion format.
			      format_s = 7, // 's' conversion format.
			      None = 0,     // No positive sign display.
			      Blank = 1,    // Display positive sign as blank.
			      Plus = 2;     // Display positive sign as +.

                                            // Conversion formats.
    public final static char  conversions[] = {
                                '?',
				'i',
				'o',
				'x',
				'e',
				'f',
				'g',
				's'
                              },
			      signs[] = {   // Positive sign selections.
                               '?',
			       ' ',
			       '+'
			      },
			      fixed = '*',  // Fixed format indicator.
			      left = '-';   // Left justification indicator.

/*
  *********************************************************************
  ***                                                               ***
  ***   Name :  ToString            (Constructor: default)          ***
  ***   By   :  Ian Brown           (Creare Inc., Hanover, NH)      ***
  ***   For  :  FlyScan                                             ***
  ***   Date :  April, 1998                                         ***
  ***                                                               ***
  ***   Copyright 1998 Creare Inc.                                  ***
  ***   All Rights Reserved                                         ***
  ***                                                               ***
  ***   Description :                                               ***
  ***      This constructor builds a default ToString object.       ***
  ***                                                               ***
  *********************************************************************
*/
  public ToString() {
  }

/*
  *********************************************************************
  ***                                                               ***
  ***   Name :  convertFormat       (Convert a format)              ***
  ***   By   :  Ian Brown           (Creare Inc., Hanover, NH)      ***
  ***   For  :  FlyScan                                             ***
  ***   Date :  April, 1998                                         ***
  ***                                                               ***
  ***   Copyright 1998 Creare Inc.                                  ***
  ***   All Rights Reserved                                         ***
  ***                                                               ***
  ***   Description :                                               ***
  ***      This method converts the input format to a format object ***
  ***   and returns it.                                             ***
  ***                                                               ***
  ***   Input :                                                     ***
  ***      formatI                  The format to convert.          ***
  ***                                                               ***
  ***   Returns :                                                   ***
  ***      convertFormat            The format object.              ***
  ***                                                               ***
  *********************************************************************
*/
  Format convertFormat(String formatI) throws Exception {
    return (new Format(formatI));
  }

/*
  *********************************************************************
  ***                                                               ***
  ***   Name :  toString            (Long to string)                ***
  ***   By   :  Ian Brown           (Creare Inc., Hanover, NH)      ***
  ***   For  :  FlyScan                                             ***
  ***   Date :  April, 1998                                         ***
  ***                                                               ***
  ***   Copyright 1998 Creare Inc.                                  ***
  ***   All Rights Reserved                                         ***
  ***                                                               ***
  ***   Description :                                               ***
  ***      This method converts a long to a string using the        ***
  ***   specified format.                                           ***
  ***                                                               ***
  ***   Input :                                                     ***
  ***      formatI                  The format string.  Only the    ***
  ***                               i, u, and x formats are used.   ***
  ***      longI                    The long value.                 ***
  ***                                                               ***
  ***   Returns :                                                   ***
  ***      toString                 The string.                     ***
  ***                                                               ***
  *********************************************************************
*/
  public static String toString
    (String formatI,
     long longI)
  throws Exception {
    ToString tString = new ToString();
    Format   format = tString.convertFormat(formatI);

    /* Handle this based on the conversion format. */
    switch (format.conversion) {

      /* Decimal integer format. */
      case format_i: return (integerConversion(format,longI,10));

      /* Octal integer format. */
      case format_o: return (integerConversion(format,longI,8));

      /* Hexadecimal integer format. */
      case format_x: return (integerConversion(format,longI,16));

      /* Nothing else is supported. */
      default:
	throw new Exception
	  ("Format conversion is not supported for longs " + formatI + ".");
    }
  }

/*
  *********************************************************************
  ***                                                               ***
  ***   Name :  integerConversion   (Integer conversion)            ***
  ***   By   :  Ian Brown           (Creare Inc., Hanover, NH)      ***
  ***   For  :  FlyScan                                             ***
  ***   Date :  April - July, 1998                                  ***
  ***                                                               ***
  ***   Copyright 1998 Creare Inc.	                            ***
  ***   All Rights Reserved                                         ***
  ***                                                               ***
  ***   Description :                                               ***
  ***      This method converts the input long value to a string    ***
  ***   using the specified radix.                                  ***
  ***                                                               ***
  ***   Input :                                                     ***
  ***      formatI                  The format description object.  ***
  ***      longI                    The value to convert.           ***
  ***      radixI                   The radix.                      ***
  ***                                                               ***
  ***   Returns :                                                   ***
  ***      integerConversion        The string representing the     ***
  ***                               converted value.                ***
  ***                                                               ***
  *********************************************************************
*/
  private static String integerConversion(Format formatI,long longI,int radixI)
  throws Exception {
    long   working = Math.abs(longI);
    String sign = new String(),
           integer = Long.toString(working,radixI),
           valueR = new String();

    /* A precision of zero is an error. */
    if (formatI.precision == 0) {
      throw new Exception("Integer precision must not be zero.");
    }

    /* If the value is negative, set the sign to -. */
    if (longI < 0.) {
      sign += "-";

    /*
       If the format conversion specifies a positive sign handling, figure out
       what to do.
    */
    } else if (formatI.positive != None) {

      /*
	 A value of 0 always has a space. We also produce a space if the format
	 conversion is blank.
      */
      if ((formatI.positive == Blank) || (longI == 0)) {
	sign += ' ';

      /* Otherwise, produce a "+" sign. */
      } else {
	sign += "+";
      }
    }

    /*
       If the sign plus the value is too long for a fixed width format, fill
       with *'s.
    */
    if (formatI.fixedWidth &&
	(sign.length() + integer.length() > formatI.width)) {
      for (int idx = 0; idx < formatI.width; ++idx) {
	valueR += "*";
      }

    /*
       Otherwise, ensure that the integer portion is has at least precision
       digits (so long as adding extras does not exceed the fixed field width)
       and then combine the pieces.
    */
    } else {
      int toAdd = formatI.precision;

      if (formatI.fixedWidth &&
	  (formatI.width - sign.length() - integer.length() < toAdd)) {
	toAdd = formatI.width - sign.length() - integer.length();
      }

      for (int idx = 0; idx < toAdd; ++idx) {
	integer = "0" + integer;
      }

      valueR = sign + integer;
    }

    /* Pad the string as needed. */
    if (formatI.width != -1) {
      for (int idx = valueR.length(); idx < formatI.width; ++idx) {
	if (formatI.leftJustify) {
	  valueR += " ";
	} else {
	  valueR = " " + valueR;
	}
      }
    }

    return (valueR);
  }

/*
  *********************************************************************
  ***                                                               ***
  ***   Name :  toString            (Double to string)              ***
  ***   By   :  Ian Brown           (Creare Inc., Hanover, NH)      ***
  ***   For  :  FlyScan                                             ***
  ***   Date :  April, 1998                                         ***
  ***                                                               ***
  ***   Copyright 1998 Creare Inc.                                  ***
  ***   All Rights Reserved                                         ***
  ***                                                               ***
  ***   Description :                                               ***
  ***      This method converts a double to a string using the      ***
  ***   specified format.                                           ***
  ***                                                               ***
  ***   Input :                                                     ***
  ***      formatI                  The format string.  Only the    ***
  ***                               e, f, and g formats are used.   ***
  ***      doubleI                  The double value.               ***
  ***                                                               ***
  ***   Returns :                                                   ***
  ***      toString                 The string.                     ***
  ***                                                               ***
  *********************************************************************
*/
  public static String toString
    (String formatI,
     double doubleI)
  throws Exception {
    ToString tString = new ToString();
    Format   format = tString.convertFormat(formatI);
    boolean  isNaN = (new Double(doubleI)).isNaN();

    /* Special values: NaN, infinity. */
    if (isNaN ||
	(doubleI == Double.NEGATIVE_INFINITY) ||
	(doubleI == Double.POSITIVE_INFINITY)) {
      String valueR = new String("");
      int minLength;

      if (isNaN) {
	valueR = "NaN";
	minLength = 3;
      } else if (doubleI == Double.NEGATIVE_INFINITY) {
	valueR = "-Infinity";
	minLength = 4;
      } else {
	if (format.positive == Blank) {
	  valueR = " Infinity";
	  minLength = 4;
	} else if (format.positive == Plus) {
	  valueR = "+Infinity";
	  minLength = 4;
	} else {
	  valueR = "Infinity";
	  minLength = 3;
	}
      }

      if (format.fixedWidth) {
	if (minLength > format.width) {
	  valueR = "*";
	  for (int idx = 1; idx < format.width; ++idx) {
	    valueR += "*";
	  }
	} else if (valueR.length() > format.width) {
	  valueR = valueR.substring(0,format.width);
	} else if (format.leftJustify) {
	    for (int idx = 0;
		 idx < format.width - valueR.length();
		 ++idx) {
		valueR += " ";
	    }
	} else {
	    for (int idx = 0;
		 idx < format.width - valueR.length();
		 ++idx) {
		valueR = " " + valueR;
	    }
	}
      }

      return (valueR);
    }

    /* Handle this based on the conversion format. */
    switch (format.conversion) {

      /* Exponential format. */
      case format_e: return (exponentialConversion(format,doubleI));

      /* Fixed format. */
      case format_f: return (fixedConversion(format,doubleI));

      /* Automatic selction of double format. */
      case format_g: return (autoDoubleConversion(format,doubleI));

      /* Nothing else is supported. */
      default:
	throw new Exception
	  ("Format conversion is not supported for doubles " + formatI + ".");
    }
  }

/*
  *********************************************************************
  ***                                                               ***
  ***   Name :  fixedConversion     (Fixed format conversion)       ***
  ***   By   :  Ian Brown           (Creare Inc., Hanover, NH)      ***
  ***   For  :  FlyScan                                             ***
  ***   Date :  April, 1998 - February, 1999			    ***
  ***                                                               ***
  ***   Copyright 1998, 1999, 2002 Creare Inc.			    ***
  ***   All Rights Reserved                                         ***
  ***                                                               ***
  ***   Description :                                               ***
  ***      This method converts the input double precision value    ***
  ***   to a string using a fixed point conversion.                 ***
  ***                                                               ***
  ***   Input :                                                     ***
  ***      formatI                  The format description object.  ***
  ***      doubleI                  The value to convert.           ***
  ***                                                               ***
  ***   Returns :                                                   ***
  ***      fixedConversion          The string representing the     ***
  ***                               converted value.                ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/06/2002 - INB					    ***
  ***		Handle the case where rounding the digits after	    ***
  ***		the decimal results in a value that is larger or    ***
  ***		equal to 10^tPrecision.				    ***
  ***                                                               ***
  *********************************************************************
*/
  private static String fixedConversion(Format formatI,double doubleI)
  throws Exception {
    double working = Math.abs(doubleI);
    String valueR = new String(),
           sign = new String(),
           decimal = new String(),
           integer = new String();
    int	   precision =
      (formatI.precision != -1) ?
        formatI.precision :
        formatI.fixedWidth ?
          formatI.width :
          6;

    /* If the value is negative, set the sign to -. */
    if (doubleI < 0.) {
      sign += "-";

    /*
       If the format conversion specifies a positive sign handling, figure out
       what to do.
    */
    } else if (formatI.positive != None) {

      /*
	 A value of 0 always has a space. We also produce a space if the format
	 conversion is blank.
      */
      if ((formatI.positive == Blank) || (doubleI == 0.)) {
	sign += ' ';

      /* Otherwise, produce a "+" sign. */
      } else {
	sign += "+";
      }
    }

    /*
       If the precision is zero, then the integer part defines the entire
       thing.
    */
    if (precision == 0) {


      /* Convert the integer part of the value. */
      double iWorking = Math.round(working);
      while (iWorking >= Long.MAX_VALUE) {
	integer = ((int) (iWorking % 10.0)) + integer;
	iWorking /= 10.;
      }
      integer = ((long) iWorking) + integer;

      /*
	 If the field width is fixed and the value exceeds its length, produce
	 asterisks.
      */
      if (formatI.fixedWidth &&
	  (integer.length() + sign.length() > formatI.width)) {
	for (int idx = 0; idx < formatI.width; ++idx) {
	  valueR += "*";
	}

      /* Otherwise, the result is equal to the sign plus the value. */
      } else {
	valueR = sign + integer;
      }

    /* If the precision is greater than zero, convert digit by digit. */
    } else {

      /* Convert the integer part. */
      double iWorking = Math.floor(working);
      while (iWorking >= Long.MAX_VALUE) {
	integer = ((int) (iWorking % 10.0)) + integer;
	iWorking /= 10.;
      }
      integer = ((long) iWorking) + integer;

      /* In fixed width, we reduce the precision to try and make it fit. */
      if (formatI.fixedWidth &&
	  (sign.length() + integer.length() + 1 + precision > formatI.width)) {
	precision = formatI.width - sign.length() - integer.length() - 1;
	if (precision == 0) {
	  decimal = "";
	}
      }

      /* If there is any precision left, use it. */
      if (precision > 0) {
        long   value = (long) Math.round
	  ((working - Math.floor(working))*Math.pow(10.,precision));
        int    tPrecision = precision;
	String asString;

	if (value == 0) {
	  asString = new String();
	  if (formatI.precision == -1) {
	    tPrecision = 0;
	  }
	} else {
	  if (formatI.precision == -1) {
	    for (; (value % 10) == 0; value /= 10) { --tPrecision; }
	  }
	  asString = Long.toString(value);
	}

	if (asString.length() < tPrecision) {
	  for (int idx = asString.length(); idx < tPrecision; ++idx) {
	    asString = "0" + asString;
	  }

	} else if (asString.length() > tPrecision) {
	    integer = ("" +
		       (Long.parseLong(integer) +
		       Long.parseLong((asString.substring(0,1)))));
	    asString = asString.substring(1);
	}

	if (asString.equals("")) {
	  decimal = asString;
	} else {
	  decimal += "." + asString;
	}
      }

      /* If the result is not too large, combine the pieces. */
      if (!formatI.fixedWidth ||
	  (sign.length() +
	   integer.length() +
	   decimal.length() <= formatI.width)) {
	valueR = sign + integer + decimal;

      /* Otherwise, fill in asterisks. */
      } else {
	for (int idx = 0; idx < formatI.width - 1 - formatI.precision; ++idx) {
	  valueR += "*";
	}
        if (formatI.precision > 0) {
	  valueR += ".";
	  for (int idx = 0; idx < formatI.precision; ++idx) {
	    valueR += "*";
	  }
	}
      }
    }

    /* Pad the string as needed. */
    if (formatI.width != -1) {
      for (int idx = valueR.length(); idx < formatI.width; ++idx) {
	if (formatI.leftJustify) {
	  valueR += " ";
	} else {
	  valueR = " " + valueR;
	}
      }
    }

    return (valueR);
  }

/*
  *********************************************************************
  ***                                                               ***
  ***   Name :  exponentialConversion                               ***
  ***   By   :  Ian Brown           (Creare Inc., Hanover, NH)      ***
  ***   For  :  FlyScan                                             ***
  ***   Date :  April - July, 1998                                  ***
  ***                                                               ***
  ***   Copyright 1998, 2002 Creare Inc.			    ***
  ***   All Rights Reserved                                         ***
  ***                                                               ***
  ***   Description :                                               ***
  ***      This method converts the input double precision value    ***
  ***   to a string using a exponentail conversion.                 ***
  ***                                                               ***
  ***   Input :                                                     ***
  ***      formatI                  The format description object.  ***
  ***      doubleI                  The value to convert.           ***
  ***                                                               ***
  ***   Returns :                                                   ***
  ***      exponentailConversion    The string representing the     ***
  ***                               converted value.                ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/11/2002 - INB					    ***
  ***		Handle the case where rounding the digits after	    ***
  ***		the decimal results in a value that is larger or    ***
  ***		equal to 10^tPrecision.				    ***
  ***                                                               ***
  *********************************************************************
*/
  private static String exponentialConversion(Format formatI,double doubleI)
  throws Exception {
    double working = Math.abs(doubleI);
    String valueR = new String(),
           sign = new String(),
           exponent = new String("e"),
           decimal = new String(),
           integer = new String();
    int    iExponent = 0,
	   precision =
      (formatI.precision != -1) ?
        formatI.precision :
        formatI.fixedWidth ?
          formatI.width :
          6;

    /* If the value is negative, set the sign to -. */
    if (doubleI < 0.) {
      sign += "-";

    /*
       If the format conversion specifies a positive sign handling, figure out
       what to do.
    */
    } else if (formatI.positive != None) {

      /*
	 A value of 0 always has a space. We also produce a space if the format
	 conversion is blank.
      */
      if ((formatI.positive == Blank) || (doubleI == 0.)) {
	sign += ' ';

      /* Otherwise, produce a "+" sign. */
      } else {
	sign += "+";
      }
    }

    /* If the value is less than one, calculate a negative exponent. */
    while ((working != 0.) && (working < 1.)) {
      --iExponent;
      working *= 10.;
    }

    /*
       If the value is greater than or equal to 10, calculate a positive
       exponent.
    */
    while (working > 10.) {
      ++iExponent;
      working /= 10.;
    }

    /* Produce the actual exponent. */
    exponent += iExponent;

    /*
       If the precision is zero, then the integer part defines the entire
       thing.
    */
    if (precision == 0) {

      /* Convert the integer part of the value. */
      double iWorking = Math.round(working);

System.err.println("Round: " + iWorking);

      while (iWorking >= Long.MAX_VALUE) {
	integer = ((int) (iWorking % 10.0)) + integer;
	iWorking /= 10.;
      }
      integer = ((long) iWorking) + integer;

      /*
	 If the field width is fixed and the value exceeds its length, produce
	 asterisks.
      */
      if (formatI.fixedWidth &&
	  (integer.length() + sign.length() + exponent.length() >
	   formatI.width)) {
	for (int idx = 0; idx < formatI.width - 2; ++idx) {
	  valueR += "*";
	}
	valueR += "e*";

      /* Otherwise, the result is equal to the sign plus the value. */
      } else {
	valueR = sign + integer + exponent;
      }

    /* If the precision is greater than zero, convert digit by digit. */
    } else {
      /* Convert the integer part. */
      double iWorking = Math.floor(working);
      while (iWorking >= Long.MAX_VALUE) {
	integer = ((int) (iWorking % 10.0)) + integer;
	iWorking /= 10.;
      }
      integer = ((long) iWorking) + integer;

      /* In fixed width, we reduce the precision to try and make it fit. */
      if (formatI.fixedWidth &&
	  (sign.length() +
	   integer.length() +
	   exponent.length() +
	   1 +
	   precision > formatI.width)) {
	precision =
	  formatI.width -
	  sign.length() -
	  integer.length() -
	  exponent.length() -
	  1;
	if (precision == 0) {
	  decimal = "";
	}
      }

      /* If there is any precision left, use it. */
      if (precision > 0) {
        long   value = (long) Math.round
	  ((working - Math.floor(working))*Math.pow(10.,precision));

	int    tPrecision = precision;
	String asString;

	if (value == 0) {
	  asString = new String();
	  if (formatI.precision == -1) {
	    tPrecision = 0;
	  }
	} else {
	  if (formatI.precision == -1) {
	    for (; (value % 10) == 0; value /= 10) { --tPrecision; }
	  }
	  asString = Long.toString(value);
	}

        if (asString.length() < tPrecision) {
	  for (int idx = asString.length(); idx < tPrecision; ++idx) {
	    asString = "0" + asString;
	  }

	} else if (asString.length() > tPrecision) {
	  int digit = Integer.parseInt(asString.substring(0,1));
	  asString = asString.substring(1);

	  for (int idx = integer.length() - 1;
	       (digit != 0) && (idx >= 0);
	       --idx) {
	      if (idx == integer.length() - 1) {
		  digit += Integer.parseInt(integer.substring(idx));
		  integer = integer.substring(0,idx) + (digit % 10);
	      } else {
		  digit += Integer.parseInt(integer.substring(idx,idx + 1));
		  integer = (integer.substring(0,idx) +
			     (digit % 10) +
			     integer.substring(idx + 1));
	      }
	      digit /= 10;
	  }
	}

	if (asString.equals("")) {
	  decimal = asString;
	} else {
	  decimal += "." + asString;
	}
      }

      /* If the result is not too large, combine the pieces. */
      if (!formatI.fixedWidth ||
	  (sign.length() +
	   integer.length() +
	   decimal.length() +
	   exponent.length() <= formatI.width)) {
	valueR = sign + integer + decimal + exponent;

      /* Otherwise, fill in asterisks. */
      } else {
	for (int idx = 0;
	     idx < formatI.width - 1 - formatI.precision - 2;
	     ++idx) {
	  valueR += "*";
	}
        if (formatI.precision > 0) {
	  valueR += ".";
	  for (int idx = 0; idx < formatI.precision; ++idx) {
	    valueR += "*";
	  }
	}
	valueR += "e*";
      }
    }

    /* Pad the string as needed. */
    if (formatI.width != -1) {
      for (int idx = valueR.length(); idx < formatI.width; ++idx) {
	if (formatI.leftJustify) {
	  valueR += " ";
	} else {
	  valueR = " " + valueR;
	}
      }
    }

    return (valueR);
  }

/*
  *********************************************************************
  ***                                                               ***
  ***   Name :  autoDoubleConversion                                ***
  ***   By   :  Ian Brown           (Creare Inc., Hanover, NH)      ***
  ***   For  :  FlyScan                                             ***
  ***   Date :  April - July, 1998                                  ***
  ***                                                               ***
  ***   Copyright 1998 Creare Inc.                                  ***
  ***   All Rights Reserved                                         ***
  ***                                                               ***
  ***   Description :                                               ***
  ***      This method converts the input double precision value    ***
  ***   to a string using either fixed or exponential conversion as ***
  ***   appropriate.                                                ***
  ***                                                               ***
  ***   Input :                                                     ***
  ***      formatI                  The format description object.  ***
  ***      doubleI                  The value to convert.           ***
  ***                                                               ***
  ***   Returns :                                                   ***
  ***      autoDoubleConversion     The string representing the     ***
  ***                               converted value.                ***
  ***                                                               ***
  *********************************************************************
*/
  private static String autoDoubleConversion(Format formatI,double doubleI)
  throws Exception {
    double working = Math.abs(doubleI);
    String valueR = null;
    int	   precision =
      (formatI.precision != -1) ?
        formatI.precision :
        formatI.fixedWidth ?
          formatI.width :
          6;

    /* Try fixed format. */
    if ((precision == -1) ||
	(doubleI == 0.) ||
       ((Math.abs(doubleI) >= 1.e-3) &&
	(Math.abs(doubleI) <= Math.pow(10.,precision)))) {
      valueR = fixedConversion(formatI,doubleI);
    }

    /*
       If we are working on a fixed width conversion or there is no converted
       value or we ran out of space, try exponential format.
    */
    if (formatI.fixedWidth || (valueR == null) || (valueR.charAt(0) == '*')) {
      String value2 = exponentialConversion(formatI,doubleI);

      /* Given no fixed conversion value, use the exponential form. */
      if (valueR == null) {
	valueR = value2;

      /*
	 If the fixed conversion failed and exponential did not, use the
	 exponential form.
      */
      } else if ((valueR.indexOf("*") != -1) &&
		 (value2.indexOf("*") == -1)) {
	valueR = value2;

      /*
	 If the exponential form failed, use the fixed form. Otherwise,
	 determine which format provides a larger number of digits.
      */
      } else if (value2.indexOf("*") == -1) {
	int fixedDigits = 0,
	    expoDigits = 0;

	for (int idx = 0; idx < valueR.length(); ++idx) {
	  if (Character.isDigit(valueR.charAt(idx)) &&
	      (valueR.charAt(idx) != '0')) {
	    ++fixedDigits;
	  }
	}

	for (int idx = 0; idx < value2.length(); ++idx) {
	  if (Character.toUpperCase(value2.charAt(idx)) == 'E') {
	    break;
	  } else if (Character.isDigit(value2.charAt(idx)) &&
		     (value2.charAt(idx) != '0')) {
	    ++expoDigits;
	  }
	}

	if (expoDigits > fixedDigits) {
	  valueR = value2;
	}
      }
    }

    return (valueR);
  }

/*
  *********************************************************************
  ***                                                               ***
  ***   Name :  toString            (String to string)              ***
  ***   By   :  Ian Brown           (Creare Inc., Hanover, NH)      ***
  ***   For  :  FlyScan                                             ***
  ***   Date :  April, 1998                                         ***
  ***                                                               ***
  ***   Copyright 1998 Creare Inc.                                  ***
  ***   All Rights Reserved                                         ***
  ***                                                               ***
  ***   Description :                                               ***
  ***      This method converts a string to a string using the      ***
  ***   specified format.                                           ***
  ***                                                               ***
  ***   Input :                                                     ***
  ***      formatI                  The format string.  Only the    ***
  ***                               e, f, and g formats are used.   ***
  ***      stringI                  The double value.               ***
  ***                                                               ***
  ***   Returns :                                                   ***
  ***      toString                 The string.                     ***
  ***                                                               ***
  *********************************************************************
*/
  public static String toString
    (String formatI,
     String stringI)
  throws Exception {
    ToString tString = new ToString();
    Format   format = tString.convertFormat(formatI);

    /* Handle this based on the conversion format. */
    switch (format.conversion) {

      /* Exponential format. */
      case format_s: return (stringConversion(format,stringI));

      /* Nothing else is supported. */
      default:
	throw new Exception
	  ("Format conversion is not supported for strings " + formatI + ".");
    }
  }

/*
  *********************************************************************
  ***                                                               ***
  ***   Name :  stringConversion    (Convert string to a string)    ***
  ***   By   :  Ian Brown           (Creare Inc., Hanover, NH)      ***
  ***   For  :  FlyScan                                             ***
  ***   Date :  April, 1998                                         ***
  ***                                                               ***
  ***   Copyright 1998 Creare Inc.                                  ***
  ***   All Rights Reserved                                         ***
  ***                                                               ***
  ***   Description :                                               ***
  ***      This method converts the input string value to a string  ***
  ***   padded or trimmed as desired.                               ***
  ***                                                               ***
  ***   Input :                                                     ***
  ***      formatI                  The format description object.  ***
  ***      stringI                  The value to convert.           ***
  ***                                                               ***
  ***   Returns :                                                   ***
  ***      stringConversion         The string representing the     ***
  ***                               converted value.                ***
  ***                                                               ***
  *********************************************************************
*/
  private static String stringConversion(Format formatI,String stringI)
  throws Exception {

    /* Positive sign selection is illegal. */
    if (formatI.positive != None) {
      throw new Exception("The s format does not support + or space flags.");
    }

    /* If there is no field width and no precision, return the input. */
    if ((formatI.width == -1) && (formatI.precision == -1)) {
      return (stringI);

    /* Otherwise, things may get more complicated. */
    } else {

      /* A precision and a fixed field width must match. */
      if (formatI.fixedWidth) {
	if ((formatI.precision != -1) &&
	    (formatI.width != formatI.precision)) {
	  throw new Exception
	    ("Precision " +
	     formatI.precision +
	     " must match fixed field width " +
	     formatI.width +
	     ".");
	} else {
	  formatI.precision = formatI.width;
	}
      }

      /* If the string is the right size, return it. */
      if (((formatI.width != -1) && (stringI.length() == formatI.width)) ||
	  ((formatI.width == -1) && (stringI.length() <= formatI.precision))) {
	return (stringI);

      /*
	 If the string is too long, truncate it on the right or left as
	 specified by the flag.
      */
      } else if ((formatI.precision != -1) &&
		 (stringI.length() > formatI.precision)) {
	if (formatI.leftJustify) {
	  return (stringI.substring(stringI.length() - formatI.precision));
	} else {
	  return (stringI.substring(0,formatI.precision));
	}

      /* Otherwise, pad the string as necessary. */
      } else {
	String valueR = new String(stringI);

	for (int idx = valueR.length(); idx < formatI.width; ++idx) {
	  if (formatI.leftJustify) {
	    valueR += " ";
	  } else {
	    valueR = " " + valueR;
	  }
	}

	return (valueR);
      }
    }
  }

/*
  *********************************************************************
  ***                                                               ***
  ***   Name :  main                (Test program)                  ***
  ***   By   :  Ian Brown           (Creare Inc., Hanover, NH)      ***
  ***   For  :  FlyScan                                             ***
  ***   Date :  April, 1998                                         ***
  ***                                                               ***
  ***   Copyright 1998 Creare Inc.                                  ***
  ***   All Rights Reserved                                         ***
  ***                                                               ***
  ***   Description :                                               ***
  ***      This method allows a user to test the conversions.       ***
  ***                                                               ***
  ***   Syntax :                                                    ***
  ***      java ToString <format> <value>                           ***
  ***                                                               ***
  *********************************************************************
*/
  public final static void main(String argv[]) {
    if (argv.length < 2) {
      System.err.println("Syntax:");
      System.err.println("  java ToString <format> <value>");
      System.exit(-1);
    }

    try {
      String format = argv[0];
      double doubleV;
      long   longV;
      String stringV;

      /* Select the variable type to convert. */
      switch (format.charAt(format.length() - 1)) {
        case 'f':
        case 'e':
        case 'g':
	  try {
	    doubleV = Double.valueOf(argv[1]).doubleValue();
	  } catch (NumberFormatException e1) {
	    if (argv[1].equalsIgnoreCase("NaN")) {
	      doubleV = Double.NaN;
	    } else if ((argv[1].length() >= 3) &&
		       argv[1].substring(0,3).equalsIgnoreCase("Inf")) {
	      doubleV = Double.POSITIVE_INFINITY;
	    } else if ((argv[1].length() >= 4) &&
		       argv[1].substring(0,4).equalsIgnoreCase("+Inf")) {
	      doubleV = Double.POSITIVE_INFINITY;
	    } else if ((argv[1].length() >= 4) &&
		       argv[1].substring(0,4).equalsIgnoreCase("-Inf")) {
	      doubleV = Double.NEGATIVE_INFINITY;
	    } else {
	      doubleV = Double.NaN;
	    }
	  }

	  System.out.println("Show: " + doubleV + " using " + format);
	  System.out.println
	    ("Result: | " + ToString.toString(format,doubleV) + " |");
	break;

        case 'i':
        case 'o':
        case 'x':
	  longV = Long.parseLong(argv[1]);

	  System.out.println("Show: " + longV + " using " + format);
	  System.out.println
	    ("Result: | " + ToString.toString(format,longV) + " |");
	break;

        case 's':
	  System.out.println("Show: " + argv[1] + " using " + format);
	  System.out.println
	    ("Result: | " + ToString.toString(format,argv[1]) + " |");
	break;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

/*
  *********************************************************************
  ***                                                               ***
  ***   Name :  Format              (Format description class)      ***
  ***   By   :  Ian Brown           (Creare Inc., Hanover, NH)      ***
  ***   For  :  FlyScan                                             ***
  ***   Date :  April, 1998                                         ***
  ***                                                               ***
  ***   Copyright 1998 Creare Inc.                                  ***
  ***   All Rights Reserved                                         ***
  ***                                                               ***
  ***   Description :                                               ***
  ***      This private class describes the formats used by the     ***
  ***   ToString class in a simple way that can be quickly checked. ***
  ***                                                               ***
  *********************************************************************
*/
  private final class Format {

    /* Public fields: */
                                            // Fixed field width?
    public boolean            fixedWidth = false,
                                            // Left justify field?
                              leftJustify = false;
                                            // Conversion format to use.
    public int                conversion = Unknown,
                                            // Sign display for positive value?
                              positive = None,
                              width = -1,   // Field width.
                                            // Field precision.
                              precision = -1;

/*
  *********************************************************************
  ***                                                               ***
  ***   Name :  Format              (Constructor: from string)      ***
  ***   By   :  Ian Brown           (Creare Inc., Hanover, NH)      ***
  ***   For  :  FlyScan                                             ***
  ***   Date :  April, 1998                                         ***
  ***                                                               ***
  ***   Copyright 1998 Creare Inc.                                  ***
  ***   All Rights Reserved                                         ***
  ***                                                               ***
  ***   Description :                                               ***
  ***      This constructor builds the format object from the input ***
  ***   format string.                                              ***
  ***                                                               ***
  ***   Input :                                                     ***
  ***      formatI                  The format string.              ***
  ***                                                               ***
  *********************************************************************
*/
    public Format(String formatI) throws Exception {
      int idx = 0;

      /* Skip the leading character if it is a %. */
      if (formatI.charAt(0) == '%') {
	++idx;
      }

      /* Check for the leading flags. */
      for (; !Character.isDigit(formatI.charAt(idx)); ++idx) {

	/* Check for the fixed format flag. */
	if (formatI.charAt(idx) == fixed) {
	  if (fixedWidth) {
	    throw new Exception
	      ("Found fixed flag (" +
	       fixed +
	       ") more than once in format " +
	       formatI +
	       ".");
	  }
	  fixedWidth = true;

	/* Check for the left justify flag. */
	} else if (formatI.charAt(idx) == left) {
	  if (leftJustify) {
	    throw new Exception
	      ("Found left justify flag (" +
	       left +
	       ") more than once in format " +
	       formatI +
	       ".");
	  }
	  leftJustify = true;

	/* Check for positive sign values. */
	} else {
	  int idx1;

	  for (idx1 = Blank; idx1 <= Plus; ++idx1) {
	    if (formatI.charAt(idx) == signs[idx1]) {
	      break;
	    }
	  }

	  /* If it isn't one, break out. */
	  if (idx1 > Plus) {
	    break;

	  /* Otherwise, update the positive flag setting. */
	  } else {
	    if (positive != Plus) {
	      positive = idx1;
	    } else if (idx1 == Plus) {
	      throw new Exception
		("Found plus sign flag (" +
		 signs[Plus] +
		 ") more than once in format " +
		 formatI +
		 ".");
	    }
	  }
	}
      }

      /* If we are looking at a digit, then what we have is the field width. */
      if (Character.isDigit(formatI.charAt(idx))) {
	int idx1;

	/* Find the other end of the field width. */
	for (idx1 = idx + 1; idx1 < formatI.length(); ++idx1) {
	  if (!Character.isDigit(formatI.charAt(idx1))) {
	    break;
	  }
	}

        width = Integer.parseInt(formatI.substring(idx,idx1));
	idx = idx1;
      }

      /* If we are now looking at a ., then that is the precision. */
      if (formatI.charAt(idx) == '.') {

	/*
	   If the next character is a digit, then the precision is
	   specified.
	*/
	++idx;
	if (Character.isDigit(formatI.charAt(idx))) {
	  int idx1;

	  /* Find the other end of the field precision. */
	  for (idx1 = idx + 1; idx1 < formatI.length(); ++idx1) {
	    if (!Character.isDigit(formatI.charAt(idx1))) {
	      break;
	    }
	  }

	  precision = Integer.parseInt(formatI.substring(idx,idx1));
	  idx = idx1;

          /* Ensure that the precision and width make sense. */
	  if ((width != -1) && (precision > width)) {
	    throw new Exception
	      ("Precision exceeds width in format " +
	       formatI +
	       ".");
	  }
	}
      }

      /* The field width must be specified for fixed width. */
      if (fixedWidth && (width == -1)) {
	throw new Exception
	  ("The field width must be specified in fixed width format.");
      }

      /* If we have more than one character after that, it is an error. */
      if (idx + 1 != formatI.length()) {
	throw new Exception("Format string " + formatI + " cannot be parsed.");
      }

      /* The final character should be one of the conversion values. */
      for (conversion = format_i; conversion <= format_s; ++conversion) {
	if (formatI.charAt(idx) == conversions[conversion]) {
	  break;
	}
      }

      /* Make sure it is a legal conversion. */
      if (conversion > format_s) {
	throw new Exception("Illegal conversion in format " + formatI + ".");
      }
    }
  }
}
