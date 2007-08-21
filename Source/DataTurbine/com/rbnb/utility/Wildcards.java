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
  ***								    ***
  ***	Name :	Wildcards	    (Wildcard matching class)	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998 - January, 1999			    ***
  ***								    ***
  ***	Copyright 1998, 1999, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This class performs wildcard matching for channel names. ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   05/20/1999 - INB					    ***
  ***		Added any character in set expression type.	    ***
  ***		Added '\' as a quoting character.		    ***
  ***	   08/18/1999 - INB					    ***
  ***		Added "!" at the beginning of the wildcard. It	    ***
  ***		indicates that the meaning of the entire wildcard   ***
  ***		expression is inverted.				    ***
  ***	   02/04/2000 - INB					    ***
  ***		Added "|" (or) and "&" (and) operators.		    ***
  ***	   02/08/2000 - INB					    ***
  ***		Added ability to request partial match.		    ***
  ***	   05/03/2000 - INB					    ***
  ***		Added wildcardCharacters field and retrieval	    ***
  ***		method.						    ***
  ***	   07/26/2000 - 07/28/2000 - INB			    ***
  ***		Made this class serializable.			    ***
  ***		Ensure that we break out when we run out of input.  ***
  ***								    ***
  *********************************************************************
*/
package com.rbnb.utility;

import java.io.Serializable;

public class Wildcards implements Serializable {

  /* Private variables: */
					// The expression to match.
  private Expression	      expression = null;
					// Invert the meaning?
  private boolean	      invert = false;

  /* Class variables: */
					// Print debug?
  static boolean	      debug = false;

  /* Private constants: */

