package com.quattroresearch.helm;

import java.io.*;
import static org.junit.Assert.*;
import org.junit.Test;

import com.quattroresearch.helm.HelmHighlighter;

public class HighlighterGeneratesUnchangedStringForConfigWithNoHighlighting {
	// leadInFull: starting HTML sequence, leadOut: closing HTML sequence in the output HTML file
	String leadInPart1	= "<html>\n<head>\n<meta charset=\"UTF-8\">\n<font style=\"font-size: ";
	String leadInPart2	= HelmHighlighter.DEFAULT_FONTSIZE;
	String leadInPart3	= "pt\">\n</head>\n<body>\n";
	String leadInFull	= leadInPart1 + leadInPart2 + leadInPart3;	
	String leadOut		= "</body>\n</html>";

	@Test
	public void test() throws Exception {
		String configFileName = "/tmp/EmptyHelmHighlighterConfig.txt";
		PrintWriter printWriter = new PrintWriter(configFileName); // create a new empty config file for this test
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
		// Assert that the output HTML file only contains the lead-in and lead-out HTML seqences and the unchanged HELM2 string between them:
		assertEquals(leadInFull + expectedHelmString + "\n"  + leadOut, stringWriter.toString());
	}

}


