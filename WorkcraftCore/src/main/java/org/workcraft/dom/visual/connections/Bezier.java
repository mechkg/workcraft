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

package org.workcraft.dom.visual.connections;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.util.ExpressionUtil;

public class Bezier implements ConnectionGraphicConfiguration {

	private Node parent;
	private final ModifiableExpression<BezierControlPoint> cp1;
	private final ModifiableExpression<BezierControlPoint> cp2;
	public final StorageManager storage;
	
	public Bezier(VisualConnection parent, StorageManager storage) {
		this.storage = storage;
		cp1 = storage.create(null);
		cp2 = storage.create(null);
		
		this.parent = parent;
	}
	
	@Override
	public ModifiableExpression<Node> parent() {
		return ExpressionUtil.modificationNotSupported(parentConnection());
	}

	private ExpressionBase<Node> parentConnection() {
		return new ExpressionBase<Node>() {
			@Override
			protected Node evaluate(EvaluationContext context) {
				return parent;
			} 
		};
	}

	public void initControlPoints(BezierControlPoint cp1, BezierControlPoint cp2) {
		this.cp1.setValue(cp1);
		this.cp2.setValue(cp2);
	}
	
	public void finaliseControlPoints() {
		eval(cp1).parent().setValue(this);
		eval(cp2).parent().setValue(this);
	}
	
	public Expression<BezierControlPoint[]> getControlPoints() {
		return new ExpressionBase<BezierControlPoint[]>(){

			@Override
			protected BezierControlPoint[] evaluate(EvaluationContext context) {
				return new BezierControlPoint[] { context.resolve(cp1), context.resolve(cp2) };
			}
		};
	}
	@Override
	public Expression<? extends Collection<? extends ControlPoint>> children() {
		return new ExpressionBase<List<BezierControlPoint>>(){
			@Override
			protected List<BezierControlPoint> evaluate(
					EvaluationContext context) {
				return Arrays.asList(context.resolve(getControlPoints()));
			}
		};
	}

	@Override
	public <T> T accept(ConnectionGraphicConfigurationVisitor<T> visitor) {
		return visitor.visitBezier(new BezierConfiguration() {
			
			@Override
			public Expression<? extends Point2D> controlPoint2() {
				throw new NotImplementedException();
			}
			
			@Override
			public Expression<? extends Point2D> controlPoint1() {
				throw new NotImplementedException();
			}
		});
	}
}