  // This is the list of reserved wildcard characters. Update this list
  // any time you add new reserved characters.
  private static final String wildcardCharacters = "*?[{!|&";

/*
  *********************************************************************
  ***								    ***
  ***	Name :	Wildcards	    (Constructor: from string)	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 1999, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This constructor builds a wildcards object from the	    ***
  ***	input string.						    ***
  ***								    ***
  ***	Input :							    ***
  ***	   wildcardI		    The wildcard string.	    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   08/18/1999 - INB					    ***
  ***		Added "!" at the beginning of the wildcard. It	    ***
  ***		indicates that the meaning of the entire wildcard   ***
  ***		expression is inverted.				    ***
  ***	   02/04/2000 - INB					    ***
  ***		Handle binary expression exceptions.		    ***
  ***								    ***
  *********************************************************************
*/
  public Wildcards(String wildcardI)
  throws Exception {
    int idx = 0;

    if (debug) {
      System.err.println("Build wildcard for: " + wildcardI);
    }

    /* See if this expression has been inverted. */
    if (wildcardI.charAt(0) == '!') {
      invert = true;
      idx = 1;

      if (debug) {
	System.err.println("Inverted expression.");
      }
    }

    /* Build the expression. */
    try {
      if ((expression = Expression.makeExpression(wildcardI,idx)) == null) {
	throw new Exception("Unable to build expression from " + wildcardI);
      }
    } catch (Exception e) {
      do {
	if (!(e instanceof BinaryExpressionException)) {
	  throw e;
	}

	/*
	   On binary expression exceptions, we move forward by building the
	   second half of the expression. Note that we may end up having a
	   second exception thrown.
	*/
	BinaryExpressionException be = (BinaryExpressionException) e;

	if (debug) {
	  be.printStackTrace();
	}

	expression = be.getBinaryExpression();
	try {
	  if ((expression.nextExpression =
	       Expression.makeExpression(wildcardI,be.getIndex())) == null) {
	    throw new Exception
	      ("Unable to build expression from " + wildcardI);
	  }
	  break;
	} catch (Exception e1) {
	  if (!(e1 instanceof BinaryExpressionException)) {
	    throw e1;
	  }
	  e = e1;

	  BinaryExpressionException be1 = (BinaryExpressionException) e1;

	  if (debug) {
	    be1.printStackTrace();
	  }

	  expression.nextExpression = be1.getBinaryExpression().firstValue;
	  be1.getBinaryExpression().firstValue = expression;
	}
      } while (true);
    }
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	getWildcardCharacters				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This static method returns the list of reserved	***
  ***	wildcard characters.					***
  ***								***
  ***	Returns :						***
  ***	   getWildcardCharacters				***
  ***				The list of wildcards.		***
  ***								***
  *****************************************************************
*/
  public final static String getWildcardCharacters() {
    return (wildcardCharacters);
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	isConstant	    (Is this a constant?)	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 1999 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method determines if there is only a single,	    ***
  ***	constant expression.					    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   isConstant		    True if this is a constant.	    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   08/18/1999 - INB					    ***
  ***		An inverted expression is not a constant.	    ***
  ***								    ***
  *********************************************************************
*/
  public final boolean isConstant() {
    return (!invert &&
	    (expression != null) &&
	    (expression.nextExpression == null) &&
	    (expression instanceof Constant) &&
	    (expression.maximumRepeats == 1));
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	matches		    (Wildcard comparison)	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 1999, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method compares the wildcard expression to the	    ***
  ***	input string to determine if they match.		    ***
  ***								    ***
  ***	Input :							    ***
  ***	   toMatchI		    The string to match.	    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   matches		    String matches wildcard?	    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   08/18/1999 - INB					    ***
  ***		Handle inverted meaning.			    ***
  ***	   02/08/2000 - INB					    ***
  ***		Call version that allows partial matches.	    ***
  ***								    ***
  *********************************************************************
*/
  public final boolean matches(String toMatchI) {
    return (matches(toMatchI,false));
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	matches		    (Wildcard comparison)	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 1999, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method compares the wildcard expression to the	    ***
  ***	input string to determine if they match.		    ***
  ***	   If partial matches are allowed, then the entire input    ***
  ***	string must be matched by the wildcard expression(s),	    ***
  ***	but it not all of the expressions need to be executed.	    ***
  ***								    ***
  ***	Input :							    ***
  ***	   toMatchI		    The string to match.	    ***
  ***	   partialI		    Allow partial matches?	    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   matches		    String matches wildcard?	    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   08/18/1999 - INB					    ***
  ***		Handle inverted meaning.			    ***
  ***	   02/08/2000 - INB					    ***
  ***		Allow for partial matches. Cannot have inverted	    ***
  ***		partials.					    ***
  ***								    ***
  *********************************************************************
*/
  public final boolean matches(String toMatchI,boolean partialI) {
    boolean matches = invert;

    if (invert && partialI) {
      return (false);
    }

    if (debug) {
      System.err.println
	("Match against: " + toMatchI + " partial? " + partialI);
    }

    if (expression != null) {
      matches = expression.matches(toMatchI,0,partialI);
      if (invert) {
	matches = !matches;
      }
    }

    return (matches);
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	toString					    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	August, 1999					    ***
  ***								    ***
  ***	Copyright 1999, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method returns a string representation of this	    ***
  ***	wildcard.						    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   toString		    The string representation.	    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/04/2000 - INB					    ***
  ***		Expressions now show their parts.		    ***
  ***								    ***
  *********************************************************************
*/
  public final String toString() {
    String value = "";

    if (invert) {
      value = "not ";
    }
    value += expression;

    return (value);
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	main		    (Test main)			    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998 - February, 2000			    ***
  ***								    ***
  ***	Copyright 1998, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method creates a wildcard from the first string and ***
  ***	compares it to the subsequent strings.			    ***
  ***								    ***
  *********************************************************************
*/
  public final static void main(String args[]) {
    try {
      Wildcards wildcard = new Wildcards(args[0]);

      System.out.println(args[0] + " = " + wildcard);

      int idx = 1;
      boolean allowPartial = false;

      if (args[idx].equals("--partialOK")) {
	allowPartial = true;
	++idx;
      }

      if (args[idx].equals("--debug")) {
	debug = true;
	++idx;
      }

      for (; idx < args.length; ++idx) {
	boolean matched = wildcard.matches(args[idx],allowPartial);

	if (matched) {
	  System.out.println(args[idx] +
			     ((allowPartial) ?
			      " partially matches " :
			      " matches ")
			     + args[0]);
	} else {
	  System.out.println(args[idx] + " does not match " + args[0]);
	}
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

/*
  *********************************************************************
  ***								    ***
  ***	Name :	Expression	    (Wildcard matching expression)  ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This abstract class provides the structure for handling  ***
  ***	a single wildcard expression. For the sake of consistency,  ***
  ***	a set of characters without any wildcards is an expression. ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/04/2000 - INB					    ***
  ***		Added "|" (or) and "&" (and) operators.		    ***
  ***	   02/08/2000 - INB					    ***
  ***		Added partial matching capability.		    ***
  ***	   07/26/2000 - INB					    ***
  ***		Made this class serializable.			    ***
  ***								    ***
  *********************************************************************
*/
abstract class Expression implements Serializable {

  /* Class accessible variables: */
					  // Are repeats allowed?
  protected boolean	      repeatsAllowed = false;

					  // Minimum number of repeats.
  protected int		      minimumRepeats = 1,
					  // Maximum number of repeats.
			      maximumRepeats = 1,
			      repeats = 0;// Repeats matched so far.

					  // The next expression.
  protected Expression	      nextExpression = null;

/*
  *********************************************************************
  ***								    ***
  ***	Name :	matches		    (Match expression)		    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method matches an expression against part of the    ***
  ***	input string.						    ***
  ***								    ***
  ***	Input :							    ***
  ***	   toMatchI		    The string to match.	    ***
  ***	   toMatchIndexI	    The index into the string at    ***
  ***				    which to start matching.	    ***
  ***	    partialI		    Allow partial matches?	    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   matches		    True if the expression was	    ***
  ***				    matched.			    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/04/2000 - INB					    ***
  ***		Binary expressions can be fully matched on the	    ***
  ***		minimum comparison with no need to match the	    ***
  ***		remainder. If this is the case, the compare	    ***
  ***		Minimum method returns -2.			    ***
  ***	   02/08/2000 - INB					    ***
  ***		Allow partial matching.				    ***
  ***								    ***
  *********************************************************************
*/
  boolean matches(String toMatchI,int toMatchIndexI,boolean partialI) {
    int toMatchIndex;

    if (Wildcards.debug) {
      System.err.println
	(this + " matching against: " + toMatchI.substring(toMatchIndexI));
    }

    /*
       We first match a the minimum necessary to accept this expression,
       and then we try to match the remainder of the string.
    */
    int oldMatchIndex;

    /* If we cannot match a minimum part of this expression, we fail. */
    if ((toMatchIndex = compareMinimum
	 (toMatchI,
	  toMatchIndexI,
	  partialI)) == -1) {
      return (false);

    /* If the minimum was all that is needed, we're done. */
    } else if (toMatchIndex == -2) {
      return (true);
    }

    /*
       Work our way forwards until we can get a match or completely
       fail.
    */
    return (matchesRemainder(toMatchI,toMatchIndex,partialI));
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	matchesRemainder    (Match the remainder of string) ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method is called after we've matched a minimum part ***
  ***	of the input string to this expression. If tries to match   ***
  ***	more until it has matched everything.			    ***
  ***								    ***
  ***	Input :							    ***
  ***	   toMatchI		    The string to match.	    ***
  ***	   toMatchIndexI	    The index into the string at    ***
  ***				    which to start matching.	    ***
  ***	   partialI		    Allow partial matches?	    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   matchesRemainder	    True if the expression was	    ***
  ***				    matched.			    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/08/2000 - INB					    ***
  ***		Allow partial matching.				    ***
  ***	   07/27/2000 - INB					    ***
  ***		Ensure that we break out when we run out of input.  ***
  ***								    ***
  *********************************************************************
*/
  boolean matchesRemainder
    (String toMatchI,
     int toMatchIndexI,
     boolean partialI) {
    int toMatchIndex = toMatchIndexI,
	 oldMatchIndex = toMatchIndexI;

    if (Wildcards.debug) {
      System.err.println
	(this + " matching remainder of: " +
	 toMatchI.substring(toMatchIndexI));
    }

    /*
       Work forwards matching this expression until we either can no
       longer work forwards or we get a complete match.
    */
    do {
      int toMatchIndex2;

      /*
	 If we can match a minimum part of the next expression, try
	 to match the rest of the string using the next expression.
      */
      if ((toMatchIndex < toMatchI.length()) &&
	  (nextExpression != null) &&
	  ((toMatchIndex2 = nextExpression.compareMinimum
	    (toMatchI,
	     toMatchIndex,
	     partialI)) != -1)) {

	/* If we can match the rest of the string, we are done. */
	if (nextExpression.matchesRemainder(toMatchI,toMatchIndex2,partialI)) {
	  return (true);
	}

      /*
	 If we've reached the end of the input, we are done one way or
	 another.
      */
      } else if (toMatchIndex == toMatchI.length()) {
	return ((nextExpression == null) || partialI);
      }

      /*
	 If we get this far, we were unable to match the next
	 expression, so try to match some more of this string.
      */
      oldMatchIndex = toMatchIndex;
    } while (((toMatchIndex = compareMore
	       (toMatchI,
		toMatchIndex,
		partialI)) != -1) &&
	     (toMatchIndex != oldMatchIndex));

    /* If we fall out the loop, we've failed. */
    return (false);
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	makeExpression	    (Make an expression)	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 1999, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method attempts to make an expression of some sort  ***
  ***	from the list of possible expressions.			    ***
  ***								    ***
  ***	Input :							    ***
  ***	   wildcardI		    The wildcard string to turn	    ***
  ***				    into expressions.		    ***
  ***	   indexI		    Index into the wildcard string. ***
  ***	   partialI		    Allow partial matches?	    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   makeExpression	    The expression built.	    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   05/20/1999 - INB					    ***
  ***		Added AnyCharacterInSet expression type.	    ***
  ***	   02/04/2000 - INB					    ***
  ***		Added ORExpression and ANDExpression types.	    ***
  ***		Handle binary expression exceptions.		    ***
  ***								    ***
  *********************************************************************
*/
  static final Expression makeExpression(String wildcardI,int indexI)
  throws Exception {
    Expression expressionR = null;
    int	       index = -1;

    /*
       Try to build each type of expression in turn until we get one that
       works or we completely fail.

       NOTE: modify this loop if you want to add new expression types.
    */
    for (int idx = 0; (expressionR == null) && (idx < 4); ++idx) {
      switch (idx) {

	/* Try to build an any character expression. */
	case 0:
	  expressionR = (Expression) new AnyCharacter();
	break;

	/* Try to build an any character in set expression. */
	case 1:
	  expressionR = (Expression) new AnyCharacterInSet();
	break;

	/* Try to build an OR expression. */
	case 2:
	  expressionR = (Expression) new ORExpression();
	break;

	/* Try to build an AND expression. */
	case 3:
	  expressionR = (Expression) new ANDExpression();
	break;
      }
      if ((index = expressionR.buildExpression
	   (wildcardI,
	    indexI)) == -1) {
	expressionR = null;
      }
    }

    /*
       Try to build a constant expression. This is the default if all of
       the other expression types fail.
    */
    if (expressionR == null) {
      expressionR = (Expression) new Constant();
      if ((index = expressionR.buildExpression(wildcardI,indexI)) == -1) {
	expressionR = null;
      }
    }

    /* If we haven't built an expression yet, that's an error. */
    if (expressionR == null) {
      throw new Exception
	("Failed to build wildcard expression out of " +
	 wildcardI +
	 " starting at index " +
	 indexI);
    }

    /*
       If we haven't hit the end of the input string, create another
       expression.
    */
    if (index != wildcardI.length()) {
      if ((index = expressionR.buildRepeats
	   (wildcardI,
	    index)) != wildcardI.length()) {
	try {
	  expressionR.nextExpression = makeExpression(wildcardI,index);

	/*
	   On binary expression exceptions, we need to build up the first
	   value expression by inserting the expression we would have
	   returned here into the list at its beginning.
	*/
	} catch (Exception e) {
	  if (e instanceof BinaryExpressionException) {
	    BinaryExpressionException be = (BinaryExpressionException) e;

	    expressionR.nextExpression = be.getBinaryExpression().firstValue;
	    be.getBinaryExpression().firstValue = expressionR;
	  }
	  throw e;
	}
      }
    }

    return (expressionR);
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	buildRepeats	    (Build repeats)		    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998 - January, 1999			    ***
  ***								    ***
  ***	Copyright 1998, 1999 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method attempts to find a repeat count expression   ***
  ***	at the input character in the input string. If it finds	    ***
  ***	such a count, it stores the range and returns the index of  ***
  ***	the character following the repeat count.		    ***
  ***	   The legal repeat counts are:				    ***
  ***								    ***
  ***		{n}	repeat exactly n times			    ***
  ***		{n,}	repeat at least n times			    ***
  ***		{,m}	repeat at most m times			    ***
  ***		{n,m}	repeat n to m times			    ***
  ***								    ***
  ***	Input :							    ***
  ***	   wildcardI		    The wildcard string to turn	    ***
  ***				    into expressions.		    ***
  ***	   indexI		    Index into the wildcard string. ***
  ***								    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   buildRepeats		    The index of the character that ***
  ***				    follows the repeat count or the ***
  ***				    input index if not repeat count ***
  ***				    can be found.		    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   05/20/1999 - INB					    ***
  ***		Ensure that the opening brace is not quoted.	    ***
  ***								    ***
  *********************************************************************
*/
  private final int buildRepeats(String wildcardI,int indexI)
  throws Exception {
    boolean quoted = false;

    if (indexI > 0) {
      int idx;

      for (idx = 1; idx < indexI; ++idx) {
	if (wildcardI.charAt(indexI - idx) != '\\') {
	  break;
	}
      }
      quoted = ((idx % 2) == 0);
    }

    /*
       If the character is a curly brace, then it must mark the start of
       a repeat string.
    */
    if (!quoted && (wildcardI.charAt(indexI) == '{')) {

      /*
	 If repeat counts are not legal for this type of expression, report
	 an error.
      */
      if (!repeatsAllowed) {
	throw new Exception
	  ("Repeat count not allowed for " +
	   this +
	   ", found at " +
	   indexI +
	   " in " +
	   wildcardI);
      }

      /* Locate the end of the repeat count. */
      int endOfCount = wildcardI.indexOf("}",indexI);

      /* If no end can be found, that is an error. */
      if (endOfCount == -1) {
	throw new Exception
	  ("Badly formed repeat count expression in " +
	   wildcardI +
	   " starting at " +
	   indexI);
      }

      /* Get the repeat count expression. */
      String repeatCount = wildcardI.substring(indexI + 1,endOfCount);

      /* See if there is a comma in the repeat count. */
      int comma = repeatCount.indexOf(",");

      /* If there is no comma, then we've got an exact count. */
      if (comma == -1) {
	int count = Integer.parseInt(repeatCount);

	if (count < 0) {
	  throw new Exception
	    ("Illegal negative expression repeat count found in " +
	     wildcardI +
	     " at " +
	     indexI);
	}

	minimumRepeats =
	  maximumRepeats = count;

      /* If the comma is the first character, we have a maximum count. */
      } else if (comma == 0) {
	maximumRepeats = Integer.parseInt(repeatCount.substring(1));

	if (maximumRepeats < 0) {
	  throw new Exception
	    ("Illegal negative expression repeat count found in " +
	     wildcardI +
	     " at " +
	     indexI);
	}

      /* If the comma is the last character, we have a minimum count. */
      } else if (comma == repeatCount.length() - 1) {
	minimumRepeats = Integer.parseInt
	  (repeatCount.substring(0,repeatCount.length() - 1));
	maximumRepeats = Integer.MAX_VALUE;

	if (minimumRepeats < 0) {
	  throw new Exception
	    ("Illegal negative expression repeat count found in " +
	     wildcardI +
	     " at " +
	     indexI);
	}

      /* Otherwise, we have a range. */
      } else {
	minimumRepeats = Integer.parseInt(repeatCount.substring(0,comma));
	maximumRepeats = Integer.parseInt(repeatCount.substring(comma + 1));

	if (minimumRepeats < 0) {
	  throw new Exception
	    ("Illegal negative expression repeat count found in " +
	     wildcardI +
	     " at " +
	     indexI);
	} else if (minimumRepeats > maximumRepeats) {
	  throw new Exception
	    ("Expression minimum repeat count must be greater than maximum in " +
	     wildcardI +
	     " at " +
	     indexI);
	}
      }

      return (endOfCount + 1);
    }

    /* If there was no repeat count, return the input index. */
    return (indexI);
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	buildExpression	    (Build an expression)	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998 Creare Inc.				    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This abstract method tries to build a specific type of   ***
  ***	expression from the input string starting at the specified  ***
  ***	character.						    ***
  ***								    ***
  ***	Input :							    ***
  ***	   wildcardI		    The wildcard string to turn	    ***
  ***				    into expressions.		    ***
  ***	   indexI		    Index into the wildcard string. ***
  ***								    ***
  ***	Returns :						    ***
  ***	   buildExpression	    The index of the character that ***
  ***				    follows the expression or -1 if ***
  ***				    no expression could be built.   ***
  ***								    ***
  *********************************************************************
*/
  abstract int buildExpression(String wildcardI,int indexI) throws Exception;

/*
  *********************************************************************
  ***								    ***
  ***	Name :	compareMinimum	    (Compare minimum match)	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This abstract method finds the minimum part of the	    ***
  ***	input string starting at the input index that will satisfy  ***
  ***	this expression.					    ***
  ***								    ***
  ***	Input :							    ***
  ***	   toMatchI		    The string to be matched.	    ***
  ***	   toMatchIndexI	    The index to match.		    ***
  ***	   partialI		    Allow partial matches?	    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   compareMinimum	    The index of the character that ***
  ***				    follows the minimum match, -2   ***
  ***				    the minimum is a complete match,***
  ***				     or -1 if no match can be made. ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/08/2000 - INB					    ***
  ***		Allow partial matching.				    ***
  ***								    ***
  *********************************************************************
*/
  abstract int compareMinimum
    (String toMatchI,
     int toMatchIndexI,
     boolean partialI);

/*
  *********************************************************************
  ***								    ***
  ***	Name :	compareMore	    (Compare after partial match)   ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This abstract method finds the next portion of the input ***
  ***	string that matches this expression after we've already	    ***
  ***	matched part of the input string to this expression. It	    ***
  ***	deals with repeats and strings that can be matched in	    ***
  ***	different ways.						    ***
  ***								    ***
  ***	Input :							    ***
  ***	   toMatchI		    The string to be matched.	    ***
  ***	   toMatchIndexI	    The index to match.		    ***
  ***	   partialI		    Allow partial matches?	    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   compareMore		    The index of the character that ***
  ***				    follows the next match or -1    ***
  ***				    if no match can be made.	    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/08/2000 - INB					    ***
  ***		Allow partial matching.				    ***
  ***								    ***
  *********************************************************************
*/
  abstract int compareMore(String toMatchI,int toMatchIndexI,boolean partialI);

/*
  *****************************************************************
  ***								***
  ***	Name :	toString					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method builds up a representation of this	***
  ***	expression as a string. It should be called at the	***
  ***	end of all of the subclass toString methods.		***
  ***								***
  ***	Returns :						***
  ***	   toString		The string representation.	***
  ***								***
  *****************************************************************
*/
  public String toString() {
    if (nextExpression == null) {
      return ("");
    }
    return (" " + nextExpression);
  }
}

/*
  *********************************************************************
  ***								    ***
  ***	Name :	Constant	    (Constant "expression")	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998 Creare Inc.				    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This extension of the Expression class matches a	    ***
  ***	constant string of characters.				    ***
  ***								    ***
  *********************************************************************
*/
class Constant extends Expression {

  /* Private variables: */
					  // The constant to match.
  private String	      constant = null;

/*
  *********************************************************************
  ***								    ***
  ***	Name :	Constant	    (Constructor: default)	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This constructor builds an empty constant expression.    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/08/2000 - INB					    ***
  ***		Allow partial matching.				    ***
  ***								    ***
  *********************************************************************
*/
  Constant() {
    repeatsAllowed = true;
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	buildExpression	    (Build a constant expression)   ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This routine builds a constant expression. A constant    ***
  ***	expression consists of any characters except:		    ***
  ***								    ***
  ***		|, &, ?, *, [, ], {, and }			    ***
  ***								    ***
  ***	Input :							    ***
  ***	   wildcardI		    The wildcard string to turn	    ***
  ***				    into expressions.		    ***
  ***	   indexI		    Index into the wildcard string. ***
  ***								    ***
  ***	Returns :						    ***
  ***	   buildExpression	    The index of the character that ***
  ***				    follows the expression or -1 if ***
  ***				    no expression could be built.   ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/04/2000 - INB					    ***
  ***		Added "|" and "&" to the list of proscribed	    ***
  ***		characters.					    ***
  ***								    ***
  *********************************************************************
*/
  int buildExpression(String wildcardI,int indexI) {
    int    index = indexI,
	   indexR = indexI;
    String specialCharacters = "|&?*[]{}";

    /* Build up the string. */
    constant = new String("");

    /*
       Loop until we hit a special character. If we find a '\', copy
       everything up to the current position, skip the '\', copy the
       next character, and move on.
    */
    while (specialCharacters.indexOf(wildcardI.charAt(indexR)) == -1) {

      /* Handle backslash ('\') as a quote character. */
      if (wildcardI.charAt(indexR) == '\\') {
	constant += wildcardI.substring(index,indexR);
        index = ++indexR;
	if (indexR == wildcardI.length()) {
	  break;
	}
	constant += wildcardI.charAt(index);
	index = indexR + 1;
      }

      /* Move onto the next character. */
      if (++indexR == wildcardI.length()) {
	break;
      }
    }

    /* If there are characters left to copy, do so. */
    if (indexR != index) {
      constant += wildcardI.substring(index,indexR);
    }

    /* If we got nothing, report it. */
    if (constant.equals("")) {
      indexR = -1;
    }

    return (indexR);
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	compareMinimum	    (Find minimum match)	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method finds the minimum portion of the input	    ***
  ***	string starting at the input index that matches this	    ***
  ***	constant. The minimum match is an exact match repeated the  ***
  ***	minimum number of times.				    ***
  ***								    ***
  ***	Input :							    ***
  ***	   toMatchI		    The string to be matched.	    ***
  ***	   toMatchIndexI	    The index to match.		    ***
  ***	   partialI		    Allow partial match?	    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   compareMinimum	    The index of the character that ***
  ***				    follows the minimum match or -1 ***
  ***				    if no match can be made.	    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/08/2000 - INB					    ***
  ***		Allow partial matching.				    ***
  ***								    ***
  *********************************************************************
*/
  int compareMinimum(String toMatchI,int toMatchIndexI,boolean partialI) {
    int toMatchIndexR = toMatchIndexI;

    /* Match the string the minimum number of repeats. */
    for (repeats = 0; repeats < minimumRepeats; ++repeats) {

      /* If we cannot get a match, we've failed. */
      if (!toMatchI.startsWith(constant,toMatchIndexR)) {

	/* 
	   With partial matches, it is OK to match the entire remainder of
	   the input string to the constant.
	*/
	if (partialI) {
	  if (constant.startsWith(toMatchI.substring(toMatchIndexR))) {
	    return (toMatchI.length());
	  }
	}
	return (-1);
      }

      toMatchIndexR += constant.length();
    }

    return (toMatchIndexR);
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	compareMore	    (Compare after partial match)   ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998 Creare Inc.				    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method finds the next portion of the input string   ***
  ***	string that matches this expression after we've already	    ***
  ***	matched part of the input string to this expression. It	    ***
  ***	deals with repeats and strings that can be matched in	    ***
  ***	different ways.						    ***
  ***	   With a constant, we just have to match it one more time  ***
  ***	if we haven't reached the repeat limit.			    ***
  ***								    ***
  ***	Input :							    ***
  ***	   toMatchI		    The string to be matched.	    ***
  ***	   toMatchIndexI	    The index to match.		    ***
  ***	   partialI		    Allow partial match?	    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   compareMore		    The index of the character that ***
  ***				    follows the next match or -1    ***
  ***				    if no match can be made.	    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/08/2000 - INB					    ***
  ***		Allow partial matching.				    ***
  ***								    ***
  *********************************************************************
*/
  int compareMore(String toMatchI,int toMatchIndexI,boolean partialI) {
      
    /* If we have reached the repeat limit, we've failed. */
    if (repeats == maximumRepeats) {
      return (-1);

    /* If we cannot match the constant, we've failed. */
    } else if (!toMatchI.startsWith(constant,toMatchIndexI)) {

      /* 
	 With partial matches, it is OK to match the entire remainder of
	 the input string to the constant.
      */
      if (partialI) {
	if (constant.startsWith(toMatchI.substring(toMatchIndexI))) {
	  return (toMatchI.length());
	}
      }
      return (-1);
    }

    /* Increment the repeat count and move past the constant. */
    ++repeats;
    return (toMatchIndexI + constant.length());
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	toString	    (Constant as string)	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method returns a string representation of this	    ***
  ***	constant.						    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   toString		   The string representation.	    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/04/2000 - INB					    ***
  ***		Call the Expression.toString method, too.	    ***
  ***								    ***
  *********************************************************************
*/
  public String toString() {
    return ("Constant: " +
	    constant +
	    " {" +
	    minimumRepeats +
	    "," +
	    maximumRepeats +
	    "}" +
	    super.toString());
  }
}

/*
  *********************************************************************
  ***								    ***
  ***	Name :	AnyCharacterInSet   (Match any character in a set)  ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	May, 1999					    ***
  ***								    ***
  ***	Copyright 1999, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This class matches any single character in a set or not  ***
  ***	in a set.. By using the repeat count, it can match any	    ***
  ***	number of characters.					    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/08/2000 - INB					    ***
  ***		Allow partial matching.				    ***
  ***								    ***
  *********************************************************************
*/
class AnyCharacterInSet extends Expression {

					  // Match characters not in set?
  private boolean	      notInSet = false,
					  // Match a range of characters?
			      range = false;
  private String	      set = null; // Characters in the set.
					  // Start of a range of characters.
  private char		      startOfRange = ' ',
					  // End of a range of characters.
			      endOfRange = ' ';

/*
  *********************************************************************
  ***								    ***
  ***	Name :	AnyCharacterInSet   (Constructor: default)	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	May, 1999					    ***
  ***								    ***
  ***	Copyright 1999 Creare Inc.				    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This constructor builds an empty any character in set    ***
  ***	expression. It won't match anything.			    ***
  ***								    ***
  *********************************************************************
*/
  AnyCharacterInSet() {
    repeatsAllowed = true;
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	buildExpression	    (Build an any char in set expr) ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	May, 1999					    ***
  ***								    ***
  ***	Copyright 1999 Creare Inc.				    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method builds an any character in set expression.   ***
  ***	The any character in set expression takes the forms:	    ***
  ***								    ***
  ***		[x...]						    ***
  ***		[!x...]						    ***
  ***		[a-z]						    ***
  ***		[!a-z]						    ***
  ***								    ***
  ***	The first two forms match any character (not) in the list   ***
  ***	of characters between the brackets. The second two forms    ***
  ***	match any character (not) in the range specified.	    ***
  ***								    ***
  ***	Input :							    ***
  ***	   wildcardI		    The wildcard string to turn	    ***
  ***				    into expressions.		    ***
  ***	   indexI		    Index into the wildcard string. ***
  ***								    ***
  ***	Returns :						    ***
  ***	   buildExpression	    The index of the character that ***
  ***				    follows the expression or -1 if ***
  ***				    no expression could be built.   ***
  ***								    ***
  *********************************************************************
*/
  int buildExpression(String wildcardI,int indexI) {

    /* The first character must be a '['. */
    if (wildcardI.charAt(indexI) == '[') {
      int index = indexI + 1,
	  indexR = wildcardI.indexOf("]",index);

      /*
	 From the closing bracket, work backwards and ensure that it hasn't
	 been quoted. If it has, look for another closing bracket and try
	 again.
      */
      do {
	int idx;

	for (idx = 1; (indexR - idx) > index; ++idx) {
	  if (wildcardI.charAt(indexR - idx) != '\\') {
	    break;
	  }
	}
	if ((idx % 2) == 1) {
	  break;
	}
      } while ((indexR = wildcardI.indexOf("]",indexR + 1)) != -1);

      if (indexR == -1) {
	return (-1);
      }

      /*
	 If the second character is an '!', we are matching characters not
	 in the set.
      */
      if (wildcardI.charAt(index) == '!') {
	notInSet = true;
	++index;
      }

      /*
	 Copy the set of characters into a local variable after pulling
	 out any quote characters.
      */
      String local = "";
      int    quote;

      if (((quote = wildcardI.indexOf("\\",index)) == -1) ||
	   (quote > indexR)) {
	local = wildcardI.substring(index,indexR);
      } else {
	while ((quote != -1) && (quote < indexR)) {
	  local += wildcardI.substring(index,quote);
	  index = quote + 1;
	  quote = wildcardI.indexOf("\\",index + 1);
	}
	if (index < indexR) {
	  local += wildcardI.substring(index,indexR);
	}
      }

      /*
	 See if we have a range of characters. A range consists of two
	 characters separated by a '-'.
      */
      if ((local.length() == 3) && (local.charAt(1) == '-')) {
	range = true;
	startOfRange = local.charAt(0);
	endOfRange = local.charAt(2);

      /* Otherwise, we have just a set of characters. */
      } else {
	set = local;
      }

      return (indexR + 1);
    }

    /* Otherwise, we did not match. */
    return (-1);
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	acceptable					    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	May, 1999					    ***
  ***								    ***
  ***	Copyright 1999 Creare Inc.				    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   Determine if the input character is (not) in the set or  ***
  ***	range of characters.					    ***
  ***								    ***
  ***	Input :							    ***
  ***	   charI		    The character in question.	    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   acceptable		    Is character acceptable?	    ***
  ***								    ***
  *********************************************************************
*/
  private boolean acceptable(char charI) {
    boolean inSet = false;

    /* If we are looking for a range, check against the range. */
    if (range) {
      inSet = (charI >= startOfRange) && (charI <= endOfRange);

    /* Otherwise, look for a match in the set. */
    } else {
      inSet = (set.indexOf(charI) != -1);
    }

    return (notInSet ? !inSet : inSet);
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	compareMinimum	    (Find minimum match)	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	May, 1999					    ***
  ***								    ***
  ***	Copyright 1999 Creare Inc.				    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method finds the minimum portion of the input	    ***
  ***	string starting at the input index that matches this	    ***
  ***	expression. The minimum match is a number of characters	    ***
  ***	equal to the minimum repeat count that are all (not) in	    ***
  ***	the set or range.					    ***
  ***								    ***
  ***	Input :							    ***
  ***	   toMatchI		    The string to be matched.	    ***
  ***	   toMatchIndexI	    The index to match.		    ***
  ***	   partialI		    Allow partial match?	    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   compareMinimum	    The index of the character that ***
  ***				    follows the minimum match or -1 ***
  ***				    if no match can be made.	    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/08/2000 - INB					    ***
  ***		Allow partial matching.				    ***
  ***								    ***
  *********************************************************************
*/
  int compareMinimum(String toMatchI,int toMatchIndexI,boolean partialI) {

    /* If the string isn't long enough, that is an error. */
    if (!partialI && (toMatchI.length() < (toMatchIndexI + minimumRepeats))) {
      return (-1);
    }

    /*
       Otherwise, check each of those characters to see if they are
       acceptable.
    */
    for (repeats = 0; repeats < minimumRepeats; ++repeats) {
      if (toMatchIndexI + repeats == toMatchI.length()) {
	break;
      }
      if (!acceptable(toMatchI.charAt(toMatchIndexI + repeats))) {
	break;
      }
    }
    if (!partialI && (repeats < minimumRepeats)) {
      repeats = 0;
      return (-1);
    }

    return (toMatchIndexI + repeats);
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	compareMore	    (Compare after partial match)   ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method finds the next portion of the input string   ***
  ***	string that matches this expression after we've already	    ***
  ***	matched part of the input string to this expression. It	    ***
  ***	deals with repeats and strings that can be matched in	    ***
  ***	different ways.						    ***
  ***	    Check the next character to see if it is acceptable up  ***
  ***	to the maximum repeat count.				    ***
  ***								    ***
  ***	Input :							    ***
  ***	   toMatchI		    The string to be matched.	    ***
  ***	   toMatchIndexI	    The index to match.		    ***
  ***	   partialI		    Allow partial match?	    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   compareMore		    The index of the character that ***
  ***				    follows the next match or -1    ***
  ***				    if no match can be made.	    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/08/2000 - INB					    ***
  ***		Allow partial matching.				    ***
  ***								    ***
  *********************************************************************
*/
  int compareMore(String toMatchI,int toMatchIndexI,boolean partialI) {
      
    /* If we have reached the repeat limit, we've failed. */
    if (repeats == maximumRepeats) {
      return (-1);
    }

    /* If the next character is not acceptable, we've failed. */
    if (!acceptable(toMatchI.charAt(toMatchIndexI))) {
      return (-1);
    }

    /* Otherwise, increase the repeat count. */
    ++repeats;

    return (toMatchIndexI + 1);
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	toString	    (AnyCharacter as string)	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	May, 1999					    ***
  ***								    ***
  ***	Copyright 1999, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method returns a string representation of this	    ***
  ***	any character.						    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   toString		   The string representation.	    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/04/2000 - INB					    ***
  ***		Call the Expression.toString method, too.	    ***
  ***								    ***
  *********************************************************************
*/
  public String toString() {
    if (range) {
      return ("Range: " +
	      (notInSet ? "not in" : "") +
	      " [" + startOfRange + "-" + endOfRange + "] {" +
	      minimumRepeats + "," + maximumRepeats + "}" + super.toString());
    } else {
      return ("Set: " +
	      (notInSet ? "not in" : "") +
	      " [" + set + "] {" +
	      minimumRepeats + "," + maximumRepeats + "}" + super.toString());
    }
  }
}

/*
  *********************************************************************
  ***								    ***
  ***	Name :	AnyCharacter	    (Match any character)	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This class matches any single character. By using the    ***
  ***	repeat count, it can match any number of characters.	    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/08/2000 - INB					    ***
  ***		Allow partial matching.				    ***
  ***								    ***
  *********************************************************************
*/
class AnyCharacter extends Expression {

/*
  *********************************************************************
  ***								    ***
  ***	Name :	AnyCharacter	    (Constructor: default)	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998 Creare Inc.				    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This constructor builds an empty any character	    ***
  ***	expression.						    ***
  ***								    ***
  *********************************************************************
*/
  AnyCharacter() {
    repeatsAllowed = true;
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	buildExpression	    (Build an any character expr)   ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 1999 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method builds an any character expression. It	    ***
  ***	accepts either of two symbols: ? (match one character) or   ***
  ***	* (match any number of characters - sets the repeat count). ***
  ***								    ***
  ***	Input :							    ***
  ***	   wildcardI		    The wildcard string to turn	    ***
  ***				    into expressions.		    ***
  ***	   indexI		    Index into the wildcard string. ***
  ***								    ***
  ***	Returns :						    ***
  ***	   buildExpression	    The index of the character that ***
  ***				    follows the expression or -1 if ***
  ***				    no expression could be built.   ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   05/20/1999 - INB					    ***
  ***		Skip '?' or '*' preceeded by '\' quoting character. ***
  ***								    ***
  *********************************************************************
*/
  int buildExpression(String wildcardI,int indexI) {

    /* If the character is a ?, we match any single character. */
    if (wildcardI.charAt(indexI) == '?') {
      return (indexI + 1);

    /* If the character is an *, we match any number of characters. */
    } else if (wildcardI.charAt(indexI) == '*') {
      minimumRepeats = 0;
      maximumRepeats = Integer.MAX_VALUE;

      /* The user cannot specify a different repeat count. */
      repeatsAllowed = false;

      return (indexI + 1);
    }

    /* Otherwise, we did not match. */
    return (-1);
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	compareMinimum	    (Find minimum match)	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method finds the minimum portion of the input	    ***
  ***	string starting at the input index that matches this	    ***
  ***	expression. The minimum match is any character repeated the ***
  ***	minimum number of times.				    ***
  ***								    ***
  ***	Input :							    ***
  ***	   toMatchI		    The string to be matched.	    ***
  ***	   toMatchIndexI	    The index to match.		    ***
  ***	   partialI		    Allow partial matches?	    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   compareMinimum	    The index of the character that ***
  ***				    follows the minimum match or -1 ***
  ***				    if no match can be made.	    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/08/2000 - INB					    ***
  ***		Allow partial matching.				    ***
  ***								    ***
  *********************************************************************
*/
  int compareMinimum(String toMatchI,int toMatchIndexI,boolean partialI) {

    /* If the string isn't long enough, that is an error. */
    if (!partialI && (toMatchI.length() < (toMatchIndexI + minimumRepeats))) {
      return (-1);
    }

    /*
       Otherwise, we skip the next minimum repeats characters or the remainder
       of the string.
    */
    repeats = Math.min(minimumRepeats,toMatchI.length() - toMatchIndexI);
    return (toMatchIndexI + repeats);
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	compareMore	    (Compare after partial match)   ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998 Creare Inc.				    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method finds the next portion of the input string   ***
  ***	string that matches this expression after we've already	    ***
  ***	matched part of the input string to this expression. It	    ***
  ***	deals with repeats and strings that can be matched in	    ***
  ***	different ways.						    ***
  ***	   We always skip one more character up to the maximum	    ***
  ***	repeat count.						    ***
  ***								    ***
  ***	Input :							    ***
  ***	   toMatchI		    The string to be matched.	    ***
  ***	   toMatchIndexI	    The index to match.		    ***
  ***	   partialI		    Allow partial matches?	    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   compareMore		    The index of the character that ***
  ***				    follows the next match or -1    ***
  ***				    if no match can be made.	    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/08/2000 - INB					    ***
  ***		Allow partial matching.				    ***
  ***								    ***
  *********************************************************************
*/
  int compareMore(String toMatchI,int toMatchIndexI,boolean partialI) {
      
    /* If we have reached the repeat limit, we've failed. */
    if (repeats == maximumRepeats) {
      return (-1);
    }

    /* Increment the repeat count and move one character forwards. */
    ++repeats;
    return (toMatchIndexI + 1);
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	toString	    (AnyCharacter as string)	    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  FlyScan						    ***
  ***	Date :	December, 1998					    ***
  ***								    ***
  ***	Copyright 1998, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method returns a string representation of this	    ***
  ***	any character.						    ***
  ***								    ***
  ***	Returns :						    ***
  ***	   toString		   The string representation.	    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   02/04/2000 - INB					    ***
  ***		Call the Expression.toString method, too.	    ***
  ***								    ***
  *********************************************************************
*/
  public String toString() {
    return
      ("AnyCharacter: {" + minimumRepeats + "," + maximumRepeats + "}" +
       super.toString());
  }
}

/*
  *****************************************************************
  ***								***
  ***	Name :	BinaryExpression				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This abstract class is the parent class of all	***
  ***	binary valued expressions.				***
  ***								***
  *****************************************************************
*/
abstract class BinaryExpression extends Expression {

  /* Class accessible variables: */
					  // The first value to match.
  protected Expression		firstValue = null;
					  // Was the first part actually
					  // matched?
  protected boolean		matchedFirst = false;

/*
  *****************************************************************
  ***								***
  ***	Name :	matchesRemainder				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called after we've matched a minimum	***
  ***	part of the input string to this expression. If tries	***
  ***	to match more until it has matched everything.		***
  ***	   It overrides the standard Expression.matches		***
  ***	Remainder method in order to determine how to handle	***
  ***	the match/no match of the second value expression of	***
  ***	a binary expression.					***
  ***								***
  ***	Input :							***
  ***	   toMatchI		The string to match.		***
  ***	   toMatchIndexI	The index into the string at    ***
  ***				which to start matching.	***
  ***	   partialI		Allow partial matches?		***
  ***								***
  ***	Returns :						***
  ***	   matchesRemainder	True if the expression was	***
  ***				matched.			***
  ***								***
  *****************************************************************
*/
  boolean matchesRemainder
    (String toMatchI,
     int toMatchIndexI,
     boolean partialI) {
    boolean second = super.matchesRemainder(toMatchI,toMatchIndexI,partialI);

    return (matchedOnSecond(second,partialI));
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	compareMinimum					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method provides the basic minimum match method	***
  ***	for all binary-valued expressions. What it does is try	***
  ***	to match the first value expression.			***
  ***								***
  ***	Input :							***
  ***	   toMatchI		The string to be matched.	***
  ***	   toMatchIndexI	The index to match.		***
  ***	   partialI		Allow partial matches?		***
  ***								***
  ***	Returns :						***
  ***	   compareMinimum	The input index if the second	***
  ***				value (nextExpression) needs to	***
  ***				be checked, -1 on a failure,	***
  ***				-2 if we're done.		***
  ***								***
  *****************************************************************
*/
  int compareMinimum(String toMatchI,int toMatchIndexI,boolean partialI) {
    matchedFirst = firstValue.matches(toMatchI,toMatchIndexI,partialI);

    return (matchedOnFirst(toMatchIndexI,partialI));
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	compareMore					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method always returns the index of the end of	***
  ***	the string.						***
  ***								***
  ***	Input :							***
  ***	   toMatchI		The string to be matched.	***
  ***	   toMatchIndexI	The index to match.		***
  ***	   partialI		Allow partial matches?		***
  ***								***
  ***	Returns :						***
  ***	   compareMore		The index of the end of the	***
  ***				string.				***
  ***								***
  *****************************************************************
*/
  int compareMore(String toMatchI,int toMatchIndexI,boolean partialI) {
    return (toMatchI.length());
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	matchedOnFirst					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This abstract method determines whether or not the	***
  ***	results of attempting to match the first part of a	***
  ***	binary expression is sufficient to call for a definite	***
  ***	match or not.						***
  ***								***
  ***	Input :							***
  ***	   toMatchIndexI	The index into the string to	***
  ***				start the next match.		***
  ***	   partialI		Allow partial matches?		***
  ***								***
  ***	Returns :						***
  ***	   matchedOnFirst	The input index if the second	***
  ***				value (nextExpression) needs to	***
  ***				be checked, -1 on a failure,	***
  ***				-2 if we're done.		***
  ***								***
  *****************************************************************
*/
  abstract int matchedOnFirst(int toMatchIndexI,boolean partialI);

/*
  *****************************************************************
  ***								***
  ***	Name :	matchedOnSecond					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This abstract method determines whether or not the	***
  ***	results of attempting to match both parts of a binary	***
  ***	expression should be declared a match or no match.	***
  ***								***
  ***	Input :							***
  ***	   secondResultI	The result of the second match	***
  ***				attempt.			***
  ***	   partialI		Allow partial matches?		***
  ***								***
  ***	Returns :						***
  ***	   matchedOnSecond	True if the expression matches.	***
  ***								***
  *****************************************************************
*/
  abstract boolean matchedOnSecond(boolean secondResultI,boolean partialI);
}

/*
  *****************************************************************
  ***								***
  ***	Name :	ORExpression					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class provides an OR operator. The OR operator	***
  ***	is a binaryu-valued expression that matches if either	***
  ***	the first value or the remainder of the expression	***
  ***	matches the string.					***
  ***								***
  *****************************************************************
*/
class ORExpression extends BinaryExpression {

/*
  *****************************************************************
  ***								***
  ***	Name :	ORExpression					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor builds an empty OR expression.	***
  ***								***
  *****************************************************************
*/
  ORExpression() {
    repeatsAllowed = false;
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	buildExpression					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method builds an OR expression. An OR		***
  ***	expression is one where the next character is a "|".	***
  ***	If the method finds such a character, it throws a	***
  ***	BinaryExpressionException.				***
  ***								***
  ***	Input :							***
  ***	   wildcardI		The wildcard string to turn	***
  ***				into expressions.		***
  ***	   indexI		Index into the wildcard string. ***
  ***								***
  ***	Returns :						***
  ***	   buildExpression	The index of the character that ***
  ***				follows the expression or -1 if ***
  ***				no expression could be built.   ***
  ***								***
  *****************************************************************
*/
  int buildExpression(String wildcardI,int indexI) throws Exception {
    int index,
	indexR = -1;

    /* Skip any whitespace. */
    for (index = indexI; index < wildcardI.length(); ++index) {
      if (!Character.isWhitespace(wildcardI.charAt(index))) {
	break;
      }
    }

    /* See if the next character is a "|". */
    if ((index < wildcardI.length()) &&
	(wildcardI.charAt(index) == '|')) {

      /* Skip any further whitespace. */
      for (++index; index < wildcardI.length(); ++index) {
	if (!Character.isWhitespace(wildcardI.charAt(index))) {
	  break;
	}
      }

      throw new BinaryExpressionException(this,index);
    }

    return (indexR);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	matchedOnFirst					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   If the first value expression was matched, we're	***
  ***	done, otherwise, we need to move on to the next part.	***
  ***								***
  ***	Input :							***
  ***	   toMatchIndexI	The index into the string to	***
  ***				start the next match.		***
  ***	   partialI		Allow partial matches?		***
  ***								***
  ***	Returns :						***
  ***	   matchedOnFirst	The input index if the second	***
  ***				value (nextExpression) needs to	***
  ***				be checked, -1 on a failure,	***
  ***				-2 if we're done.		***
  ***								***
  *****************************************************************
*/
  int matchedOnFirst(int toMatchIndexI,boolean partialI) {
    return (matchedFirst ? -2 : toMatchIndexI);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	matchedOnSecond					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Return the results of the second match attempt.	***
  ***								***
  ***	Input :							***
  ***	   secondI		The second match attempt result.***
  ***	   partialI		Allow partial matches?		***
  ***								***
  ***	Returns :						***
  ***	   matchedOnSecond	The input value.		***
  ***								***
  *****************************************************************
*/
  boolean matchedOnSecond(boolean secondI,boolean partialI) {
    return (secondI);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	toString					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns a string representation of this	***
  ***	expression.						***
  ***								***
  ***	Returns :						***
  ***	   toString		The string representation.	***
  ***								***
  *****************************************************************
*/
  public String toString() {
    return (firstValue + " OR" + super.toString());
  }
}

/*
  *****************************************************************
  ***								***
  ***	Name :	ANDExpression					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class provides an AND operator. An AND operator	***
  ***	is a binaryu-valued expression that matches if either	***
  ***	the first value or the remainder of the expression	***
  ***	matches the string.					***
  ***								***
  *****************************************************************
*/
class ANDExpression extends BinaryExpression {

/*
  *****************************************************************
  ***								***
  ***	Name :	ANDExpression					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor builds an empty AND expression.	***
  ***								***
  *****************************************************************
*/
  ANDExpression() {
    repeatsAllowed = false;
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	buildExpression					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method builds an AND expression. An AND		***
  ***	expression is one where the next character is a "&".	***
  ***	If the method finds such a character, it throws a	***
  ***	BinaryExpressionException.				***
  ***								***
  ***	Input :							***
  ***	   wildcardI		The wildcard string to turn	***
  ***				into expressions.		***
  ***	   indexI		Index into the wildcard string. ***
  ***								***
  ***	Returns :						***
  ***	   buildExpression	The index of the character that ***
  ***				follows the expression or -1 if ***
  ***				no expression could be built.   ***
  ***								***
  *****************************************************************
*/
  int buildExpression(String wildcardI,int indexI) throws Exception {
    int index,
	indexR = -1;

    /* Skip any whitespace. */
    for (index = indexI; index < wildcardI.length(); ++index) {
      if (!Character.isWhitespace(wildcardI.charAt(index))) {
	break;
      }
    }

    /* See if the next character is an "&". */
    if ((index < wildcardI.length()) &&
	(wildcardI.charAt(index) == '&')) {

      /* Skip any further whitespace. */
      for (++index; index < wildcardI.length(); ++index) {
	if (!Character.isWhitespace(wildcardI.charAt(index))) {
	  break;
	}
      }

      throw new BinaryExpressionException(this,index);
    }

    return (indexR);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	matchedOnFirst					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   If the first value expression was not matched, we're	***
  ***	done and have no match, otherwise, we need to move on	***
  ***	to the next part.					***
  ***								***
  ***	Input :							***
  ***	   toMatchIndexI	The index into the string to	***
  ***				start the next match.		***
  ***	   partialI		Allow partial matches?		***
  ***								***
  ***	Returns :						***
  ***	   matchedOnFirst	The input index if the second	***
  ***				value (nextExpression) needs to	***
  ***				be checked, -1 on a failure,	***
  ***				-2 if we're done.		***
  ***								***
  *****************************************************************
*/
  int matchedOnFirst(int toMatchIndexI,boolean partialI) {
    return (matchedFirst ? toMatchIndexI : -1);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	matchedOnSecond					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Return the results of the second match attempt.	***
  ***								***
  ***	Input :							***
  ***	   secondI		The second match attempt result.***
  ***	   partialI		Allow partial matches?		***
  ***								***
  ***	Returns :						***
  ***	   matchedOnSecond	The input value.		***
  ***								***
  *****************************************************************
*/
  boolean matchedOnSecond(boolean secondI,boolean partialI) {
    return (secondI);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	toString					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns a string representation of this	***
  ***	expression.						***
  ***								***
  ***	Returns :						***
  ***	   toString		The string representation.	***
  ***								***
  *****************************************************************
*/
  public String toString() {
    return (firstValue + " AND" + super.toString());
  }
}

/*
  *****************************************************************
  ***								***
  ***	Name :	BinaryExpressionException			***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This exception is thrown whenever a binary valued	***
  ***	expression is located. Binary valued expressions are	***
  ***	things like "this or that" and "this and that". When	***
  ***	the "or" or "and" is hit, this exception is thrown,	***
  ***	causing the makeExpression method of Expression to	***
  ***	back up the chain to the top and then start down	***
  ***	again.							***
  ***								***
  *****************************************************************
*/
class BinaryExpressionException extends Exception {

  /* Private variables: */
					// The binary-expression object.
  private BinaryExpression	binaryExpression = null;
					// The current index.
  private int			index;

/*
  *****************************************************************
  ***								***
  ***	Name :	BinaryExpressionException			***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor builds a binary-expression		***
  ***	exception from the input binary-expression and index.	***
  ***								***
  ***	Input :							***
  ***	   binaryExpressionI	The binary-expression.		***
  ***	   indexI		The index into the string.	***
  ***								***
  *****************************************************************
*/
  BinaryExpressionException(BinaryExpression binaryExpressionI,int indexI) {
    super();
    binaryExpression = binaryExpressionI;
    index = indexI;
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	getBinaryExpression				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the binary-expression object.	***
  ***								***
  ***	Returns :						***
  ***	   getBinaryExpression	The binary-expression object.	***
  ***								***
  *****************************************************************
*/
  final BinaryExpression getBinaryExpression() {
    return (binaryExpression);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	getIndex					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the index into the wildcard	***
  ***	string marking the beginning of the next expression to	***
  ***	be parsed.						***
  ***								***
  ***	Returns :						***
  ***	   getIndex		The index.			***
  ***								***
  *****************************************************************
*/
  final int getIndex() {
    return (index);
  }
}
