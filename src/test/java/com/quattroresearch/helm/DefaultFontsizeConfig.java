package com.quattroresearch.helm;

import java.io.*;
import static org.junit.Assert.*;
import org.junit.Test;

import com.quattroresearch.helm.HelmHighlighter;

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
			HelmHighlighter.setupHlConfigEntries(configFileName);
			HelmHighlighter.processHelmString(helmString, printWriter);
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

