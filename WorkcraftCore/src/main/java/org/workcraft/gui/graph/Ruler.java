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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dom.visual.GraphicalContent;

/**
 * The <code>Ruler</code> is used to displays a ruler-style header for the
 * viewport.
 * 
 * @author Ivan Poliakov
 * 
 */
public class Ruler {
	private final Color background = new Color(225, 231, 242);
	private final Font font;
	private final Color foreground = new Color(0, 0, 0);
	private final int majorTickSize = 10;
	private final int minorTickSize = 3;
	private final ModifiableExpression<Rectangle> shapeVar = Variable.create(new Rectangle());
	private final int size = 15;
	private final Expression<RulerState> state;

	/**
	 * Constructs a new ruler with the default parameters:
	 * <ul>
	 * <li> Ruler size = 15 pixels;
	 * <li> Major tick size = 10 pixels;
	 * <li> Minor tick size = 3 pixels;
	 * <li> 10pt SansSerif font to display coordinates.
	 * </ul>
	 * @param grid The Grid
	 */
	public Ruler(final Grid grid) {
		font = new Font(Font.SANS_SERIF, 0, 10);
		
		state = new ExpressionBase<RulerState>(){
			@Override
			protected RulerState evaluate(EvaluationContext context) {
				return getRulerState(grid, context);
			}
		};
	}

	/**
	 * @return The current background color of the ruler area
	 */
	public Color getBackground() {
		return background;
	}

	/**
	 * @return The font that is currently used to display coordinates
	 */
	public Font getFont() {
		return font;
	}

	/**
	 * @return The current foreground color of the ruler
	 */
	public Color getForeground() {
		return foreground;
	}

	/**
	 * @return The current major tick size, in pixels
	 */
	public int getMajorTickSize() {
		return majorTickSize;
	}

	/**
	 * @return The size, in pixels, of the painted ruler area
	 */
	public int getSize() {
		return size;
	}

	class RulerState implements GraphicalContent {
		
		private final Rectangle shape;

		public RulerState(String[] horizontalMajorCaptions,
				int[] horizontalMajorTicks,
				int[] horizontalMinorTicks,
				String[] verticalMajorCaptions, int[] verticalMajorTicks,
				int[] verticalMinorTicks, Rectangle shape) {
			super();
			this.horizontalMajorCaptions = horizontalMajorCaptions;
			this.horizontalMajorTicks = horizontalMajorTicks;
			this.horizontalMinorTicks = horizontalMinorTicks;
			this.verticalMajorCaptions = verticalMajorCaptions;
			this.verticalMajorTicks = verticalMajorTicks;
			this.verticalMinorTicks = verticalMinorTicks;
			this.shape = shape;
		}
		public final String[] horizontalMajorCaptions;
		public final int[] horizontalMajorTicks;
		public final int[] horizontalMinorTicks;
		public final String[] verticalMajorCaptions;
		public final int[] verticalMajorTicks;
		public final int[] verticalMinorTicks;

		public void draw(Graphics2D g) {
			
			g.setStroke(new BasicStroke(1f));
			
			g.setBackground(background);
			g.clearRect(shape.x, shape.y, shape.width, size);
			g.clearRect(shape.x, shape.y + size, size, shape.height - size);

			g.setColor(foreground);
			g.drawLine(shape.x, size, shape.width, size);
			g.drawLine(size, shape.y, size, shape.height);



			if (minorTickSize > 0) {
				for (int t : horizontalMinorTicks)
					g.drawLine(t + shape.x, size + shape.y, t + shape.x, size + shape.y
							- minorTickSize);

				for (int t : verticalMinorTicks)
					g.drawLine(shape.x + size, t + shape.y, shape.x + size
							- minorTickSize, t + shape.y);
			}

			for (int i = 0; i < horizontalMajorTicks.length; i++) {
				int t = horizontalMajorTicks[i];
				g.drawLine(t + shape.x, size + shape.y, t + shape.x, size + shape.y
						- majorTickSize);
				g.setColor(foreground);
				g.setFont(font);
				g.drawString(horizontalMajorCaptions[i], t + shape.x + 2, size
						+ shape.y - 2);
			}

			for (int i = 0; i < verticalMajorTicks.length; i++) {
				int t = verticalMajorTicks[i];
				g.drawLine(shape.x + size, t + shape.y, shape.x + size
						- majorTickSize, t + shape.y);
				g.setColor(foreground);
				g.setFont(font);
				AffineTransform re = g.getTransform();

				g.translate(shape.x + size - 2, shape.y + t - 2);
				g.rotate(-Math.PI / 2);
				g.drawString(verticalMajorCaptions[i], 0, 0);
				g.setTransform(re);

			}
		}
	
	}

	public RulerState getRulerState(Grid grid, EvaluationContext context) {
		int[][] minorLinesScreen = context.resolve(grid.minorLinePositionsScreen());
		int[] horizontalMinorTicks = minorLinesScreen[0];
		int[] verticalMinorTicks = minorLinesScreen[1];
		int[][] majorLinesScreen = context.resolve(grid.majorLinePositionsScreen());
		int[] horizontalMajorTicks = majorLinesScreen[0];
		int[] verticalMajorTicks = majorLinesScreen[1];

		double[][] majorLines = context.resolve(grid.majorLinePositions());
		String[] horizontalMajorCaptions = new String[majorLines[0].length];
		String[] verticalMajorCaptions = new String[majorLines[1].length];

		for (int i = 0; i < majorLines[0].length; i++)
			horizontalMajorCaptions[i] = String
			.format("%.2f", majorLines[0][i]);
		for (int i = 0; i < majorLines[1].length; i++)
			verticalMajorCaptions[i] = String.format("%.2f", majorLines[1][i]);
		
		return new RulerState(horizontalMajorCaptions, horizontalMajorTicks, horizontalMinorTicks, verticalMajorCaptions, verticalMajorTicks, verticalMinorTicks, context.resolve(shapeVar));
	}

	/**
	 * Set the size, in pixels, of major ruler ticks. Set to 0 to hide the major
	 * ticks.
	 */
//	public void setMajorTickSize(int majorTickSize) {

	/**
	 * Set the size, in pixels, of major ruler ticks. Set to 0 to hide the minor
	 * ticks.
	 */
//	public void setMinorTickSize(int minorTickSize) {

	public void setShape(int x, int y, int width, int height) {
		shapeVar.setValue(new Rectangle(x, y, width, height));
	}

	public void setShape(Rectangle shape) {
		setShape(shape.x, shape.y, shape.width, shape.height);
	}
	
	public Expression<? extends GraphicalContent> graphicalContent(){
		return state;
	}

	/**
	 * Set the size, in pixels, of the painted ruler area. The sizes of vertical
	 * and horizontal rulers are the same.
	 * 
	 * @param size
	 *            The new ruler size
	 */
	//public void setSize(int size) {
}