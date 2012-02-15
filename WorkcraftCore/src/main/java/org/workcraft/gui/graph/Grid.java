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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.visual.GraphicalContent;


/**
 * The <code>Grid</code> class is used to generate and draw the background grid, guidelines,
 * as well as to handle the coordinate "snapping".
 * @author Ivan Poliakov
 *
 */
public class Grid {
	protected static final double minorIntervalFactor = 0.1;
	protected static final double intervalScaleFactor = 2;

	protected static final double magThreshold = 5;
	protected static final double minThreshold = 2.5;

	static class GridInstance {
		
		public GridInstance(Path2D minorLinesPath, Path2D majorLinesPath,
				double[][] majorLinePositions, double[][] minorLinePositions,
				int[][] majorLinePositionsScreen,
				int[][] minorLinePositionsScreen, double majorInterval) {
			super();
			this.minorLinesPath = minorLinesPath;
			this.majorLinesPath = majorLinesPath;
			this.majorLinePositions = majorLinePositions;
			this.minorLinePositions = minorLinePositions;
			this.majorLinePositionsScreen = majorLinePositionsScreen;
			this.minorLinePositionsScreen = minorLinePositionsScreen;
			this.majorInterval = majorInterval;
		}

		public final Path2D minorLinesPath;
		public final Path2D majorLinesPath;
	
		public final double[][] majorLinePositions;
		public final double[][] minorLinePositions;
	
		public final int [][] majorLinePositionsScreen;
		public final int [][] minorLinePositionsScreen;

		public final double majorInterval;
	}
	
	public final Expression<GridInstance> lines;

	protected static final Stroke stroke = new BasicStroke();

	protected final Color majorLinesColor = new Color (200,200,200);
	protected final Color minorLinesColor = new Color (240,240,240);
	protected final Color guideLinesColor = Color.RED;


	/**
	 * Set the interval magnification threshold. The grid interval will be increased by <code>intervalScaleFactor</code>  if more than <code>magThreshold</code>
	 * major grid intervals become visible across the vertical dimension of the viewport.
	 * @param magThreshold
	 * The new interval magnification threshold.
	 */
	//public void setMagThreshold(double magThreshold) {

	/**
	 * Set the interval minimisation threshold. The grid interval will be decreased by <code>intervalScaleFactor</code> if less than <i>minThreshold</i>
	 * major grid intervals become visible across the vertical dimension of the viewport.
	 * major grid intervals become visible.
	 * @param minThreshold
	 */

	/**
	 * Constructs a grid with default parameters:
	 * <ul>
	 * <li> Major grid lines interval = 10 units
	 * <li> Minor grid lines frequency = 10 per major line interval
	 * <li> Dynamic scaling factor = 2
	 * <li> Dynamic magnification threshold = 5 (see <code>magThreshold</code>)
	 * <li> Dynamic minimisation threshold = 2.5 ( see <code>minThreshold</code>)
	 * </ul>
	 **/
	public Grid(Viewport viewport) {
		lines = getGridInstance(viewport);
	}

