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
import static org.workcraft.util.Geometry.add;
import static org.workcraft.util.Geometry.changeBasis;
import static org.workcraft.util.Geometry.multiply;
import static org.workcraft.util.Geometry.normalize;
import static org.workcraft.util.Geometry.rotate90CCW;
import static org.workcraft.util.Geometry.subtract;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionImpl;
import org.workcraft.dom.visual.connections.VisualConnection.ScaleMode;
import org.workcraft.exceptions.NotImplementedException;

public class ControlPointScaler extends ExpressionBase<Map<ControlPoint, ModifiableExpression<AffineTransform>>> {
	private static double THRESHOLD = 0.00001;
	private Point2D.Double oldC1, oldC2;
	private final Expression<? extends VisualConnectionProperties> connectionInfo;
	private final Expression<? extends Collection<? extends ControlPoint>> controlPoints;

	public ControlPointScaler(Expression<? extends VisualConnectionProperties> connectionInfo, Expression<? extends Collection<? extends ControlPoint>> controlPoints) {
		this.connectionInfo = connectionInfo;
		this.controlPoints = controlPoints;
	}

	private static List<? extends Point2D.Double> scale (
			Point2D.Double oldC1, Point2D.Double oldC2, 
			Point2D.Double newC1, Point2D.Double newC2, 
			List<? extends Point2D.Double> points, VisualConnection.ScaleMode mode) {
		
		if (mode == VisualConnection.ScaleMode.NONE)
			return points;
		
		List<Point2D.Double> result = new ArrayList<Point2D.Double>();
		
		if (mode == VisualConnection.ScaleMode.LOCK_RELATIVELY)
		{
			Point2D.Double dC1 = subtract(newC1, oldC1);
			Point2D.Double dC2 = subtract(newC2, oldC2);
			
			int n = points.size();
			int i=0;
			for (Point2D.Double cp : points)
			{
				Point2D.Double delta;
				if(i<n/2)
					delta = dC1;
				else
					if(i>(n-1)/2)
						delta = dC2;
					else
						delta = multiply(add(dC1, dC2), 0.5);

				result.add(add(cp, delta));

				i++;	
			}
			return result;
		}

		Point2D.Double v0 = subtract(oldC2, oldC1);

		if (v0.distanceSq(0, 0) < THRESHOLD)
			v0.setLocation(0.001, 0);

		Point2D.Double up0 = getUpVector(mode, v0);

		Point2D.Double v = subtract(newC2, newC1);

		if (v.distanceSq(0, 0) < THRESHOLD)
			v.setLocation(0.001, 0);

		Point2D.Double up = getUpVector(mode, v);

		for (Point2D.Double cp : points) {
			Point2D.Double p = subtract(cp, oldC1);

			Point2D dp = changeBasis (p, v0, up0);

			result.add(
					add(
							add(
									multiply (v, dp.getX()),
									multiply (up, dp.getY())),
									newC1
					));
		}
		return result;
	}

	public static Point2D.Double reduce (Point2D.Double p) {
		return multiply (normalize(p), Math.pow(p.distanceSq(0, 0), 0.2));
	}
	
	private static Point2D.Double getUpVector(VisualConnection.ScaleMode mode, Point2D.Double v0) {
		switch (mode) {
		case SCALE:
			return rotate90CCW(v0);
		case STRETCH:
			return normalize(rotate90CCW(v0));
		case ADAPTIVE:
			return reduce(rotate90CCW(v0));
		default:
			throw new RuntimeException ("Unexpected value of scale mode");
		}
	}

	private void applyScale(List<? extends ControlPoint> cpoints, List<? extends Point2D> scaled) {
		/*if(cpoints.size() != scaled.size())
			throw new ArgumentException("bad arg sizes");
		for(int i=0;i<cpoints.size();i++) {
			Point2D p = scaled.get(i);
			cpoints.get(i).simpleTransform().setValue(AffineTransform.getTranslateInstance(p.getX(), p.getY()));
		}*/
	}
	
	@Override
	protected Map<ControlPoint, ModifiableExpression<AffineTransform>> evaluate(EvaluationContext context) {
		
		VisualConnectionProperties connInfo = context.resolve(connectionInfo);
		
		final List<? extends ControlPoint> cpoints = new ArrayList<ControlPoint>(context.resolve(controlPoints));
		ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
		for(ControlPoint p : cpoints) {
			AffineTransform tr = context.resolve(p.simpleTransform());
			points.add(new Point2D.Double(tr.getTranslateX(), tr.getTranslateY()));
		}
		
		final Point2D.Double newC1 = null; // TODO:
		final Point2D.Double newC2 = null;
		ScaleMode scaleMode = null;
		if(true)throw new NotImplementedException(); 

		if (oldC1==null || oldC2 == null) {
			oldC1 = newC1;
			oldC2 = newC2;
		}
		
		final List<? extends Point2D.Double> scaled = scale(oldC1, oldC2, newC1, newC2, points, scaleMode);
		
		applyScale(cpoints, scaled);
		
		//oldC1 = newC1;
		//oldC2 = newC2;
				
		Map<ControlPoint, ModifiableExpression<AffineTransform>> result = new HashMap<ControlPoint, ModifiableExpression<AffineTransform>>();
		
		for(int i=0;i<cpoints.size();i++) {
			final ControlPoint cpoint = cpoints.get(i);
			final int index = i;
			result.put(cpoint, new ModifiableExpressionImpl<AffineTransform>() {

				@Override
				protected void simpleSetValue(AffineTransform newValue) {
					AffineTransform oldValue = eval(this);
					Point2D shift = new Point2D.Double(
							newValue.getTranslateX() - oldValue.getTranslateX(), 
							newValue.getTranslateY() - oldValue.getTranslateY()); 
					
					AffineTransform tr = new AffineTransform(eval(cpoint.simpleTransform()));
					tr.concatenate(AffineTransform.getTranslateInstance(shift.getX(), shift.getY()));
					cpoint.simpleTransform().setValue(tr);
				}

				@Override
				protected AffineTransform evaluate(EvaluationContext context) {
					Point2D p = scaled.get(index);
					return AffineTransform.getTranslateInstance(p.getX(), p.getY());
				}
			});
		}
			
		return result;
	}
}