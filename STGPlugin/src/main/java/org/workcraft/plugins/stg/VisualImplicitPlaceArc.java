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

package org.workcraft.plugins.stg;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionImpl;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.ExpressionPropertyDeclaration;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class VisualImplicitPlaceArc extends VisualConnection {
	
	static <T> ModifiableExpression<T> unfold(final Expression<ModifiableExpression<T>> expr) {
		return new ModifiableExpressionImpl<T>() {

			@Override
			protected void simpleSetValue(T newValue) {
				eval(expr).setValue(newValue);
			}

			@Override
			protected T evaluate(EvaluationContext context) {
				return context.resolve(context.resolve(expr));
			}
		};
	}
	
	private final Variable<STGPlace> implicitPlace = Variable.create(null);
	private final Variable<MathConnection> refCon1 = Variable.create(null);
	private final Variable<MathConnection> refCon2 = Variable.create(null);
	
	private static double tokenSpaceSize = 0.8;
	private static double singleTokenSize = tokenSpaceSize / 1.9;
	private static double multipleTokenSeparation = 0.0125;
	private static Color tokenColor = Color.BLACK;
	
	private void addPropertyDeclarations() {
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Tokens", tokens(), Integer.class));
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Capacity", capacity(), Integer.class));
		
		/*addPopupMenuSegment(new PopupMenuBuilder.PopupMenuSegment() {
			public void addItems(JPopupMenu menu,
					ScriptedActionListener actionListener) {
				ScriptedActionMenuItem addToken = new ScriptedActionMenuItem(new VisualPlace.AddTokenAction(implicitPlace));
				addToken.addScriptedActionListener(actionListener);
				
				ScriptedActionMenuItem removeToken = new ScriptedActionMenuItem(new VisualPlace.RemoveTokenAction(implicitPlace));
				removeToken.addScriptedActionListener(actionListener);
				
				menu.add(new JLabel ("Implicit place"));
				menu.addSeparator();
				menu.add(addToken);
				menu.add(removeToken);				
			}
		});*/
	}
	
	public VisualImplicitPlaceArc () {
		super();
		addPropertyDeclarations();
	}
	
	public void setImplicitPlaceArcDependencies (MathConnection refCon1, MathConnection refCon2, STGPlace implicitPlace) {
		this.refCon1.setValue(refCon1);
		this.refCon2.setValue(refCon2);
		this.implicitPlace.setValue(implicitPlace);
	}

	public VisualImplicitPlaceArc (VisualComponent first, VisualComponent second, MathConnection refCon1, MathConnection refCon2, STGPlace implicitPlace) {
		super(null, first, second);
		this.refCon1.setValue(refCon1);
		this.refCon2.setValue(refCon2);
		this.implicitPlace.setValue(implicitPlace);
		
		addPropertyDeclarations();
	}

	@Override
	public Expression<? extends GraphicalContent> graphicalContent() {
		
		final Expression<? extends GraphicalContent> superGraphicalContent = super.graphicalContent();
		return new ExpressionBase<GraphicalContent>() {

			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				final int tokens = context.resolve(tokens());
				
				return new GraphicalContent() {

					@Override
					public void draw(DrawRequest r) {
						
						Point2D p = getPointOnConnection(0.5);
						
						r.getGraphics().translate(p.getX(), p.getY());		
						VisualPlace.drawTokens(tokens, singleTokenSize, multipleTokenSeparation, tokenSpaceSize, 0, Coloriser.colorise(tokenColor, r.getDecoration().getColorisation()), r.getGraphics());
						
						context.resolve(superGraphicalContent).draw(r);
					}
					
				};
			}
			
		};
	}
	
	@NoAutoSerialisation
	public ModifiableExpression<Integer> tokens() {
		return unfold(new ExpressionBase<ModifiableExpression<Integer>>(){
			@Override
			public ModifiableExpression<Integer> evaluate(EvaluationContext context) {
				return context.resolve(implicitPlace).tokens();
			}
		});
	}

	@NoAutoSerialisation
	public ModifiableExpression<Integer> capacity() {
		return unfold(new ExpressionBase<ModifiableExpression<Integer>>(){
			@Override
			public ModifiableExpression<Integer> evaluate(EvaluationContext context) {
				return context.resolve(implicitPlace).capacity();
			}
		});
	}

	@NoAutoSerialisation
	public STGPlace getImplicitPlace() {
		return eval(implicitPlace);
	}

	public MathConnection getRefCon1() {
		return eval(refCon1);
	}

	public MathConnection getRefCon2() {
		return eval(refCon2);
	}

	@Override
	public Set<MathNode> getMathReferences() {
		Set<MathNode> ret = new HashSet<MathNode>();
		ret.add(getImplicitPlace());
		ret.add(getRefCon1());
		ret.add(getRefCon2());
		return ret;
	}
	
}
