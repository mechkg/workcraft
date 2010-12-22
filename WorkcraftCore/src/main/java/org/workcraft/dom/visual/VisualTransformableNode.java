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

package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Element;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionImpl;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.gui.propertyeditor.ExpressionPropertyDeclaration;
import org.workcraft.serialisation.xml.NoAutoSerialisation;
import org.workcraft.util.Geometry;


public abstract class VisualTransformableNode extends VisualNode implements MovableNew {
	private final static class AffineTransform_X extends ModifiableExpressionImpl<Double> {
		private final ModifiableExpression<AffineTransform> transform;

		public AffineTransform_X(ModifiableExpression<AffineTransform> transform) {
			this.transform = transform;
		}
		
		@Override
		public Double evaluate(EvaluationContext context) {
			return context.resolve(transform).getTranslateX(); 
			
		}

		@Override
		public void simpleSetValue(Double newValue) {
			AffineTransform old = GlobalCache.eval(transform);
			transform.setValue(translate(old, newValue-old.getTranslateX(), 0));
		}
	}

	private final static class AffineTransform_Y extends ModifiableExpressionImpl<Double> {
		private final ModifiableExpression<AffineTransform> transform;

		public AffineTransform_Y(ModifiableExpression<AffineTransform> transform) {
			this.transform = transform;
		}
		
		@Override
		public Double evaluate(EvaluationContext context) {
			return context.resolve(transform).getTranslateY(); 
			
		}

		@Override
		public void simpleSetValue(Double newValue) {
			AffineTransform old = GlobalCache.eval(transform);
			transform.setValue(translate(old, 0, newValue-old.getTranslateY()));
		}
	}

	private final static class AffineTransform_Position extends ModifiableExpressionImpl<Point2D> {
		private final AffineTransform_X x;
		private final AffineTransform_Y y;

		public AffineTransform_Position(ModifiableExpression<AffineTransform> transform) {
			this.x = new AffineTransform_X(transform);
			this.y = new AffineTransform_Y(transform);
		}
		
		@Override
		public Point2D evaluate(EvaluationContext resolver) {
			return new Point2D.Double(resolver.resolve(x), resolver.resolve(y)); 
		}

		@Override
		public void simpleSetValue(Point2D newValue) {
			x.setValue(newValue.getX());
			y.setValue(newValue.getY());
		}
	}

	protected Variable<AffineTransform> localToParentTransform = new Variable<AffineTransform>(new AffineTransform());
	protected ExpressionBase<AffineTransform> parentToLocalTransform = new ExpressionBase<AffineTransform>(){
		@Override
		public AffineTransform evaluate(org.workcraft.dependencymanager.advanced.core.EvaluationContext resolver) {
			return Geometry.optimisticInverse(resolver.resolve(localToParentTransform));
		}
	};
	
	private void addPropertyDeclarations() {
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("X", x(), Double.class));
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Y", y(), Double.class));
	}

	public VisualTransformableNode() {
		super();
		addPropertyDeclarations();
	}

	public VisualTransformableNode (Element visualNodeElement) {
		super();
		addPropertyDeclarations();
		
		VisualTransformableNodeDeserialiser.initTransformableNode(visualNodeElement, this);
	}
	
	static AffineTransform translate(AffineTransform original, double dx, double dy) {
		AffineTransform res = new AffineTransform(original);
		res.translate(dx, dy);
		return res;
	}

	@NoAutoSerialisation
	public ModifiableExpression<Double> x() {
		return new AffineTransform_X(localToParentTransform);
	}

	@NoAutoSerialisation
	public ModifiableExpression<Double> y() {
		return new AffineTransform_Y(localToParentTransform);
	}
	
	@NoAutoSerialisation
	public ModifiableExpression<Point2D> position() {
		return new AffineTransform_Position(localToParentTransform);
	}

	protected Rectangle2D transformToParentSpace(Rectangle2D rect)
	{
		if(rect == null)
    		return null;
		
		Point2D p0 = new Point2D.Double(rect.getMinX(), rect.getMinY()); 
		Point2D p1 = new Point2D.Double(rect.getMaxX(), rect.getMaxY());
		
		AffineTransform t = getLocalToParentTransform();
		t.transform(p0, p0);
		t.transform(p1, p1);

		Rectangle2D.Double result = new Rectangle2D.Double(p0.getX(), p0.getY(), 0, 0);
		result.add(p1);
		
		return result;
	}

	public AffineTransform getLocalToParentTransform() {
		return localToParentTransform.getValue();
	}
	
	public AffineTransform getParentToLocalTransform() {
		return GlobalCache.eval(parentToLocalTransform);
	}
	
	public void applyTransform(AffineTransform transform)
	{
		AffineTransform result = new AffineTransform(localToParentTransform.getValue());
		result.preConcatenate(transform);
		localToParentTransform.setValue(result);
	}

	@NoAutoSerialisation
	public double getRotation() {
		return 0;
	}
	
	@NoAutoSerialisation
	public double getScaleX() {
		return 0;
	}

	@NoAutoSerialisation
	public double getScaleY() {
		return 0;
	}

	@NoAutoSerialisation
	public void setRotation(double rotation) {
		throw new RuntimeException("not supported");
	}
	
	@NoAutoSerialisation
	public void setScaleX(double scaleX) {
		throw new RuntimeException("not supported");
	}

	@NoAutoSerialisation
	public void setScaleY(double scaleY) {
		throw new RuntimeException("not supported");
	}

	@Override
	public ModifiableExpression<AffineTransform> transform() {
		return localToParentTransform;
	}
	
	public abstract Expression<? extends Touchable> localSpaceTouchable();
	
	@Override
	public final Expression<? extends Touchable> shape() {
		return TransformHelper.transform(localSpaceTouchable(), transform());
	}
}
