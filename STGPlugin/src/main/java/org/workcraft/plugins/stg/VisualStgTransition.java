package org.workcraft.plugins.stg;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;

public class VisualStgTransition extends VisualComponent {
	public VisualStgTransition(MathNode refNode, StorageManager storage) {
		super(refNode, storage);
	}

	public StgTransition getReferencedTransition(){
		MathNode refComp = super.getReferencedComponent();
		if (refComp instanceof SignalTransition)
			return ((SignalTransition) refComp).asSignal();
		else
			if (refComp instanceof DummyTransition)
				return ((DummyTransition) refComp).asSignal();
			else
				throw new RuntimeException("bad case");
	}
}
