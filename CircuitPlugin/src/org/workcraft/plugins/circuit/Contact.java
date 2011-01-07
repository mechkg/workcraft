/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
* 
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.circuit;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionImpl;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;

@DisplayName("Contact")
@VisualClass("org.workcraft.plugins.circuit.VisualContact")

public class Contact extends MathNode implements BooleanVariable {

	public enum IoType { INPUT, OUTPUT};
	private final IoTypeVariable ioType;
	private final ModifiableExpression<String> name;
	private Variable<Boolean> initOne = Variable.create(false);
	
	public ModifiableExpression<Boolean> initOne() {
		return initOne;
	}

	public Contact() {
		this(IoType.OUTPUT, "");
	}
	
	public Contact(IoType ioType, String name) {
		super();
		
		this.ioType = new IoTypeVariable(ioType);
		this.name = Variable.create(name);
	}
	
	
	static public String getNewName(Node n, String start, Node curNode, boolean allowShort) {
		// iterate through all contacts, check that the name doesn't exist
		int num=0;
		boolean found = true;
		
		while (found) {
			num++;
			found=false;
			
			if (allowShort) {
				
				for (Node vn : eval(n.children())) {
					if (vn instanceof Contact&& vn!=curNode) {
						if (eval(((Contact)vn).name()).equals(start)) {
							found=true;
							break;
						}
					}
				}
				if (found==false) return start;
				allowShort=false;
			}
			
			for (Node vn : eval(n.children())) {
				if (vn instanceof Contact&& vn!=curNode) {
					if (eval(((Contact)vn).name()).equals(start+num)) {
						found=true;
						break;
					}
				}
			}
		}
		return start+num;
	}
	
	public void checkName(Node parent) {
		if (parent==null) return;
		String start=eval(name());
		if (start==null||start=="") {
			if (eval(ioType())==IoType.INPUT) {
				start="input";
			} else {
				start="output";
			}
			name().setValue(getNewName(parent, start, this, false));
		}
	}
	
	@Override
	public ModifiableExpression<Node> parent() {
		return new ModifiableExpressionImpl<Node>() {

			@Override
			protected void simpleSetValue(Node newParent) {
				Contact.super.parent().setValue(newParent);
				checkName(newParent);
			}

			@Override
			protected Node evaluate(EvaluationContext context) {
				return context.resolve(Contact.super.parent());
			}
		};
	}

	class IoTypeVariable extends Variable<IoType> {
		public IoTypeVariable(IoType initialValue) {
			super(initialValue);
		}

		@Override
		public void setValue(IoType value) {
			super.setValue(value);
			if (eval(name()).startsWith("input")&&value==IoType.OUTPUT) {
				name().setValue(getNewName(eval(parent()), "output", Contact.this, false));
			} else if (eval(name()).startsWith("output")&&value==IoType.INPUT) {
				name().setValue(getNewName(eval(parent()), "input", Contact.this, false));
			}
		}
	}
	
	public ModifiableExpression<IoType> ioType() {
		return ioType;
	}
	
	public ModifiableExpression<String> name() {
		return name;
	}


	@Override
	public <T> T accept(BooleanVisitor<T> visitor) {
		return visitor.visit(this);
	}


	@Override
	public String getLabel() {
		return eval(name());
	}

}
