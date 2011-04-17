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

package org.workcraft.gui.graph.tools;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.util.Function;
import org.workcraft.util.GUI;

public class ConnectionTool<N> extends AbstractTool {
	private final ModifiableExpression<N> mouseOverObject = Variable.create(null);
	private final ModifiableExpression<N> first = Variable.create(null);

	private boolean mouseExitRequiredForSelfLoop = true;
	private boolean leftFirst = false;
	private ModifiableExpression<Point2D> lastMouseCoords = Variable.<Point2D>create(new Point2D.Double());
	private String warningMessage = null;
	private final ConnectionController<? super N> connectionManager;
	
	private final Function<? super Point2D, ? extends N> hitTester;
	private final Function<? super N, ? extends Expression<? extends Point2D>> centerProvider;

	public ConnectionTool (Function<? super N, ? extends Expression<? extends Point2D>> centerProvider, ConnectionController<? super N> connectionManager, Function<? super Point2D, ? extends N> hitTester) {
		this.centerProvider = centerProvider;
		this.connectionManager = connectionManager;
		this.hitTester = hitTester;
	}

	public Ellipse2D getBoundingCircle(Rectangle2D boundingRect) {

		double w_2 = boundingRect.getWidth()/2;
		double h_2 = boundingRect.getHeight()/2;
		double r = Math.sqrt(w_2 * w_2 + h_2 * h_2);

		return new Ellipse2D.Double(boundingRect.getCenterX() - r, boundingRect.getCenterY() - r, r*2, r*2);
	}

	@Override
	public Expression<? extends GraphicalContent> userSpaceContent(Viewport viewport, Expression<Boolean> hasFocus) {
		return connectingLineGraphicalContent(viewport);
	}

	public Expression<N> mouseOverNode() {
		return mouseOverObject;
	}
	
	public Expression<N> firstNode() {
		return mouseOverObject;
	}
	
	private Expression<? extends GraphicalContent> connectingLineGraphicalContent(final Viewport viewport) {
		return new ExpressionBase<GraphicalContent>(){

			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				return new GraphicalContent(){

					@Override
					public void draw(Graphics2D g) {
						g.setStroke(new BasicStroke((float)viewport.pixelSizeInUserSpace().getX()));

						if (context.resolve(first) != null) {
							warningMessage = null;
							if (context.resolve(mouseOverObject) != null) {
								try {
									connectionManager.validateConnection(context.resolve(first), context.resolve(mouseOverObject));
									drawConnectingLine(g, Color.GREEN, context);
								} catch (InvalidConnectionException e) {
									warningMessage = e.getMessage();
									drawConnectingLine(g, Color.RED, context);
								}
							} else {
								drawConnectingLine(g, Color.BLUE, context);
							}
						}
					}
				};
			}
			
		};
	}

	private void drawConnectingLine(Graphics2D g, Color color, EvaluationContext context) {
		g.setColor(color);
		
		Point2D center = context.resolve(centerProvider.apply(context.resolve(first)));
		
		Line2D line = new Line2D.Double(center.getX(), center.getY(), context.resolve(lastMouseCoords).getX(), context.resolve(lastMouseCoords).getY());
		g.draw(line);
	}

	@Override
	public GraphEditorMouseListener mouseListener() {
		return new DummyMouseListener() {
			@Override
			public void mouseMoved(GraphEditorMouseEvent e) {
				lastMouseCoords.setValue(e.getPosition());
				
				N newMouseOverObject = hitTester.apply(e.getPosition());
				
				mouseOverObject.setValue(newMouseOverObject);

				if (!leftFirst && mouseExitRequiredForSelfLoop) {
					if (eval(mouseOverObject) == eval(first))
						mouseOverObject.setValue(null);
					else
						leftFirst = true;
				}
			}
			
			@Override
			public void mousePressed(GraphEditorMouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (eval(first) == null) {
						if (eval(mouseOverObject) != null) { 
							assign(first, mouseOverObject);
							leftFirst = false;
							mouseMoved(e);
						}
					} else if (eval(mouseOverObject) != null) {
						try {
							connectionManager.connect(eval(first), eval(mouseOverObject));

							if ((e.getModifiers() & MouseEvent.CTRL_DOWN_MASK) != 0) {
								assign(first, mouseOverObject);
								mouseOverObject.setValue(null);
							} else {
								first.setValue(null);
							}
						} catch (InvalidConnectionException e1) {
							Toolkit.getDefaultToolkit().beep();
						}

					}
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					first.setValue(null);
					mouseOverObject.setValue(null);
				}
			}
			
		};
	}


	
	@Override
	public Expression<? extends GraphicalContent> screenSpaceContent(final Viewport viewport, final Expression<Boolean> hasFocus) {
		return new ExpressionBase<GraphicalContent>(){
			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				return new GraphicalContent(){

					@Override
					public void draw(Graphics2D g) {
						String message;
						
						if (context.resolve(hasFocus)) {
							if (warningMessage != null)
								message = warningMessage;
							else if (eval(first) == null)
								message = "Click on the first component";
							else
								message = "Click on the second component (control+click to connect continuously)";

							GUI.drawEditorMessage(viewport, g, warningMessage != null ? Color.RED : Color.BLACK, message, context);
						}
					}
				};
			}
		};
	}
	
	@Override
	public Button getButton() {
		return new Button() {
			
			@Override
			public int getHotKeyCode() {
				return KeyEvent.VK_C;		
			}

			@Override
			public Icon getIcon() {
				return GUI.createIconFromSVG("images/icons/svg/connect.svg");
			}

			@Override
			public String getLabel() {
				return "Connect";
			}
		};
	}

	@Override
	public void deactivated() {
		first.setValue(null);
		mouseOverObject.setValue(null);
	}
}
