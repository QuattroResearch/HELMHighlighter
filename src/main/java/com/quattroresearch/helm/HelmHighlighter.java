package com.quattroresearch.helm;

import java.io.*;
import java.util.regex.*;
import org.apache.commons.cli.*;
import org.helm.notation2.parser.*;
import org.helm.notation2.parser.notation.HELM2Notation;
import org.helm.notation2.parser.exceptionparser.ExceptionState;
import org.jdom2.JDOMException;

class HlConfigEntry {
	String category;
	String keyword;
	String value;
	String preHtmlSeq;
	String postHtmlSeq;
	
	HlConfigEntry() {}	
	HlConfigEntry(String category, String keyword, String value) {
		this.category = category;
		this.keyword  = keyword;
		this.value    = value;
	}
}

public class HelmHighlighter {
	final static String DEFAULT_COLOR = "#000000"; // Black
	final static String DEFAULT_FONTSIZE = "12";
	
	static HlConfigEntry defaultFontsizeEntry;
	static HlConfigEntry defaultColorEntry;
	static HlConfigEntry defaultSectionSeparator;
	static HlConfigEntry [] hlConfigEntries = null;


public static void main(String[] args) throws Exception {
	CommandLineParser cmdline_parser = new DefaultParser();
	Options options = new Options();
	String usage = "Usage: -i <input_file> -o <output_file> -c <highlight_config_file>";
	String errorLine;

	if (args.length < 6) {
		log(usage);
		return;
	}
	
	options.addOption("c", true, "config file containing highlighting options");
	options.addOption("i", true, "input file containing HELM string");
	options.addOption("o", true, "output file containing highlighted HELM string");

	try { // parse the command line arguments
		CommandLine cmd_line = cmdline_parser.parse(options, args);
		String cfilename = cmd_line.getOptionValue("c");    log("Cfg filename=" + cfilename);
		String ofilename = cmd_line.getOptionValue("o");    log("Output filename=" + ofilename);
		String ifilename = cmd_line.getOptionValue("i");    log("Input filename=" + ifilename);
		if (cfilename == null || ofilename == null || ifilename == null ||
				cfilename.equals("") || ifilename.equals("") || ofilename.equals("")) {
			log(usage);
			return;
		}
		if (! (errorLine = setupHlConfigEntries(cfilename)).equals("")) { // in case of error, a non-empty string will be returned
			log("Bad entry in " + cfilename +  ": '" + errorLine + "': missing category, keyword or value, exiting..."); // error
			return;
		}
		handleInputOutputFiles(ifilename, ofilename);
	} catch (IOException e) {
		System.out.println(e);
	}
}

public static String setupHlConfigEntries(String cfilename) throws IOException {
	int lineCount = 0;
	int hlConfigEntriesIndex = 0;
	String currentLine = "";
	String[] splitResult;

	// Count the non-commentary config lines in order to determine the dimension of the hlConfigEntries[] array:
	BufferedReader br = new BufferedReader(new FileReader(cfilename));
	while ((currentLine = br.readLine()) != null) { // count non-commentary cfg lines
		if (currentLine.equals("") || currentLine.charAt(0) == '#' || currentLine.contains("NO_HIGHLIGHTING") || currentLine.contains("DEFAULT")) {
			// log("IGNORED: " + currentLine);
			continue; // do not count lines that are empty, start with '#', contain "NO_HIGHLIGHTING" or "DEFAULT"
		}
		lineCount++;
	}
	br.close();
	
	// Create the special entries defaultFontsizeEntry, defaultColorEntry, defaultSectionSeparator with program-internal default values:
	defaultColorEntry		= new HlConfigEntry("DEFAULT_COLOR", "COLOR", DEFAULT_COLOR);
	translateCfgToHtmlSeq(defaultColorEntry);
	defaultFontsizeEntry	= new HlConfigEntry("DEFAULT_FONTSIZE", "FONTSIZE", DEFAULT_FONTSIZE);
	translateCfgToHtmlSeq(defaultFontsizeEntry);
	defaultSectionSeparator	= new HlConfigEntry("DEFAULT_SECTION_SEPARATOR", "FONTSIZE", DEFAULT_FONTSIZE);
	// no default internal highlighting of '$': empty HTML sequences
	defaultSectionSeparator.preHtmlSeq  = defaultSectionSeparator.postHtmlSeq = "";
	
	// Knowing the count of the effective cfg lines, create the "hlConfigEntries" array now. Note that the entries
	// DEFAULT_FONTSIZE, DEFAULT_COLOR, DEFAULT_SECTION_SEPARATOR have separate instances in the class "HelmHighlighter"
	// (namely defaultFontsizeEntry, defaultColorEntry, defaultSectionSeparator), therefore they will have no entries
	// in the hlConfigEntries[] array (they have already been omitted while previously computing "lineCount").
	hlConfigEntries = new HlConfigEntry[lineCount];	
	for (int i = 0; i < hlConfigEntries.length; i++) {
		hlConfigEntries[i] = new HlConfigEntry();
	}

	// Read again the config file, ignoring all lines that are empty, start with '#' or contain "NO_HIGHLIGHTING":
	br = new BufferedReader(new FileReader(cfilename)); // re-initialize the readers to start again from the beginning
	while ((currentLine = br.readLine()) != null) {
		if (currentLine.equals("") || currentLine.charAt(0) == '#' || currentLine.contains("NO_HIGHLIGHTING")) {
			continue; // ignore lines that are empty, start with '#' or ' ', or contain "NO_HIGHLIGHTING"
		}
		splitResult = currentLine.split("[ \t]+"); // '+' to suppress subsequent delimiters    
		if (splitResult.length < 3) { // error: at least one element is missing from the triple "<category> <keyword> <value>":
			br.close();
			return(currentLine); // Return the bad config line entry
		}
		switch (splitResult[0]) {
		case "DEFAULT_FONTSIZE": // Fill defaultFontsizeEntry:
			defaultFontsizeEntry.category = splitResult[0];
			defaultFontsizeEntry.keyword = splitResult[1];
			defaultFontsizeEntry.value = splitResult[2];
			translateCfgToHtmlSeq(defaultFontsizeEntry);
			break;
		case "DEFAULT_COLOR": // Fill defaultColorEntry:
			defaultColorEntry.category = splitResult[0];
			defaultColorEntry.keyword = splitResult[1];
			defaultColorEntry.value = splitResult[2];
			translateCfgToHtmlSeq(defaultColorEntry);
			break;
		case "DEFAULT_SECTION_SEPARATOR": // Fill defaultSectionSeparator:
			defaultSectionSeparator.category = splitResult[0];
			defaultSectionSeparator.keyword = splitResult[1];
			defaultSectionSeparator.value = splitResult[2];
			if (! defaultSectionSeparator.keyword.equals("FONTSIZE") ||	! defaultSectionSeparator.value.equals(DEFAULT_FONTSIZE)) {
				// translate only if something has changed (i.e., the default is no longer valid):
				translateCfgToHtmlSeq(defaultSectionSeparator);
			}
			break;
		default: // All other config entries:
			hlConfigEntries[hlConfigEntriesIndex].category = splitResult[0];
			hlConfigEntries[hlConfigEntriesIndex].keyword  = splitResult[1];
			hlConfigEntries[hlConfigEntriesIndex].value = splitResult[2];
			translateCfgToHtmlSeq(hlConfigEntries[hlConfigEntriesIndex]);
			hlConfigEntriesIndex++;
			break;
		}
	}
	// log("SIZEOF hlConfigEntries: " + hlConfigEntries.length);
	br.close();
	return(""); // there was no bad config entry, return the empty string
}
	
public static void translateCfgToHtmlSeq(HlConfigEntry configEntry) {
	String fontstyle;
	String colorHtmlCode;
	switch (configEntry.keyword) {
	case "FONTSTYLE":
		fontstyle = configEntry.value.toLowerCase();
		configEntry.preHtmlSeq  = "<"  + fontstyle + ">";
		configEntry.postHtmlSeq = "</" + fontstyle + ">";
		break;
	case "FONTSIZE":
		configEntry.preHtmlSeq  = "<font style=\"font-size: " + configEntry.value + "pt\">";
		configEntry.postHtmlSeq = "</font>";	
		break;
	case "COLOR":
		switch (configEntry.value) {							// translate text into HTML color code sequence:
		case "RED":	colorHtmlCode = "#ff0000";			break;	// RED
		case "GRE":	colorHtmlCode = "#00b050";			break;	// GREEN
		case "BLU":	colorHtmlCode = "#00b0f0";			break;	// BLUE
		case "YEL":	colorHtmlCode = "#ffc000";			break;	// YELLOW
		case "GRY":	colorHtmlCode = "#808080";			break;	// GRAY
		case "BLK":	colorHtmlCode = "#000000";			break;	// BLACK
		default:	colorHtmlCode = configEntry.value;	break;	// HTML color code sequence	
		}
		configEntry.preHtmlSeq  = "<font color=\"" + colorHtmlCode + "\">";
		configEntry.postHtmlSeq = "</font>";	
		break;
	}
	// logNoEOL("Config entry: category=" + configEntry.category + ", keyword=" + configEntry.keyword + ", value=" + configEntry.value);
	// log(", preHtmlSeq=" + configEntry.preHtmlSeq + ", postHtmlSeq=" + configEntry.postHtmlSeq);
}

public static void handleInputOutputFiles(String ifilename, String ofilename) throws IOException, JDOMException {
	BufferedReader br = new BufferedReader(new FileReader(ifilename));
	PrintWriter pw = new PrintWriter(ofilename);
	String helmString;

	// initialize the output HTML file:
	pw.println("<html>\n<head>\n<meta charset=\"UTF-8\">\n<font style=\"font-size: " + defaultFontsizeEntry.value + "pt\">\n</head>\n<body>");

	while ((helmString = br.readLine()) != null) { // read and process all HELM2-Strings from the input file
		processHelmString(helmString, pw);
	}
	br.close();
	
	pw.println("</body>\n</html>"); // write out the closing HTML sequence
	pw.flush();
	pw.close();
}


public static void processHelmString(String helmString, PrintWriter pw) throws IOException, JDOMException  {
	String outputString = "";
	String simplePolymerSect, connectionsSect, groupingSect, extAnnotSect;
	String sectionName;
	String sectionSeparator;
	int simplePolymersEndIndex, connectionsEndIndex, groupingEndIndex, extAnnotEndIndex;
	int hlConfigEntriesIndex = 0;
	Pattern pattern;
	Matcher matcher;

	// The next piece of code obtains the substrings of the individual sections from the input with the help of the ParserHELM2 class.
	// As a side-effect, one can perform a syntax check of the input HELM-string by concatenating the resulting substrings and
	// matching the result to the input HELM-string: if they are equal, the input string should be syntactically correct.
	
	// Begin code depending on Helm2Parser (note also JDOMException and the org.helm.notation2.parser-relevant imports)
	ParserHELM2 helm_parser = new ParserHELM2();
	try {
		helm_parser.parse(helmString);
	} catch (ExceptionState e) { // Auto-generated catch block
		e.printStackTrace();
	}
	HELM2Notation notation = helm_parser.getHELM2Notation();
	simplePolymerSect = notation.polymerToHELM2();
	connectionsSect   = notation.connectionToHELM2();
	groupingSect      = notation.groupingToHELM2();
	extAnnotSect      = notation.annotationToHELM2();
	outputString = simplePolymerSect + "$" + connectionsSect + "$" + groupingSect + "$" + extAnnotSect + "$" + "V2.0";
	log("outputStrg=" + outputString);	log("helmString=" + helmString);
	if (! outputString.equals(helmString)) {
		System.out.println("Helm string syntax check failed, input helmstring and toHELM2 outputString differ, quit working");
		return;
	}
	// End code depending on Helm2Parser

	// If the dependency between the Highlighter and the HelmParser is undesired and no syntax check is necessary, use the following
	// code (i.e., you can completely remove the above section with all references to HELM2Notation, ParserHELM2, JDOMException etc).
	// Here, we obtain the substrings of the individual sections from the input by identifying the dollar characters that serve as
	// section delimiters (as opposed to those of a SMILES-string).
	// The xxxEndIndex variables will contain the indices to the pertaining closing '$' characters of the respective sections.
	// Subtract 1 from the xxxEndIndex variables to start the next backward search one character BEFORE the previously found '$'.
	// On cutting out the substrings of the individual sections, add 1 to skip the previous '$' delimiter.

	/* // Begin Helm2Parser-independent code
	extAnnotEndIndex		= helmString.lastIndexOf('$'); // the position of the last '$' in the whole HELM2-string
	groupingEndIndex		= helmString.lastIndexOf('$', extAnnotEndIndex - 1);
	connectionsEndIndex		= helmString.lastIndexOf('$', groupingEndIndex - 1);
	simplePolymersEndIndex	= helmString.lastIndexOf('$', connectionsEndIndex - 1);
	simplePolymerSect		= helmString.substring(0, simplePolymersEndIndex);
	connectionsSect			= helmString.substring(simplePolymersEndIndex + 1, connectionsEndIndex);
	groupingSect			= helmString.substring(connectionsEndIndex + 1, groupingEndIndex);
	extAnnotSect			= helmString.substring(groupingEndIndex + 1, extAnnotEndIndex);
	// End Helm2Parser-independent code */

	log("simplePolymerSect=" + simplePolymerSect);
	log("connectionsSect=" + connectionsSect);
	log("groupingSect=" + groupingSect);
	log("extAnnotSect=" + extAnnotSect);

	// process all entries of the "hlConfigEntries" array:
	for (hlConfigEntriesIndex = 0; hlConfigEntriesIndex < hlConfigEntries.length; hlConfigEntriesIndex++) {
		pattern = Pattern.compile("^[^_]+");
		matcher = pattern.matcher(hlConfigEntries[hlConfigEntriesIndex].category);
		matcher.find();
		switch (sectionName = matcher.group()) {
		case "POLYMER":		simplePolymerSect	= regexEngine(simplePolymerSect, hlConfigEntries[hlConfigEntriesIndex]);	break;
		case "CONNECTION":	connectionsSect		= regexEngine(connectionsSect,   hlConfigEntries[hlConfigEntriesIndex]); 	break;
		case "GROUPING":	groupingSect		= regexEngine(groupingSect,      hlConfigEntries[hlConfigEntriesIndex]); 	break;
		case "EXTANNOT":	extAnnotSect		= regexEngine(extAnnotSect,      hlConfigEntries[hlConfigEntriesIndex]);	break;
		}
	}
	// Compose the output HELM2 string from the four sections, '$' characters and the version string.
	sectionSeparator = defaultSectionSeparator.preHtmlSeq + "$" + defaultSectionSeparator.postHtmlSeq;
	outputString =	simplePolymerSect + sectionSeparator + connectionsSect + sectionSeparator +
					groupingSect + sectionSeparator + extAnnotSect  + sectionSeparator + "V2.0";	
	pw.println(outputString);
}

public static String regexEngine(String sectionString, HlConfigEntry configEntry) {
	String outString = "";
	String hlTarget;
	Pattern pattern;
	Matcher matcher;
	
	pattern = Pattern.compile("^.*SECTION_(.*)");
	matcher = pattern.matcher(configEntry.category);
	matcher.find();
	hlTarget = matcher.group(1);
	// log("HLTARGET=" + hlTarget + ", SectionString: '" + sectionString + "'" + "configEntry.category=" + configEntry.category);

	switch (hlTarget) {
	case "PEPTIDE_ID":				pattern = Pattern.compile("(PEPTIDE[0-9]+)");	break;
	case "RNA_ID":					pattern = Pattern.compile("(RNA[0-9]+)");		break;
	case "CHEM_ID":					pattern = Pattern.compile("(CHEM[0-9]+)");		break;
	case "BLOB_ID":					pattern = Pattern.compile("(BLOB[0-9]+)");		break;
	case "GROUP_ID":				pattern = Pattern.compile("(G[0-9]+)");			break;
	case "BRANCH_MONOMER_ID":		pattern = Pattern.compile("(\\([A-Z]\\))");		break;
	case "MULTICHAR_MONOMER_ID":	pattern = Pattern.compile("(\\[[A-Za-z]+\\])");	break;
	case "MONOMER_SEPARATOR_DOT":	pattern = Pattern.compile("(\\.)");				break;
	case "SEPARATOR_PIPE":			pattern = Pattern.compile("(\\|)");				break;
	}
	
	matcher = pattern.matcher(sectionString);
	outString = matcher.replaceAll(configEntry.preHtmlSeq + "$1" + configEntry.postHtmlSeq);
	// log("OUTSTRING=" + outString);
	return(outString);
}


private static void log(String msg) {
  System.out.println(msg);
}

private static void logNoEOL(String msg) {
  System.out.print(msg);
}

}





