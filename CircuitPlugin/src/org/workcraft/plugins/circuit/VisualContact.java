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

package org.workcraft.plugins.circuit;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;

import org.apache.batik.ext.awt.geom.Polygon2D;
import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.ExpressionPropertyDeclaration;
import org.workcraft.plugins.circuit.Contact.IoType;
@DisplayName("Input/output port")
@Hotkey(KeyEvent.VK_P)
@SVGIcon("images/icons/svg/circuit-port.svg")

public class VisualContact extends VisualComponent {
	public enum Direction {	NORTH, SOUTH, EAST, WEST};
	public static final Color inputColor = Color.RED;
	public static final Color outputColor = Color.BLUE;

	private static Font nameFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);
	
	private final Variable<Direction> direction;
	
	private Shape shape=null;
	double strokeWidth = 0.05;
	
	public VisualContact(Contact contact) {
		this(contact, Direction.WEST);
	}
	
	public VisualContact(Contact contact, VisualContact.Direction dir) {
		super(contact);
		
		addPropertyDeclarations();
		
		this.direction = Variable.create(dir);
	}
	
	private Shape getShape(EvaluationContext context) {
		
		if (shape!=null) {
			return shape;
		}
		
		double size = getSize();
		if (context.resolve(parent()) instanceof VisualCircuitComponent) {
			
			shape = new Rectangle2D.Double(
				-size / 2 + strokeWidth / 2,
				-size / 2 + strokeWidth / 2,
				size - strokeWidth,
				size - strokeWidth
				);
			
		} else {
			float xx[] = {	(float) -(size / 2), 
							(float) (size / 2), 
							(float) size, 
							(float) (size / 2), 
							(float) -(size / 2)};
			float yy[] = {	(float) -(size / 2), 
							(float) -(size / 2), 0.0f, 
							(float) (size / 2), 
							(float) (size / 2)};
			
			Polygon2D poly = new Polygon2D(xx, yy, 5);
			shape = poly;
		}
		
		return shape;
	}
	
	private void addPropertyDeclarations() {
		LinkedHashMap<String, Object> types = new LinkedHashMap<String, Object>();
		types.put("Input", Contact.IoType.INPUT);
		types.put("Output", Contact.IoType.OUTPUT);
		
		LinkedHashMap<String, Object> directions = new LinkedHashMap<String, Object>();
		directions.put("North", VisualContact.Direction.NORTH);
		directions.put("East", VisualContact.Direction.EAST);
		directions.put("South", VisualContact.Direction.SOUTH);
		directions.put("West", VisualContact.Direction.WEST);
		
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Direction", direction(), direction(), VisualContact.Direction.class, directions));
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("I/O type", ioType(), ioType(), Contact.IoType.class, types));
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Name", name(), String.class));
	}

	
	@Override
	public Expression<? extends GraphicalContent> graphicalContent() {
		return new ExpressionBase<GraphicalContent>(){

			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				return new GraphicalContent() {
					
					@Override
					public void draw(DrawRequest request) {
						VisualContact.this.draw(request, context);
					}
				};
			}
		};
	}
	
	private void draw(DrawRequest r, EvaluationContext context) {

		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();
		
		if (!(context.resolve(parent()) instanceof VisualCircuitComponent)) {
			AffineTransform at = new AffineTransform();
			
			switch (context.resolve(direction())) {
			case NORTH:
				at.quadrantRotate(-1);
				break;
			case SOUTH:
				at.quadrantRotate(1);
				break;
			case EAST:
				at.quadrantRotate(2);
				break;
			}
				
			g.transform(at);
			
		}
		
		g.setColor(Coloriser.colorise(context.resolve(fillColor()), colorisation));
		g.fill(getShape(context));
		g.setColor(Coloriser.colorise(context.resolve(foregroundColor()), colorisation));
		
		g.setStroke(new BasicStroke((float)strokeWidth));
		g.draw(getShape(context));
		
		if (!(context.resolve(parent()) instanceof VisualCircuitComponent)) {
			AffineTransform at = new AffineTransform();
			
			switch (context.resolve(direction())) {
			case SOUTH:
				at.quadrantRotate(2);
				break;
			case EAST:
				at.quadrantRotate(2);
				break;
			}
			
			g.transform(at);
			
			GlyphVector gv = context.resolve(getNameGlyphs());
			Rectangle2D cur = gv.getVisualBounds();
			g.setColor(Coloriser.colorise((context.resolve(ioType())==IoType.INPUT)?inputColor:outputColor, colorisation));
			
			float xx = 0;
			
			if (context.resolve(ioType())==IoType.INPUT) {
				xx = (float)(-cur.getWidth()-0.5);
			} else {
				xx = (float)0.5;
			}
			g.drawGlyphVector(gv, xx, -0.5f);
			
		}
	}

	@Override
	public Expression<? extends Touchable> localSpaceTouchable() {
		return new ExpressionBase<Touchable>(){

			@Override
			protected Touchable evaluate(final EvaluationContext context) {
				return new Touchable(){

					@Override
					public boolean hitTest(Point2D point) {
						
						if (!(context.resolve(parent()) instanceof VisualCircuitComponent)) {
							AffineTransform at = new AffineTransform();
							
							switch (context.resolve(direction())) {
							case NORTH:
								at.quadrantRotate(1);
								break;
							case SOUTH:
								at.quadrantRotate(-1);
								break;
							case EAST:
								at.quadrantRotate(2);
								break;
							}
							
							point = at.transform(point, null);
						}
						
						return getShape(context).contains(point);
					}

					@Override
					public Rectangle2D getBoundingBox() {
						double size = getSize();
						return new Rectangle2D.Double(-size/2, -size/2, size, size);
					}

					@Override
					public Point2D getCenter() {
						return new Point2D.Double(0,0);
					}
					
				};
			}
			
		};
	}

	private double getSize() {
		return 0.5;
	}

	/////////////////////////////////////////////////////////
	public Expression<GlyphVector> getNameGlyphs() {
		return new ExpressionBase<GlyphVector>(){

			@Override
			protected GlyphVector evaluate(EvaluationContext context) {
				Direction direction = context.resolve(direction());
				if (direction==VisualContact.Direction.NORTH||direction==VisualContact.Direction.SOUTH) {
					AffineTransform at = new AffineTransform();
					at.quadrantRotate(1);
				}
				return nameFont.createGlyphVector(VisualComponent.podgonFontRenderContext(), context.resolve(name()));
			}
		};
	}
	
	public ModifiableExpression<VisualContact.Direction> direction() {
		return direction;
	}
	
	public ModifiableExpression<Contact.IoType> ioType() {
		return getReferencedContact().ioType();
	}

	
	public ModifiableExpression<String> name() {
		return getReferencedContact().name();
	}

	public Contact getReferencedContact() {
		return (Contact)getReferencedComponent();
	}

	public static boolean isDriver(Node contact) {
		if (!(contact instanceof VisualContact)) return false;
		
		return (eval(((VisualContact)contact).ioType()) == IoType.OUTPUT) == (eval(((VisualContact)contact).parent()) instanceof VisualComponent);
	}

	public static Direction flipDirection(Direction direction) {
		if (direction==Direction.EAST) return Direction.WEST;
		if (direction==Direction.WEST) return Direction.EAST;
		if (direction==Direction.SOUTH) return Direction.NORTH;
		if (direction==Direction.NORTH) return Direction.SOUTH;
		return null;
	}
	
}
