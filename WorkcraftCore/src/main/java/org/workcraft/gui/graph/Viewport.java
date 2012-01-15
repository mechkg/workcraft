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

package org.workcraft.gui.graph;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;

/**
 * The <code>Viewport</code> class represents a document viewport. It is used to map the
 * coordinates from user space (i.e. the coordinates as specified by the user) to screen space
 * (i.e. the portion of an on-screen window) and vice versa.
 * 
 * @author Ivan Poliakov
 *
 */
public class Viewport {
	/**
	 * The scaling factor per zoom level. Increasing the zoom level by 1 will effectively magnify all
	 * objects by this factor, while decreasing it by 1 will shrink all objects by the same factor.
	 */
	protected static final double SCALE_FACTOR = Math.pow(2, 0.125);

	/**
	 * The origin point in user space.
	 */
	protected static final Point2D ORIGIN = new Point2D.Double(0,0);

	/**
	 * Current horizontal view translation in user space.
	 */
	protected final ModifiableExpression<Double> tx = Variable.create(0.0);

	/**
	 * Current vertical view translation in user space.
	 */
	protected final ModifiableExpression<Double> ty = Variable.create(0.0);

	/**
	 * Current view scale factor. The default value is such that there are 16 user space units visible
	 *  across the vertical axis of the viewport.
	 */
	protected final ModifiableExpression<Double> scale = Variable.create(0.0625);

	/**
	 * The transformation from user space to screen space such that the point (0,0) in user space is
	 * mapped into the centre of the viewport, the coordinate (1) on Y axis is mapped into the topmost
	 * vertical coordinate of the viewport, the coordinate (-1) on Y axis is mapped into the bottom
	 * vertical coordinate of the viewport, and the coordinates on the X axis are mapped in such a way
	 * as to preserve the aspect ratio of the objects displayed.
	 */
	protected final Expression<AffineTransform> userToScreenTransform = new ExpressionBase<AffineTransform>(){

		@Override
		protected AffineTransform evaluate(EvaluationContext context) {
			AffineTransform result = new AffineTransform();
			Rectangle shapeValue = context.resolve(shape);
			result.translate(shapeValue.width/2 + shapeValue.x, shapeValue.height/2 + shapeValue.y);
			
			if (shapeValue.height != 0)
				result.scale(shapeValue.height/2, shapeValue.height/2);
			
			return result;
		}
		
	};

	/**
	 * The transformation of the user space that takes into account the current pan and zoom values.
	 */
	protected final Expression<AffineTransform> viewTransform = new ExpressionBase<AffineTransform>(){

		@Override
		protected AffineTransform evaluate(EvaluationContext context) {
			AffineTransform result = new AffineTransform();
			result.scale(context.resolve(scale), context.resolve(scale));
			result.translate(context.resolve(tx), context.resolve(ty));
			return result;
		}
		
	};

	/**
	 * The concatenation of the user-to-screen and pan/zoom transforms.
	 */
	Expression<AffineTransform> finalTransform = new ExpressionBase<AffineTransform>(){

		@Override
		protected AffineTransform evaluate(EvaluationContext context) {
			AffineTransform result = new AffineTransform(context.resolve(userToScreenTransform));
			result.concatenate(context.resolve(viewTransform));
			return result;
		}
	};

	/**
	 * The reverse of the final (concatenated) transform.
	 */
	Expression<AffineTransform> finalInverseTransform = new ExpressionBase<AffineTransform>(){
		@Override
		protected AffineTransform evaluate(EvaluationContext context) {
			try {
				return context.resolve(finalTransform).createInverse();
			} catch (NoninvertibleTransformException e) {
				return new AffineTransform();
			}
		}
	};

	/**
	 * The current viewport shape.
	 */
	protected ModifiableExpression<Rectangle> shape = Variable.create(new Rectangle());

	/**
	 * Initialises the user-to-screen transform according to the viewport parameters,
	 * and the view transform with the default values.
	 * @param x
	 * 	The x-coordinate of the top-left corner of the viewport (in pixels).
	 * @param y
	 * The y-coordinate of the top-left corner of the viewport (in pixels).
	 * @param w
	 * The width of the viewport (in pixels).
	 * @param h
	 * The height of the viewport (in pixels)
	 */
	public Viewport(int x, int y, int w, int h) {
		setShape (x,y,w,h);
	}

	/**
	 * Initialises the user-to-screen transform according to the viewport parameters,
	 * and the view transform with the default values.
	 * @param shape
	 * The shape of the viewport (all values in pixels).
	 */
	public Viewport(Rectangle shape) {
		this(shape.x, shape.y, shape.width, shape.height);
	}

	/**
	 * 	 @return
	 * The final transform as an AffineTransform object.
	 */
	public Expression<AffineTransform> transform() {
		return finalTransform;
	}

	/**
	 * @return
	 * The inverse of the final transform as an AffineTransform object.
	 */
	public Expression<AffineTransform> inverseTransform() {
		return finalInverseTransform;
	}

