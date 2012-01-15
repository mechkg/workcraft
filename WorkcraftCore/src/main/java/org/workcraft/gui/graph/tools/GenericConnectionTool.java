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
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

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
import org.workcraft.gui.graph.tools.GraphEditorTool.Button;
import org.workcraft.util.Function;
import org.workcraft.util.GUI;
import org.workcraft.util.Maybe;
import org.workcraft.util.MaybeVisitor;
import static org.workcraft.util.Maybe.Util.*;
import org.workcraft.util.Maybe.Util.NothingFound;
import org.workcraft.util.Nothing;

public class GenericConnectionTool<N>  {
	private final ModifiableExpression<Maybe<? extends N>> mouseOverObject = Variable.<Maybe<? extends N>>create(Maybe.Util.<N>nothing());
	private final ModifiableExpression<Maybe<? extends N>> first = Variable.<Maybe<? extends N>>create(Maybe.Util.<N>nothing());

	private boolean mouseExitRequiredForSelfLoop = true;
	private boolean leftFirst = false;
	private ModifiableExpression<Point2D.Double> lastMouseCoords = Variable.<Point2D.Double>create(new Point2D.Double.Double());
	private String warningMessage = null;
	private final ConnectionController<? super N> connectionManager;
	
	private final Function<? super Point2D.Double, ? extends Maybe<? extends N>> hitTester;
	private final Function<? super N, ? extends Expression<? extends Point2D.Double>> centerProvider;

	public GenericConnectionTool (Function<N, Expression<? extends Point2D.Double>> centerProvider, ConnectionController<? super N> connectionManager, Function<? super Point2D.Double, ? extends Maybe<? extends N>> hitTester) {
		this.centerProvider = centerProvider;
		this.connectionManager = connectionManager;
		this.hitTester = hitTester;
	}

	public Expression<GraphicalContent> userSpaceContent(Viewport viewport, Expression<? extends Boolean> hasFocus) {
		return connectingLineGraphicalContent(viewport);
	}

	public Expression<Maybe<? extends N>> mouseOverNode() {
		return mouseOverObject;
	}
	
	public Expression<Maybe<? extends N>> firstNode() {
		return mouseOverObject;
	}
	
	private Expression<GraphicalContent> connectingLineGraphicalContent(final Viewport viewport) {
		return new ExpressionBase<GraphicalContent>(){

			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				return new GraphicalContent(){

					@Override
					public void draw(final Graphics2D g) {
						g.setStroke(new BasicStroke((float)viewport.pixelSizeInUserSpace().getX()));

						context.resolve(first).accept(
							new MaybeVisitor<N, Nothing>(){
								@Override
								public Nothing visitNothing() {
									return Nothing.VALUE;
								}

								@Override
								public Nothing visitJust(final N first) {
									warningMessage = null;
									context.resolve(mouseOverObject).accept(
										new MaybeVisitor<N, Nothing>() {

											@Override
											public Nothing visitNothing() {
												drawConnectingLine(g, Color.BLUE, context);
												return Nothing.VALUE;
											}

											@Override
											public Nothing visitJust(N mouseOver) {
												try {
													connectionManager.validateConnection(first, mouseOver);
													drawConnectingLine(g, Color.GREEN, context);
												} catch (InvalidConnectionException e) {
													warningMessage = e.getMessage();
													drawConnectingLine(g, Color.RED, context);
												}
												return Nothing.VALUE;
											}
										}
									);
									return Nothing.VALUE;
								}
							}
						);
					}
				};
			}
			
			
		};
	}

	private void drawConnectingLine(Graphics2D g, Color color, EvaluationContext context) {
		try {
			g.setColor(color);
		
			Point2D.Double center = context.resolve(centerProvider.apply(extract(context.resolve(first))));
			
			Line2D line = new Line2D.Double(center.getX(), center.getY(), context.resolve(lastMouseCoords).getX(), context.resolve(lastMouseCoords).getY());
			g.draw(line);
		}
		catch (NothingFound e) {
			throw new RuntimeException("Should not happen!");
		}
	}

	public GraphEditorMouseListener mouseListener() {
		return new DummyMouseListener() {
			@Override
			public void mouseMoved(GraphEditorMouseEvent e) {
				lastMouseCoords.setValue(e.getPosition());
				
				Maybe<? extends N> newMouseOverObject = hitTester.apply(e.getPosition());
				
				mouseOverObject.setValue(newMouseOverObject);

				if (!leftFirst && mouseExitRequiredForSelfLoop) {
					if (eval(mouseOverObject).equals(eval(first)))
						mouseOverObject.setValue(Maybe.Util.<N>nothing());
					else
						leftFirst = true;
				}
			}
			
			@Override
			public void mousePressed(final GraphEditorMouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					eval(first).accept(
						new MaybeVisitor<N, Nothing> (){
							@Override
							public Nothing visitNothing() {
								return eval(mouseOverObject).accept(
									new MaybeVisitor<N, Nothing> () {
										@Override
										public Nothing visitNothing () {
											return Nothing.VALUE;
										}

										@Override
										public Nothing visitJust(N mouseOver) {
											assign(first, mouseOverObject);
											leftFirst = false;
											mouseMoved(e);
											return Nothing.VALUE;
										}
									}
								);
							}

							public Nothing visitJust(final N currentFirst) {
								return eval(mouseOverObject).accept(
									new MaybeVisitor<N, Nothing> () {
										@Override
										public Nothing visitNothing () {
											return Nothing.VALUE;
										}

										@Override
										public Nothing visitJust(N mouseOver) {
											try {
												connectionManager.connect(currentFirst, mouseOver);

												if ((e.getModifiers() & MouseEvent.CTRL_DOWN_MASK) != 0) {
													assign(first, mouseOverObject);
													mouseOverObject.setValue(Maybe.Util.<N>nothing());
												} else {
													first.setValue(Maybe.Util.<N>nothing());
												}
											} catch (InvalidConnectionException e1) {
												Toolkit.getDefaultToolkit().beep();
											}
											return Nothing.VALUE;
										}
									}
								);
							}
						}
					);
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					first.setValue(Maybe.Util.<N>nothing());
					mouseOverObject.setValue(Maybe.Util.<N>nothing());
				}
			}
			
		};
	}

	public Expression<GraphicalContent> screenSpaceContent(final Viewport viewport, final Expression<? extends Boolean> hasFocus) {
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
							else if (isNothing(eval(first)))
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
	
	public static final Button button =
		new Button() {
			
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
				return "Connection tool";
			}
		};
	
	
	public void deactivated() {
		first.setValue(Maybe.Util.<N>nothing());
		mouseOverObject.setValue(Maybe.Util.<N>nothing());
	}
}
