package org.workcraft.plugins.cpog;

import org.workcraft.util.Function;
import org.workcraft.util.Function0;

public class CheckedPrefixNameGen implements Function0<String> {

	private final Function<String, Boolean> nameChecker;
	private final String prefix;
	private int counter = 0;

	public CheckedPrefixNameGen(String prefix, Function<String, Boolean> nameChecker) {
		this.prefix = prefix;
		this.nameChecker = nameChecker;
	}
	
	@Override
	public String apply() {
		while(true) {
			String nextName = prefix + (counter++);
			if(nameChecker.apply(nextName))
				return nextName;
		}
	}
}
