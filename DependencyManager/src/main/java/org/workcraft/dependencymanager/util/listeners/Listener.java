package org.workcraft.dependencymanager.util.listeners;

import org.workcraft.util.Action;

public interface Listener {
	Action changed(); // returns the action to be performed when everything invalid has been invalidated.
}
