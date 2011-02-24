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
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionImpl;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Node;
import org.workcraft.dom.NodeContext;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.ReflectiveTouchable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.ExpressionPropertyDeclaration;
import org.workcraft.plugins.circuit.Contact.IoType;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.SignalTransition;

public class VisualContact extends VisualComponent implements ReflectiveTouchable {
	public enum Direction {	NORTH, SOUTH, EAST, WEST};
	public static final Color inputColor = Color.RED;
	public static final Color outputColor = Color.BLUE;

	private static Font nameFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);
	
	private double size = 0.5;
	private final ModifiableExpression<Direction> direction;
	
	
	private HashSet<SignalTransition> referencedTransitions=new HashSet<SignalTransition>();
	private Place referencedZeroPlace=null;
	private Place referencedOnePlace=null;
	public VisualContact(Contact contact, StorageManager storage) {
		this(contact, Direction.WEST, storage);
	}
	
	static public AffineTransform getDirectionTransform(Direction dir) {
		AffineTransform at = new AffineTransform();
		at.setToIdentity();
		if (dir!=null) {
			switch (dir) {
			case NORTH:
				at.quadrantRotate(3);
				break;
			case SOUTH:
				at.quadrantRotate(1);
				break;
			case WEST:
				at.quadrantRotate(2);
				break;
			}
		}
		return at;
	}
	
	public VisualContact(Contact contact, VisualContact.Direction dir, StorageManager storage) {
		super(contact, storage);
		
		this.direction = storage.create(dir);
		nameGlyphs = createGlyphsExpression(direction, contact.name());

		addPropertyDeclarations();
	}

	public void transformChanged(AffineTransform at) {
		Node parent = eval(parent());
		if(parent instanceof VisualCircuitComponent) {
			
			double x = at.getTranslateX();
			double y = at.getTranslateY();
			
			Rectangle2D r = eval(((VisualCircuitComponent)parent).createContactLabelBbExpression());
			if (r==null) r = new Rectangle2D.Double(-0.5, -0.5, 1, 1);
			
			VisualContact.Direction dir = eval(direction());
			VisualContact.Direction newDir = dir;
			
			if (x<r.getMinX()&&y>r.getMinY()&&y<r.getMaxY()) newDir = Direction.WEST;
			if (x>r.getMaxX()&&y>r.getMinY()&&y<r.getMaxY()) newDir = Direction.EAST;
			if (y<r.getMinY()&&x>r.getMinX()&&x<r.getMaxX()) newDir = Direction.NORTH;
			if (y>r.getMaxY()&&x>r.getMinX()&&x<r.getMaxX()) newDir = Direction.SOUTH;
			
			if (dir!=newDir) {
	 			direction().setValue(newDir);
			}
		}
	}
	
	private int connections = 0;
	private final Expression<GlyphVector> nameGlyphs;
	@Override
	public ModifiableExpression<AffineTransform> transform() {
		
		final ModifiableExpression<AffineTransform> superTransform = super.transform();
		return new ModifiableExpressionImpl<AffineTransform>(){

			@Override
			protected void simpleSetValue(AffineTransform newValue) {
				superTransform.setValue(newValue);
				transformChanged(newValue);
			}

			@Override
			protected AffineTransform evaluate(EvaluationContext context) {
				AffineTransform result = context.resolve(superTransform);
				Node parent = context.resolve(parent());
				if(parent instanceof ContactPositioner) {
					Point2D position = ((ContactPositioner)parent).position(VisualContact.this, new Point2D.Double(result.getTranslateX(), result.getTranslateY()), context);
					return AffineTransform.getTranslateInstance(position.getX(), position.getY()); 
				}
				else
					return result;
			}
		};
	}
	
	private Shape getShape(EvaluationContext context) {

		double size = getSize();
		if (context.resolve(parent()) instanceof VisualCircuitComponent) {
			if (CircuitSettings.getShowContacts()) {
				return new Rectangle2D.Double(
						-size / 2 + CircuitSettings.getCircuitWireWidth(),
						-size / 2 + CircuitSettings.getCircuitWireWidth(),
						size - CircuitSettings.getCircuitWireWidth()*2,
						size - CircuitSettings.getCircuitWireWidth()*2
						);
			} else {
				if (connections!=1) {
					return VisualJoint.shape;
				}
				
				return new Line2D.Double(0,0,0,0);
			}
		} else {
			
			Path2D path = new Path2D.Double();
			
			path.moveTo(-(size / 2), -(size / 2));
			path.lineTo((size / 2), -(size / 2));
			path.lineTo(size, 0);
			path.lineTo((size / 2), (size / 2));
			path.lineTo(-(size / 2), (size / 2));
			path.closePath();
			
			return path;
		}
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
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Init to one", initOne(), Boolean.class));
	}
	
	public ModifiableExpression<Boolean> initOne() {
		return getReferencedContact().initOne();
	}
	
	public static Expression<? extends ColorisableGraphicalContent> createGraphicalContent(final Expression<? extends NodeContext> nodeContext, final VisualContact contact) {
		return new ExpressionBase<ColorisableGraphicalContent>(){

			@Override
			protected ColorisableGraphicalContent evaluate(final EvaluationContext context) {

				return new ColorisableGraphicalContent() {

					@Override
					public void draw(DrawRequest request) {

						int connections = context.resolve(nodeContext).getConnections(contact).size();
						
						Graphics2D g = request.getGraphics();
						Color colorisation = request.getColorisation().getColorisation();
						Color fillColor = request.getColorisation().getBackground();
						if (fillColor==null) fillColor=context.resolve(contact.fillColor());
						
						if (!(context.resolve(contact.parent()) instanceof VisualCircuitComponent)) {
							
							AffineTransform at = new AffineTransform();
							switch (context.resolve(contact.direction())) {
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
						
						Shape shape = contact.getShape(context);
						if (connections>1&&(context.resolve(contact.parent()) instanceof VisualCircuitComponent)&&!CircuitSettings.getShowContacts()) {
							g.setColor(Coloriser.colorise(context.resolve(contact.foregroundColor()), colorisation));
							g.fill(shape);
							
						} else {
							if (!(shape instanceof Line2D)) {
								g.setStroke(new BasicStroke((float)CircuitSettings.getCircuitWireWidth()));
								g.setColor(fillColor);
								g.fill(shape);
								g.setColor(Coloriser.colorise(context.resolve(contact.foregroundColor()), colorisation));
								g.draw(shape);
							}
						}
						
						if (!(context.resolve(contact.parent()) instanceof VisualCircuitComponent)) {
							AffineTransform at = new AffineTransform();
							
							switch (context.resolve(contact.direction())) {
							case SOUTH:
								at.quadrantRotate(2);
								break;
							case EAST:
								at.quadrantRotate(2);
								break;
							}
							
							g.transform(at);
							
							GlyphVector gv = context.resolve(contact.nameGlyphs());
							Rectangle2D cur = gv.getVisualBounds();
							g.setColor(Coloriser.colorise((context.resolve(contact.ioType())==IoType.INPUT)?inputColor:outputColor, colorisation));
							
							float xx = 0;
							
							if (context.resolve(contact.ioType())==IoType.INPUT) {
								xx = (float)(-cur.getWidth()-0.5);
							} else {
								xx = (float)0.5;
							}
							g.drawGlyphVector(gv, xx, -0.5f);
							
						}

					}
				};
			}

		};
	}		

	@Override
	public Expression<? extends Touchable> shape() {
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
						return getShape(context).getBounds2D();
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
		return size;
	}

	/////////////////////////////////////////////////////////
	public Expression<GlyphVector> nameGlyphs() {
		return nameGlyphs;
	}

	private static Expression<GlyphVector> createGlyphsExpression(final Expression<Direction> direction, final Expression<String> name) {
		return new ExpressionBase<GlyphVector>(){

			@Override
			protected GlyphVector evaluate(EvaluationContext context) {
				Direction dir = context.resolve(direction);
				if (dir==VisualContact.Direction.NORTH||dir==VisualContact.Direction.SOUTH) {
					AffineTransform at = new AffineTransform();
					at.quadrantRotate(1);
				}
				return nameFont.createGlyphVector(VisualComponent.podgonFontRenderContext(), context.resolve(name));
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

	public HashSet<SignalTransition> getReferencedTransitions() {
		return referencedTransitions;
	}

	public void setReferencedOnePlace(Place referencedOnePlace) {
		this.referencedOnePlace = referencedOnePlace;
	}

	public Place getReferencedOnePlace() {
		return referencedOnePlace;
	}

	public void setReferencedZeroPlace(Place referencedZeroPlace) {
		this.referencedZeroPlace = referencedZeroPlace;
	}

	public Place getReferencedZeroPlace() {
		return referencedZeroPlace;
	}
	
}