	/**
	 * Maps a point in user space into a point in screen space.
	 * @param pointInUserSpace
	 * The point in user space (in double precision)
	 * @return
	 * The corresponding point in screen space (in integer precision)
	 */
	public Point userToScreen (Point2D pointInUserSpace) {
		Point result = new Point();
		GlobalCache.eval(finalTransform).transform(pointInUserSpace, result);
		return result;
	}
	
	public Rectangle userToScreen (Rectangle2D rectInUserSpace) {
		Point ul = userToScreen (new Point2D.Double(rectInUserSpace.getMinX(), rectInUserSpace.getMinY()));
		Point lr = userToScreen (new Point2D.Double(rectInUserSpace.getMaxX(), rectInUserSpace.getMaxY()));
				
		return new Rectangle(ul.x, ul.y, lr.x-ul.x, lr.y-ul.y);
	}

	/**
	 * Maps a point in screen space into a point in user space.
	 * @param pointInScreenSpace
	 * The point in screen space (in integer precision)
	 * @return
	 * The corresponding point in user space (in double precision)
	 */
	public Point2D.Double screenToUser (Point pointInScreenSpace) {
		Point2D.Double result = new Point2D.Double();
		GlobalCache.eval(finalInverseTransform).transform(pointInScreenSpace, result);
		return result;
	}

	/**
	 * Calculates the size of one screen pixel in user space.
	 * @return
	 * X value of the returned point contains the horizontal pixel size,
	 * Y value contains the vertical pixel size.
	 * With the default user-to-screen transform these values are equal.
	 */
	public Point2D pixelSizeInUserSpace () {
		Point originInScreenSpace = userToScreen (ORIGIN);
		originInScreenSpace.x += 1;
		originInScreenSpace.y += 1;
		return screenToUser (originInScreenSpace);
	}

	/**
	 * Pans the viewport by the specified amount.
	 * @param dx
	 * The amount of horizontal panning required (in pixels)
	 * @param dy
	 * The amount of vertical panning required (in pixels)
	 */
	public void pan (int dx, int dy) {
		Point originInScreenSpace = userToScreen (ORIGIN);
		originInScreenSpace.x += dx;
		originInScreenSpace.y += dy;
		Point2D panInUserSpace = screenToUser (originInScreenSpace);

		tx.setValue(eval(tx) + panInUserSpace.getX());
		ty.setValue(eval(ty) + panInUserSpace.getY());
	}

	/**
	 * Zooms the viewport by the specified amount of levels. One positive level is the magnification
	 * by 2 to the power of 1/4. Thus, increasing the zoom by 4 levels magnifies the objects to twice
	 * their size. One negative level results in the decreasing of the objects size by the same factor.
	 * @param levels
	 * The required change of the zoom level. Use positive value to zoom in, negative value to zoom out.
	 */
	public void zoom (int levels) {
		double newScale = eval(scale) * Math.pow(SCALE_FACTOR, levels);

		if (newScale < 0.01f)
			newScale = 0.01f;
		if (newScale > 1.0f)
			newScale = 1.0f;

		scale.setValue(newScale);
	}

	/**
	 * Zooms the viewport by the specified amount of levels. One positive level is the magnification
	 * by 2 to the power of 1/4. Thus, increasing the zoom by 4 levels magnifies the objects to twice
	 * their size. One negative level results in the decreasing of the objects size by the same factor.
	 * 
	 * Anchors the viewport to the specified point, i.e. ensures that the point given in screen space
	 * does not change its coordinates in user space after zoom change is carried out, allowing to zoom
	 * "into" or "out of" the specified point.
	 * 
	 * @param levels
	 * The required change of the zoom level. Use positive value to zoom in, negative value to zoom out.
	 * @param anchor
	 * The anchor point in screen space.
	 */
	public void zoom (int levels, Point anchor) {
		Point2D anchorInUserSpace = screenToUser(anchor);
		zoom(levels);

		Point2D anchorInNewSpace = screenToUser(anchor);

		tx.setValue(eval(tx) + anchorInNewSpace.getX() - anchorInUserSpace.getX());
		ty.setValue(eval(ty) + anchorInNewSpace.getY() - anchorInUserSpace.getY());
	}

	/**
	 * Changes the shape of the viewport.
	 * @param x
	 * 	The x-coordinate of the top-left corner of the new viewport (in pixels).
	 * @param y
	 * The y-coordinate of the top-left corner of the new viewport (in pixels).
	 * @param width
	 * The width of the new viewport (in pixels).
	 * @param height
	 * The height of the new viewport (in pixels)
	 */
	public void setShape (int x, int y, int width, int height) {
		shape.setValue(new Rectangle(x, y, width, height));
	}

	/**
	 * Changes the shape of the viewport.
	 * @param shape
	 * 	The new shape of the viewport as Rectangle (in pixels).
	 */
	public void setShape (Rectangle shape) {
		setShape(shape.x, shape.y, shape.width, shape.height);
	}

	/**
	 * @return The current viewport shape.
	 */
	public Expression<Rectangle> shape() {
		return shape;
	}
}
