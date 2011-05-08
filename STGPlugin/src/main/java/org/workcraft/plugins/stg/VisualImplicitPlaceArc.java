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

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;
import static org.workcraft.util.Graphics.*;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionImpl;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.TouchableProvider;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.ParametricCurve;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnectionGui;
import org.workcraft.dom.visual.connections.VisualConnectionGui.ExprConnectionGui;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.integer.IntegerProperty;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.serialisation.xml.NoAutoSerialisation;
import org.workcraft.util.Function2;

import pcollections.PVector;

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
	
	private final ModifiableExpression<STGPlace> implicitPlace;
	private final ModifiableExpression<MathConnection> refCon1;
	private final ModifiableExpression<MathConnection> refCon2;
	
	private static double tokenSpaceSize = 0.8;
	private static double singleTokenSize = tokenSpaceSize / 1.9;
	private static double multipleTokenSeparation = 0.0125;
	private static Color tokenColor = Color.BLACK;
	
	@Override
	public PVector<EditableProperty> getProperties() {
		return super.getProperties()
			.plus(IntegerProperty.create("Tokens", tokens()))
			.plus(IntegerProperty.create("Capacity", capacity()))
			;
	}
	
	public VisualImplicitPlaceArc (StorageManager storage) {
		super(storage);
		implicitPlace = storage.create(null);
		refCon1 = storage.create(null);
		refCon2 = storage.create(null);
		
	}
	
	public void setImplicitPlaceArcDependencies (MathConnection refCon1, MathConnection refCon2, STGPlace implicitPlace) {
		this.refCon1.setValue(refCon1);
		this.refCon2.setValue(refCon2);
		this.implicitPlace.setValue(implicitPlace);
	}

	public VisualImplicitPlaceArc (VisualComponent first, VisualComponent second, MathConnection refCon1, MathConnection refCon2, STGPlace implicitPlace, StorageManager storage) {
		super(null, first, second, storage);
		this.refCon1 = storage.create(refCon1);
		this.refCon2 = storage.create(refCon2);
		this.implicitPlace  = storage.create(implicitPlace);
	}

	public static Expression<? extends ColorisableGraphicalContent> graphicalContent(TouchableProvider<Node> tp, VisualImplicitPlaceArc arc) {
		
		ExprConnectionGui gui = VisualConnectionGui.getConnectionGui(TouchableProvider.Util.podgonHideMaybe(tp), arc);
		Function2<ParametricCurve, Integer, ColorisableGraphicalContent> drawTokens = new Function2<ParametricCurve, Integer, ColorisableGraphicalContent>(){
			@Override
			public ColorisableGraphicalContent apply(final ParametricCurve curve, final Integer tokens) {
				return new ColorisableGraphicalContent() {
					
					@Override
					public void draw(DrawRequest r) {
						Point2D p = curve.getPointOnCurve(0.5);
						
						r.getGraphics().translate(p.getX(), p.getY());		
						VisualPlace.drawTokens(tokens, singleTokenSize, multipleTokenSeparation, tokenSpaceSize, 0, Coloriser.colorise(tokenColor, r.getColorisation().getColorisation()), r.getGraphics());
					}
				};
			}
		};
		return fmap(composeColorisable, gui.graphicalContent(), fmap(drawTokens, gui.parametricCurve(), arc.tokens()));
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
