package org.workcraft.gui.trees;

import javax.swing.Icon;

import org.workcraft.dependencymanager.advanced.core.Expression;

public interface ReactiveTreeDecorator<Node> {
	public Expression<String> name(Node node);
	public Expression<Icon> icon(Node node);
}
