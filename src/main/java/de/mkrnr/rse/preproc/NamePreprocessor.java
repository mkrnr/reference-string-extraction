package de.mkrnr.rse.preproc;

import org.apache.commons.lang3.text.WordUtils;

public class NamePreprocessor {
    public static String preprocessName(String name) {
	String preprocessedName = name;
	String nonLetterRegex = "\\W*";
	// String nonLetterRegex = "";
	preprocessedName = preprocessedName.replaceAll("\\(", "");
	preprocessedName = preprocessedName.replaceAll("\\)", "");
	preprocessedName = preprocessedName.replaceAll("^" + nonLetterRegex, "");
	preprocessedName = preprocessedName.replaceAll(nonLetterRegex + "$", "");
	preprocessedName = preprocessedName.replaceAll("\\.+\\s*", " ");
	preprocessedName = WordUtils.capitalizeFully(preprocessedName);
	return preprocessedName;

    }

}
