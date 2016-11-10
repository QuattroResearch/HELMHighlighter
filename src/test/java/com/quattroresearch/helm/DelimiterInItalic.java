package com.quattroresearch.helm;

import java.io.*;
import static org.junit.Assert.*;
import org.junit.Test;

import com.quattroresearch.helm.HelmHighlighter;

public class DelimiterInItalic {
	// leadInFull: starting HTML sequence, leadOut: closing HTML sequence in the output HTML file
	String leadInPart1	= "<html>" + System.lineSeparator() +"<head>" + System.lineSeparator() +"<meta charset=\"UTF-8\">" + System.lineSeparator() +"<font style=\"font-size: ";
	String leadInPart2	= HelmHighlighter.DEFAULT_FONTSIZE;
	String leadInPart3	= "pt\">" + System.lineSeparator() +"</head>" + System.lineSeparator() +"<body>" + System.lineSeparator() +"";
	String leadInFull	= leadInPart1 + leadInPart2 + leadInPart3;	
	String leadOut		= "</body>" + System.lineSeparator() +"</html>";

	@Test
	public void test() throws Exception {
		String configFileName = "src/test/resources/HHConfigSectionDelimiter.txt";
		PrintWriter printWriter = new PrintWriter(configFileName); // create a new simple config file for this test
		printWriter.println("DEFAULT_SECTION_SEPARATOR FONTSTYLE I"); // consisting of a single line		
		printWriter.close();
		
		String sectionSeparator = "<i>$</i>";		
		StringWriter stringWriter = new StringWriter();
		printWriter = new PrintWriter(stringWriter);
		String helmString = new String("PEPTIDE1{C.Y.I.Q.N.C.P.L.G.[am]}$PEPTIDE1,PEPTIDE1,1:R3-6:R3$$$V2.0");
		String expectedHelmString = "PEPTIDE1{C.Y.I.Q.N.C.P.L.G.[am]}" + sectionSeparator + "PEPTIDE1,PEPTIDE1,1:R3-6:R3" +	
									sectionSeparator + sectionSeparator + sectionSeparator + "V2.0";
		printWriter.print(leadInFull);
		try {
			HelmHighlighter.setupHlConfigEntries(configFileName);
			HelmHighlighter.processHelmString(helmString, printWriter);
		} catch (Exception e) {
			System.out.println(e);
		}
		printWriter.print(leadOut);
		printWriter.close();
		//System.out.println("stringWriter=\n" + stringWriter.toString()); System.out.println("assertEquals=\n" + leadInFull + expectedHelmString + "\n" + leadOut);		
		// assert that the HELM2 string has '$' delimiters in boldface in the output HTML file:
		assertEquals(leadInFull + expectedHelmString + System.lineSeparator()   + leadOut, stringWriter.toString());
		
	}

}


