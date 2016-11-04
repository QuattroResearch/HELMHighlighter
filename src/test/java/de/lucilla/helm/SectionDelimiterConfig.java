package de.lucilla.helm;

import java.io.*;
import static org.junit.Assert.*;
import org.junit.Test;

public class SectionDelimiterConfig {
	// leadInFull: starting HTML sequence, leadOut: closing HTML sequence in the output HTML file
	String leadInPart1	= "<html>\n<head>\n<meta charset=\"UTF-8\">\n<font style=\"font-size: ";
	String leadInPart2	= HelmHighlighter.DEFAULT_FONTSIZE;
	String leadInPart3	= "pt\">\n</head>\n<body>\n";
	String leadInFull	= leadInPart1 + leadInPart2 + leadInPart3;	
	String leadOut		= "</body>\n</html>";

	@Test
	public void test() throws Exception {
		String configFileName = "/tmp/HHConfigSectionDelimiter.txt";
		PrintWriter printWriter = new PrintWriter(configFileName); // create a new simple config file for this test
		printWriter.println("DEFAULT_SECTION_SEPARATOR FONTSTYLE B"); // consisting of a single line		
		printWriter.close();
		
		String sectionSeparator = "<b>$</b>";		
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
		assertEquals(leadInFull + expectedHelmString + "\n"  + leadOut, stringWriter.toString());
		
	}

}

