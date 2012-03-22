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

package org.workcraft.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.connections.VisualConnectionProperties;

public class Geometry {

	public static Point2D.Double lerp(Point2D.Double p1, Point2D.Double p2, double t) {
		return new Point2D.Double(p1.getX()*(1-t)+p2.getX()*t, p1.getY()*(1-t)+p2.getY()*t);
	}
	
	public static Point2D.Double add (Point2D.Double p1, Point2D.Double p2) {
		Point2D.Double result = (Point2D.Double)p1.clone();
		result.setLocation(result.getX() + p2.getX(), result.getY() + p2.getY());
		return result;
	}

	public static Point2D.Double subtract (Point2D.Double p1, Point2D.Double p2) {
		Point2D.Double result = (Point2D.Double)p1.clone();
		result.setLocation(result.getX() - p2.getX(), result.getY() - p2.getY());
		return result;
	}
	
	public static Point2D.Double rotate90CCW (Point2D.Double p) {
		Point2D.Double result = (Point2D.Double)p.clone();
		result.setLocation(-p.getY(), p.getX());
		return result;
	}
	
	public static Point2D.Double normalize (Point2D.Double p) {
		Point2D.Double result = (Point2D.Double)p.clone();
		double length = p.distance(0, 0);
		if (length < 0.0000001)
			result.setLocation(0, 0);
		else
			result.setLocation(p.getX() / length, p.getY() / length);
		return result;
	}
	
	public static Rectangle2D.Double createRectangle (Point2D p1, Point2D p2) {
		Rectangle2D.Double rect = new Rectangle2D.Double(p1.getX(), p1.getY(), 0, 0);
		rect.add(p2);
		return rect; 
	}
	
	public static double dotProduct (Point2D v1, Point2D v2) {
		return v1.getX() * v2.getX() + v1.getY() * v2.getY();
	}
	
	public static Point2D.Double multiply (Point2D.Double p, double a) {
		Point2D.Double result = (Point2D.Double)p.clone();
		result.setLocation(p.getX() * a, p.getY() * a);
		return result;
	}
	
	public static class CurveSplitResult
	{
		public final CubicCurve2D.Double curve1;
		public final CubicCurve2D.Double curve2;

		public CurveSplitResult(CubicCurve2D.Double curve1, CubicCurve2D.Double curve2)
		{
			this.curve1 = curve1;
			this.curve2 = curve2;
		}
	}
	
	public static CubicCurve2D.Double buildCurve(Point2D.Double p1, Point2D.Double cp1, Point2D.Double cp2, Point2D.Double p2) {
		return new CubicCurve2D.Double(p1.getX(), p1.getY(), cp1.getX(), cp1.getY(), cp2.getX(), cp2.getY(),
				p2.getX(), p2.getY()
		);
	}

	public static CurveSplitResult splitCubicCurve(CubicCurve2D.Double curve, double t) {
		Point2D.Double a1 = lerp((Point2D.Double)curve.getP1(), (Point2D.Double)curve.getCtrlP1(), t);
		Point2D.Double a2 = lerp((Point2D.Double)curve.getCtrlP1(), (Point2D.Double)curve.getCtrlP2(), t);
		Point2D.Double a3 = lerp((Point2D.Double)curve.getCtrlP2(), (Point2D.Double)curve.getP2(), t);

		Point2D.Double b1 = lerp(a1, a2, t);
		Point2D.Double b2 = lerp(a2, a3, t);

		Point2D.Double c = lerp(b1, b2, t);

		return new CurveSplitResult(buildCurve((Point2D.Double)curve.getP1(), a1, b1, c), buildCurve(c, b2, a3, (Point2D.Double)curve.getP2()));
	}
	
	public static Point2D.Double getPointOnCubicCurve (CubicCurve2D.Double curve, double t) {
		Point2D.Double a1 = lerp((Point2D.Double)curve.getP1(), (Point2D.Double)curve.getCtrlP1(), t);
		Point2D.Double a2 = lerp((Point2D.Double)curve.getCtrlP1(), (Point2D.Double)curve.getCtrlP2(), t);
		Point2D.Double a3 = lerp((Point2D.Double)curve.getCtrlP2(), (Point2D.Double)curve.getP2(), t);

		Point2D.Double b1 = lerp(a1, a2, t);
		Point2D.Double b2 = lerp(a2, a3, t);

		return lerp(b1, b2, t);
	}
		
