/*******************************************************************************
 * Copyright C 2016, QUATTRO RESEARCH GMBH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.quattroresearch.helm;

import java.io.*;
import static org.junit.Assert.*;
import org.junit.Test;

import com.quattroresearch.helm.HELMHighlighter;

public class DefaultFontsizeConfig {
	// leadInFull: starting HTML sequence, leadOut: closing HTML sequence in the output HTML file
	String leadInPart1	= "<html>" + System.lineSeparator() +"<head>" + System.lineSeparator() +"<meta charset=\"UTF-8\">" + System.lineSeparator() +"<font style=\"font-size: ";
	String leadInPart2	= "8"; // fontsize=8pt
	String leadInPart3	= "pt\">" + System.lineSeparator() +"</head>" + System.lineSeparator() +"<body>" + System.lineSeparator() +"";
	String leadInFull	= leadInPart1 + leadInPart2 + leadInPart3;	
	String leadOut		= "</body>\n</html>";
	
	@Test
	public void test() throws Exception {
		String configFileName = "src/test/resources/DefaultFontsizeConfig.txt";
		PrintWriter printWriter = new PrintWriter(configFileName); // create a new simple config file for this test
		printWriter.println("DEFAULT_FONTSIZE FONTSIZE " + leadInPart2); // consisting of a single line		
		printWriter.close();
		
		StringWriter stringWriter = new StringWriter();
		printWriter = new PrintWriter(stringWriter);
		String helmString = new String("PEPTIDE1{C.Y.I.Q.N.C.P.L.G.[am]}$PEPTIDE1,PEPTIDE1,1:R3-6:R3$$$V2.0");
		String expectedHelmString = helmString; // expect that the Highlighter will not change the helmstring
		printWriter.print(leadInFull);
		try {
			HELMHighlighter.setupHlConfigEntries(configFileName);
			HELMHighlighter.processHelmString(helmString, printWriter);
		} catch (Exception e) {
			System.out.println(e);
		}
		printWriter.print(leadOut);
		printWriter.close();
		// System.out.println("stringWriter=\n" + stringWriter.toString()); System.out.println("assertEquals=\n" + leadInFull + helmString + "\n" + leadOut);		
		// Assert that the output HTML file contains default fontsize=8 in its lead-in:
		assertEquals(leadInFull + expectedHelmString  + System.lineSeparator() + leadOut, stringWriter.toString());
	}

}