	/**
	 * Recalculates visible grid lines based on the viewport parameters.
	 * @param viewport
	 * The viewport to calculate gridlines for.
	 */
	protected static Expression<GridInstance> getGridInstance(final Viewport viewport) {
		return new ExpressionBase<GridInstance>() {

			@Override
			protected GridInstance evaluate(EvaluationContext context) {
				
				Path2D minorLinesPath = new Path2D.Double();
				Path2D majorLinesPath = new Path2D.Double();

				double[][] minorLinePositions = new double[2][];
				double[][] majorLinePositions = new double[2][];

				int[][] minorLinePositionsScreen = new int[2][];
				int[][] majorLinePositionsScreen = new int[2][];
				
				double majorInterval = 10.0;
				
				Rectangle view = context.resolve(viewport.shape());
		
				// Compute the visible user space area from the viewport
		
				Point2D visibleUL = new Point2D.Double();
				Point2D visibleLR = new Point2D.Double();
				Point viewLL = new Point (view.x, view.height+view.y);
				Point viewUR = new Point (view.width+view.x, view.y);
				context.resolve(viewport.inverseTransform()).transform(viewLL, visibleUL);
				context.resolve(viewport.inverseTransform()).transform(viewUR, visibleLR);
		
		
				// Dynamic line interval scaling
		
				double visibleHeight = visibleUL.getY() - visibleLR.getY();
				while (visibleHeight / majorInterval > magThreshold)
					majorInterval *= intervalScaleFactor;
				while (visibleHeight / majorInterval < minThreshold)
					majorInterval /= intervalScaleFactor;
		
		
				// Compute the leftmost, rightmost, topmost and bottom visible grid lines
		
				int majorBottom  = (int)Math.ceil(visibleLR.getY()/majorInterval);
				int majorTop = (int)Math.floor(visibleUL.getY()/majorInterval);
		
				int majorLeft = (int)Math.ceil(visibleUL.getX()/majorInterval);
				int majorRight = (int)Math.floor(visibleLR.getX()/majorInterval);
		
				double minorInterval = majorInterval * minorIntervalFactor;
		
				int minorLeft = (int)Math.ceil(visibleUL.getX()/minorInterval);
				int minorRight = (int)Math.floor(visibleLR.getX()/minorInterval);
		
				int minorBottom = (int)Math.ceil(visibleLR.getY()/minorInterval);
				int minorTop = (int)Math.floor(visibleUL.getY()/minorInterval);
		
		
		
				// Build the gridlines positions, store them as user-space coordinates, screen-space coordinates,
				// and as a drawable path (in screen-space)
		
				minorLinesPath = new Path2D.Double();
				majorLinesPath = new Path2D.Double();
		
				final int countMinH = Math.max(0, minorRight-minorLeft+1);
				minorLinePositions[0] = new double[countMinH];
				minorLinePositionsScreen[0] = new int[countMinH];
		
				Point2D p1 = new Point2D.Double(), p2 = new Point();
		
				for (int x=minorLeft; x<=minorRight; x++) {
					minorLinePositions[0][x-minorLeft] = x*minorInterval;
					p1.setLocation(x*minorInterval, 0);
					context.resolve(viewport.transform()).transform(p1, p2);
					minorLinePositionsScreen[0][x-minorLeft] = (int)p2.getX();
		
					minorLinesPath.moveTo(p2.getX(), viewLL.getY());
					minorLinesPath.lineTo(p2.getX(), viewUR.getY());
				}
		
				final int countMinV = Math.max(0, minorTop-minorBottom+1);
				minorLinePositions[1] = new double[countMinV];
				minorLinePositionsScreen[1] = new int[countMinV];
		
		
				for (int y=minorBottom; y<=minorTop; y++) {
					minorLinePositions[1][y-minorBottom] = y*minorInterval;
					p1.setLocation(0, y*minorInterval);
					context.resolve(viewport.transform()).transform(p1, p2);
					minorLinePositionsScreen[1][y-minorBottom] = (int)p2.getY();
		
					minorLinesPath.moveTo(viewLL.getX(), p2.getY());
					minorLinesPath.lineTo(viewUR.getX(), p2.getY());
				}
		
				final int countMajH = Math.max(0, majorRight-majorLeft+1);
				majorLinePositions[0] = new double[countMajH];
				majorLinePositionsScreen[0] = new int[countMajH];
		
				for (int x=majorLeft; x<=majorRight; x++) {
					majorLinePositions[0][x-majorLeft] = x*majorInterval;
					p1.setLocation(x*majorInterval, 0);
					context.resolve(viewport.transform()).transform(p1, p2);
		
		
					majorLinePositionsScreen[0][x-majorLeft] = (int)p2.getX();
		
		
					majorLinesPath.moveTo((int)p2.getX(), viewLL.getY());
					majorLinesPath.lineTo((int)p2.getX(), viewUR.getY());
				}
		
				final int countMajV = Math.max(0, majorTop-majorBottom+1);
				majorLinePositions[1] = new double[countMajV];
				majorLinePositionsScreen[1] = new int[countMajV];
		
				for (int y=majorBottom; y<=majorTop; y++) {
					majorLinePositions[1][y-majorBottom] = y*majorInterval;
					p1.setLocation(0, y*majorInterval);
					context.resolve(viewport.transform()).transform(p1, p2);
					majorLinePositionsScreen[1][y-majorBottom] = (int)p2.getY();
		
					majorLinesPath.moveTo(viewLL.getX(), p2.getY());
					majorLinesPath.lineTo(viewUR.getX(), p2.getY());
				}
				
				return new GridInstance(minorLinesPath, majorLinesPath, majorLinePositions, minorLinePositions, majorLinePositionsScreen, minorLinePositionsScreen, majorInterval);
			}
		};
	}

