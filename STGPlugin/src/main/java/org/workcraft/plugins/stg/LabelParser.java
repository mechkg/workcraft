package org.workcraft.plugins.stg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.util.Pair;
import org.workcraft.util.Triple;

public class LabelParser {
	private static final Pattern fullPattern = Pattern
			.compile("^([_A-Za-z][_A-Za-z0-9]*)([\\+\\-\\~])(\\/([0-9]+))?");

	private static final Pattern pattern = Pattern
	.compile("^([_A-Za-z][_A-Za-z0-9]*[\\+\\-\\~]?)(\\/([0-9]+))?");
	
	public static Pair<String, String> parseImplicitPlaceReference(String ref) {
		String[] parts = ref.replaceAll(" ", "").split(",");
		
		if (parts.length < 2 || !parts[0].startsWith("<") || !parts[0].endsWith(">"))
			return null;
		
		return Pair.of(parts[0].substring(1), parts[1].substring(0, parts[1].length()-1));
	}
	
	public static Triple<String, SignalTransition.Direction, Integer> parseFull(String s) {
		final Matcher matcher = fullPattern.matcher(s);
		
		if (!matcher.find())
			return null;
 
		if (! (matcher.end() == s.length()))
			return null;
		
		final String instanceText = matcher.group(4);
		final String directionText = matcher.group(2);
		
		final Direction second;
		
		if (directionText.equals("+"))
			second = SignalTransition.Direction.PLUS;
		else if (directionText.equals("-"))
			second = SignalTransition.Direction.MINUS;
		else
			second = SignalTransition.Direction.TOGGLE;
		
		return Triple.of(matcher.group(1),
				second, instanceText == null? null : Integer
						.parseInt(instanceText));
	}
	
	public static Pair<String, Integer> parse(String s) {
		final Matcher matcher = pattern.matcher(s);
		
		if (!matcher.find())
			return null;
 
		if (! (matcher.end() == s.length()))
			return null;
			
		final String instanceGroup = matcher.group(3);
		
		return Pair.of(matcher.group(1), instanceGroup == null? null : Integer.parseInt(instanceGroup));
	}
}
