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

package org.workcraft.plugins.petri.tools;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;
import static org.workcraft.util.Maybe.Util.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Colorisation;
import org.workcraft.gui.graph.tools.Colorisator;
import org.workcraft.gui.graph.tools.DecorationProvider;
import org.workcraft.plugins.petri.PetriNetSettings;
import org.workcraft.util.Action1;
import org.workcraft.util.Function;
import org.workcraft.util.Function2;
import org.workcraft.util.GUI;
import org.workcraft.util.Maybe;
import org.workcraft.util.MaybeVisitor;

public class SimulationTool<Event> extends AbstractTool implements DecorationProvider<Colorisator> {
	private final JPanel interfacePanel;

	private final SimControl<Event> simControl;
	private final SimStateControl simStateControl;
	
	private final Function<Node, Maybe<Event>> nodeEventExtractor;
	private final Function<Point2D, Maybe<Event>> hitTester;

	public SimulationTool(SimControl<Event> simControl, SimStateControl stateControl, Function<Point2D, Maybe<Event>> hitTester, Function<Node, Maybe<Event>> nodeEventExtractor, JPanel interfacePanel) {
		this.nodeEventExtractor = nodeEventExtractor;
		this.hitTester = hitTester;
		
		this.simControl = simControl;
		this.simStateControl = stateControl;
		
		this.interfacePanel = interfacePanel; 
	}

	protected void update() {
		// TODO: ensure everything is updated as needed
	}

	@Override
	public void deactivated() {
		simStateControl.reset();
	}

	@Override
	public void activated() {
		simStateControl.rememberInitialState();
	}

	@Override
	public void keyPressed(GraphEditorKeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET)
			simControl.unfire();
		if (e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET)
			simControl.fire(simControl.getNextEvent());
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		Maybe<Event> event = hitTester.apply(e.getPosition());
		doIfJust(event, new Action1<Event>(){
			@Override
			public void run(Event event) {
				simControl.fire(event);
			}
		});
	}

	@Override
	public Expression<? extends GraphicalContent> screenSpaceContent(final Viewport view, final Expression<Boolean> hasFocus) {
		return new ExpressionBase<GraphicalContent>() {

			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				return new GraphicalContent() {
					@Override
					public void draw(Graphics2D g) {
						if (context.resolve(hasFocus)) {

							GUI.drawEditorMessage(view, g, Color.BLACK, "Simulation: click on the highlighted transitions to fire them", context);
						}
					}
				};
			}
		};
	}

	public String getLabel() {
		return "Simulation";
	}

	public int getHotKeyCode() {
		return KeyEvent.VK_M;
	}

	@Override
	public Icon getIcon() {
		return GUI.createIconFromSVG("images/icons/svg/start-green.svg");
	}

	@Override
	public JPanel getInterfacePanel() {
		return interfacePanel;
	}

	@Override
	public Colorisator getDecoration() {
		return getColorisator();
	}
	
	Function2<Color, Color, Colorisation> mkColorisation = new Function2<Color, Color, Colorisation>(){
		@Override
		public Colorisation apply(final Color argument1, final Color argument2) {
			return new Colorisation() {
				@Override
				public Color getColorisation() {
					return argument1;
				}

				@Override
				public Color getBackground() {
					return argument2;
				}
			};
		}
	};

	Expression<Colorisation> nextTransitionColorisation = 
		fmap(
				mkColorisation,
				PetriNetSettings.enabledBackgroundColor,
				PetriNetSettings.enabledForegroundColor);

	Expression<Colorisation> enabledTransitionColorisation = 
		fmap(
				mkColorisation,
				PetriNetSettings.enabledForegroundColor,
				PetriNetSettings.enabledBackgroundColor);

	public Colorisator getColorisator() {
		return new Colorisator() { // TODO:
												// make it somehow register dependency on the enabledness
			@Override
			public Expression<? extends Colorisation> getColorisation(final Node node) {
				
				return nodeEventExtractor.apply(node).accept(new MaybeVisitor<Event, Expression<Colorisation>>() {
					@Override
					public Expression<Colorisation> visitJust(final Event event) {
						return new ExpressionBase<Colorisation>() {

							@Override
							public Colorisation evaluate(final EvaluationContext context) {
								
								Event nextEvent = simControl.getNextEvent();
								
								if (event.equals(nextEvent))
									return context.resolve(nextTransitionColorisation);
								else if(simControl.canFire(event))
									return context.resolve(enabledTransitionColorisation);
								else 
									return null;
							}
						};
					}

					@Override
					public Expression<Colorisation> visitNothing() {
						return constant(Colorisation.EMPTY);
					}
				});
			}
		};
	}

	@Override
	public Expression<? extends GraphicalContent> userSpaceContent(Viewport viewport, Expression<Boolean> hasFocus) {
		return Expressions.constant(GraphicalContent.EMPTY);
	}
}