	/**
	 * Draws the grid. <i>Note that this drawing procedure assumes that the Graphics2D object will draw in screen coordinates.</i>
	 * Please restore the graphics transform object to the original state before calling this method.
	 * @param g
	 * Graphics2D object for the component the viewport is drawn onto.
	 */
	public Expression<? extends GraphicalContent> graphicalContent() {
		return new ExpressionBase<GraphicalContent>(){

			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				return new GraphicalContent() {

					@Override
					public void draw(Graphics2D g) {
						g.setStroke(stroke);
						g.setColor(minorLinesColor);
						g.draw(context.resolve(lines).minorLinesPath);
						g.setColor(majorLinesColor);
						g.draw(context.resolve(lines).majorLinesPath);
					}
					
				};
			}
			
		};
	}

	/**
	 * Returns minor grid lines positions <i>in user space, double precision</i> as a 2-dimensional array. First row of the array contains x-coordinates of the vertical grid lines,
	 * second row contains y-coordinates of the horizontal grid lines.
	 * 
	 * @return
	 * getMinorLinePositions()[0] - the array containing vertical grid lines positions
	 * getMinorLinePositions()[1] - the array containing horizontal grid lines positions
	 * 
	 */
	//public double[][] getMinorLinePositions()

	/**
	 * Returns major grid lines positions <i>in user space, double precision</i> as a 2-dimensional array. First row of the array contains x-coordinates of the vertical grid lines,
	 * second row contains y-coordinates of the horizontal grid lines.
	 * 
	 * @return
	 * getMajorLinePositions()[0] - the array containing vertical grid lines positions
	 * getMajorLinePositions()[1] - the array containing horizontal grid lines positions
	 * 
	 */
	public Expression<double[][]> majorLinePositions() {
		return new ExpressionBase<double[][]>(){

			@Override
			protected double[][] evaluate(EvaluationContext context) {
				return context.resolve(lines).majorLinePositions;
			}
			
		};
	}

	/**
	 * Returns minor grid lines positions <i>in screen space space, integer precision</i> as a 2-dimensional array.
	 * First row of the array contains x-coordinates of the vertical grid lines, second row contains y-coordinates of the horizontal grid lines,
	 * @return
	 * getMinorLinePositionsScreen()[0] - the array containing vertical grid lines positions
	 * getMinorLinePositionsScreen()[1] - the array containing horizontal grid lines positions
	 * 
	 */
	public Expression<int[][]> minorLinePositionsScreen() {
		return new ExpressionBase<int[][]>(){
			@Override
			protected int[][] evaluate(EvaluationContext context) {
				return context.resolve(lines).minorLinePositionsScreen;
			}
		};
	}


	/**
	 * Returns major grid lines positions <i>in screen space space, integer precision</i> as a 2-dimensional array. First row of the array contains Y-coordinates of the horizontal grid lines,
	 * second row contains X-coordinates of the vertical grid lines.
	 * 
	 * @return
	 * getMajorLinePositionsScreen()[0] - the array containing vertical grid lines positions
	 * getMajorLinePositionsScreen()[1] - the array containing horizontal grid lines positions
	 * 
	 */
	public Expression<int[][]> majorLinePositionsScreen() {
		return new ExpressionBase<int[][]>(){

			@Override
			protected int[][] evaluate(EvaluationContext context) {
				return context.resolve(lines).majorLinePositionsScreen;
			}
		};
	}

	/**
	 * Snap coordinate to the closest minor grid line.
	 * @param x coordinate value
	 * @return snapped coordinate value
	 */
	public double snapCoordinate(double x) {
		double m = GlobalCache.eval(lines).majorInterval*minorIntervalFactor;
		return Math.floor(x/m+0.5)*m;
	}
}