	public static Point2D.Double getDerivativeOfCubicCurve (CubicCurve2D.Double curve, double t) {
		
		Point2D.Double a1 = subtract((Point2D.Double)curve.getCtrlP1(), (Point2D.Double)curve.getP1());
		Point2D.Double a2 = subtract((Point2D.Double)curve.getCtrlP2(), (Point2D.Double)curve.getCtrlP1());
		Point2D.Double a3 = subtract((Point2D.Double)curve.getP2(), (Point2D.Double)curve.getCtrlP2());

		Point2D.Double b1 = lerp(a1, a2, t);
		Point2D.Double b2 = lerp(a2, a3, t);

		return multiply(lerp(b1, b2, t), 3.0);
	}
	
	public static Point2D.Double getSecondDerivativeOfCubicCurve (CubicCurve2D.Double curve, double t)
	{		
		Point2D.Double a1 = subtract((Point2D.Double)curve.getCtrlP1(), (Point2D.Double)curve.getP1());
		Point2D.Double a2 = subtract((Point2D.Double)curve.getCtrlP2(), (Point2D.Double)curve.getCtrlP1());
		Point2D.Double a3 = subtract((Point2D.Double)curve.getP2(), (Point2D.Double)curve.getCtrlP2());

		Point2D.Double b1 = subtract(a2, a1);
		Point2D.Double b2 = subtract(a3, a2);

		return multiply(lerp(b1, b2, t), 9.0);
	}
	
	public static AffineTransform optimisticInverse(AffineTransform transform)
	{
		try
		{
			return transform.createInverse();
		}
		catch(NoninvertibleTransformException ex)
		{
			throw new RuntimeException("Matrix inverse failed! Pessimists win :( ");
		}
	}


	/**
	 * Interpretes points as complex numbers and multiplies them.
	 * Can be used for the scale-with-rotate (translates 'a' from the basis of (b, rotate90CCW(b)) to the basis of ((1, 0), (0, 1))) 
	 */
	public static Point2D.Double complexMultiply(Point2D.Double a, Point2D.Double b) {
		return new Point2D.Double(a.getX() * b.getX() - a.getY() * b.getY(), a.getX() * b.getY() + a.getY() * b.getX());
	}
	
	public static Maybe<Point2D.Double> complexInverse(Point2D.Double a) {
		double sq = a.distanceSq(0, 0);
		if(sq < 0.0000001)
			return Maybe.Util.nothing();
		else
			return Maybe.Util.<Point2D.Double>just(new Point2D.Double(a.getX() / sq, -a.getY() / sq));
	}
	
	public static Point2D changeBasis (Point2D p, Point2D vx, Point2D vy) {
		Point2D result = (Point2D)p.clone();
		
		if (dotProduct(vx,vy) > 0.0000001)
			throw new RuntimeException ("Vectors vx and vy must be orthogonal");
		
		double vysq = vy.distanceSq(0,0);
		double vxsq = vx.distanceSq(0,0);
		
		if (vysq < 0.0000001 || vxsq < 0.0000001)
			throw new RuntimeException ("Vectors vx and vy must not have zero length");

		result.setLocation(dotProduct(p, vx) / vxsq, dotProduct(p, vy) / vysq);
		
		return result;
	}

	public static double crossProduct(Point2D p, Point2D q)
	{
		double x1 = p.getX();
		double y1 = p.getY();
		
		double x2 = q.getX();
		double y2 = q.getY();
		
		return x1 * y2 - y1 * x2;
	}

	public static TwoWayFunction<Point2D.Double, Point2D.Double> addFunc(final Point2D.Double shift) {
		return new TwoWayFunction<Point2D.Double, Point2D.Double>() {
			
			@Override
			public Point2D.Double reverse(Point2D.Double b) {
				return subtract(b, shift);
			}
			
			@Override
			public Point2D.Double apply(Point2D.Double a) {
				return add(a, shift);
			}
		};
	}
}
