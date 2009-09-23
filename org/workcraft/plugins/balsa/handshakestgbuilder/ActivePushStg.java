package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.Event;

public interface ActivePushStg extends ActiveProcess 
{
	public Event dataRelease();
}
