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
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.CustomTouchable;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.ExpressionPropertyDeclaration;
import org.workcraft.observation.StateEvent;
import org.workcraft.plugins.circuit.Contact.IoType;
import org.workcraft.plugins.circuit.VisualContact.Direction;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.Hierarchy;

@DisplayName("Abstract Component")
@Hotkey(KeyEvent.VK_A)
@SVGIcon("images/icons/svg/circuit-component.svg")

public class VisualCircuitComponent extends VisualComponent implements Container, CustomTouchable {
	
	private Color inputColor = VisualContact.inputColor;
	private Color outputColor = VisualContact.outputColor;
	
	double marginSize = 0.2;
	double contactLength = 1;
	double contactStep = 1;
	
	DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);

	private final Expression<Rectangle2D> contactLabelBB = createContactLabelBbExpression();
	protected final Expression<Rectangle2D> totalBB = createTotalBbExpression();
	
	public VisualCircuitComponent(CircuitComponent component) {
		super(component);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Name", name(), String.class));
	}
	
	// updates sequential position of the contacts
	private void updateStepPosition() {
		int north=0;
		int south=0;
		int east=0;
		int west=0;
		for (Node n: eval(children())) {
			if (n instanceof VisualContact) {
				VisualContact vc = (VisualContact)n;
				if (eval(vc.direction()).equals(Direction.EAST)) east++;
				if (eval(vc.direction()).equals(Direction.SOUTH)) south++;
				if (eval(vc.direction()).equals(Direction.NORTH)) north++;
				if (eval(vc.direction()).equals(Direction.WEST)) west++;
			}
		}
		
		double eastStep=-contactStep*(east-1)/2;
		double westStep=-contactStep*(west-1)/2;
		double northStep=-contactStep*(north-1)/2;
		double southStep=-contactStep*(south-1)/2;
		
		for (Node n: eval(children())) {
			if (!(n instanceof VisualContact)) continue;
			VisualContact vc=(VisualContact)n;
			switch (eval(vc.direction())) {
			case EAST: 
				vc.y().setValue(eastStep);
				eastStep+=contactStep;
				break;
			case WEST: 
				vc.y().setValue(westStep);
				westStep+=contactStep;
				break;
			case SOUTH:
				vc.x().setValue(southStep);
				southStep+=contactStep;
				break;
			case NORTH:
				vc.x().setValue(northStep);
				northStep+=contactStep;
				break;
			}
		}
	}
	
	private void updateSidePosition(Rectangle2D labelBB, VisualContact contact) {
		
		double side_pos_w = (double)(Math.round((labelBB.getMinX()-contactLength)*2))/2;
		double side_pos_e = (double)(Math.round((labelBB.getMaxX()+contactLength)*2))/2;
		double side_pos_s = (double)(Math.round((labelBB.getMaxY()+contactLength)*2))/2;
		double side_pos_n = (double)(Math.round((labelBB.getMinY()-contactLength)*2))/2;
		
		for (Node vn: eval(groupImpl.children())) {
			if (vn instanceof VisualContact) {
				VisualContact vc = (VisualContact)vn;
				switch (eval(vc.direction())) {
				case EAST:
					vc.x().setValue(side_pos_e);
					break;
				case WEST:
					vc.x().setValue(side_pos_w);
					break;
				case NORTH:
					vc.y().setValue(side_pos_n);
					break;
				case SOUTH:
					vc.y().setValue(side_pos_s);
					break;
				}
			}
		}
	}
	
	public void addContact(VisualContact vc) {
		if (!eval(children()).contains(vc)) {
			((CircuitComponent)this.getReferencedComponent()).add(vc.getReferencedComponent());
			add(vc);
			updateStepPosition();
		}
	}
	
	protected Expression<Rectangle2D> createTotalBbExpression() {

		return new ExpressionBase<Rectangle2D>(){
			@Override
			protected Rectangle2D evaluate(EvaluationContext context) {
				Rectangle2D result = BoundingBoxHelper.mergeBoundingBoxes(Hierarchy.getChildrenOfType(VisualCircuitComponent.this, Touchable.class));
				result = BoundingBoxHelper.union(result, context.resolve(contactLabelBB));
				return result;
			}
		};
	}
	
	public Expression<Rectangle2D> createContactLabelBbExpression() {
		return new ExpressionBase<Rectangle2D>(){

			@Override
			protected Rectangle2D evaluate(EvaluationContext context) {
		
				int north=0;
				int south=0;
				int east=0;
				int west=0;
				for (VisualContact vc : getContacts(context)) {
					if (context.resolve(vc.direction()).equals(Direction.EAST)) east++;
					if (context.resolve(vc.direction()).equals(Direction.SOUTH)) south++;
					if (context.resolve(vc.direction()).equals(Direction.NORTH)) north++;
					if (context.resolve(vc.direction()).equals(Direction.WEST)) west++;
				}
				
				Rectangle2D cur;
				double xx;
				double width_w=0;
				double width_e=0;
				double width_n=0;
				double width_s=0;
				
				for (VisualContact c: getContacts(context)) {
					GlyphVector gv = context.resolve(c.getNameGlyphs());
					cur = gv.getVisualBounds();
					xx = cur.getWidth();
					xx = (double)(Math.round(xx*4))/4;
					
					
					switch (context.resolve(c.direction())) {
						case WEST:
							width_w=(xx>width_w)?xx:width_w;
							break;
						case EAST:
							width_e=(xx>width_e)?xx:width_e;
							break;
						case NORTH:
							width_n=(xx>width_n)?xx:width_n;
							break;
						case SOUTH:
							width_s=(xx>width_s)?xx:width_s;
							break;
					}
				}
				
				double height = Math.max(east, west)*contactStep+width_n+width_s+marginSize*4;
				double width = Math.max(north, south)*contactStep+width_e+width_w+marginSize*4;
			
				Rectangle2D result = new Rectangle2D.Double(-width/2, -height/2, width, height);
				//updateSidePosition(result, null); // dangerous
			
				return result;
			}
		};
	}
	
	protected void drawContactConnections(DrawRequest r, EvaluationContext context) {
		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();
		g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
		
		Rectangle2D BB = context.resolve(contactLabelBB);
		
		for (Node n: eval(children())) {
			if (n instanceof VisualContact) {
				VisualContact vc=(VisualContact)n;
				
				Line2D line;
				switch(context.resolve(vc.direction())){
				case EAST: 
					line = new Line2D.Double(eval(vc.x()), eval(vc.y()), BB.getMaxX(), eval(vc.y()));
					break;
				case WEST:
					line = new Line2D.Double(eval(vc.x()), eval(vc.y()), BB.getMinX(), eval(vc.y()));
					break;
				case NORTH:
					line = new Line2D.Double(eval(vc.x()), eval(vc.y()), eval(vc.x()), BB.getMinY());
					break;
				case SOUTH:
					line = new Line2D.Double(eval(vc.x()), eval(vc.y()), eval(vc.x()), BB.getMaxY());
					break;
				default:
					throw new NotSupportedException();
				}
				
				g.setColor(Coloriser.colorise(CommonVisualSettings.getForegroundColor(), colorisation));
				g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
				g.draw(line);
			}
		}
		
	}
	

	private Collection<VisualContact> getContacts(EvaluationContext context) {
		ArrayList<VisualContact> result = new ArrayList<VisualContact>();
		Collection<Node> children = context.resolve(children());
		System.out.println("children: " + children.size());
		for (Node n: children) {
			if(n instanceof VisualContact)
				result.add((VisualContact)n);
		}
		System.out.println("contacts: " + result.size());
		return result;
	}
	
	protected void drawContacts(DrawRequest r, EvaluationContext context) {
		
		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();
		
		Rectangle2D BB = context.resolve(contactLabelBB);
	
		
		for (VisualContact c: getContacts(context)) {
			GlyphVector gv = context.resolve(c.getNameGlyphs());
			Rectangle2D cur = gv.getVisualBounds();
			g.setColor(Coloriser.colorise((context.resolve(c.ioType())==IoType.INPUT)?inputColor:outputColor, colorisation));
			
			double step_pos, x, y;
			AffineTransform transform = new AffineTransform();
			AffineTransform original = g.getTransform();
			
			switch (context.resolve(c.direction())) {
			case WEST:
				step_pos = eval(c.y());
				x = BB.getMinX()+marginSize;
				y = step_pos+(cur.getHeight())/2;
				break;				
			case EAST:
				step_pos = eval(c.y());
				x = BB.getMaxX()-marginSize-cur.getWidth();
				y = step_pos+(cur.getHeight())/2;
				break;
			case NORTH:
				transform.quadrantRotate(-1);
				step_pos = eval(c.x());
				x = (BB.getMaxY()-marginSize-cur.getWidth());
				y = step_pos+(cur.getHeight())/2;
				break;
			case SOUTH:
				transform.quadrantRotate(-1);
				step_pos = eval(c.x());
				x = BB.getMinY()+marginSize;
				y = step_pos+(cur.getHeight())/2;
				break;
			default:
				throw new NotSupportedException();
			}
			
			g.transform(transform);
			g.drawGlyphVector(gv, (float)x, (float)y);
			g.setTransform(original);
		}
	}
	
	@Override
	public Expression<? extends GraphicalContent> graphicalContent() {
		return new ExpressionBase<GraphicalContent>(){

			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				return new GraphicalContent() {
					
					@Override
					public void draw(DrawRequest r) {
						Graphics2D g = r.getGraphics();
						Color colorisation = r.getDecoration().getColorisation();
						
						drawContactConnections(r, context);
						
						Rectangle2D shape = context.resolve(contactLabelBB);
						
						g.setColor(Coloriser.colorise(CommonVisualSettings.getFillColor(), colorisation));
						g.fill(shape);
						g.setColor(Coloriser.colorise(CommonVisualSettings.getForegroundColor(), colorisation));
						g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
						g.draw(shape);
						
						drawContacts(r, context);
					}
				};
			}
			
		};
	}

	@Override
	public Expression<? extends Touchable> localSpaceTouchable() {
		return new ExpressionBase<Touchable>(){

			@Override
			protected Touchable evaluate(final EvaluationContext context) {
				return new Touchable(){

					@Override
					public boolean hitTest(Point2D point) {
						Rectangle2D clbb = context.resolve(contactLabelBB);
						if (clbb!=null) return clbb.contains(point);
						return false;
					}

					@Override
					public Rectangle2D getBoundingBox() {
						Rectangle2D totalbb = context.resolve(totalBB);
						if (totalbb!=null) return totalbb;
						
						double size = CommonVisualSettings.getSize();
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


	@Override
	public ExpressionBase<? extends Collection<Node>> children() {
		return groupImpl.children();
	}

	@Override
	public ModifiableExpression<Node> parent() {
		return groupImpl.parent();
	}

	@Override
	public void remove(Node node) {
		groupImpl.remove(node);
	}

	@Override
	public void add(Collection<Node> nodes) {
		groupImpl.add(nodes);
	}

	@Override
	public void add(Node node) {
		groupImpl.add(node);
	}

	@Override
	public void remove(Collection<Node> nodes) {
		for (Node n: nodes) {
			remove(n);
		}
	}

	@Override
	public void reparent(Collection<Node> nodes, Container newParent) {
		groupImpl.reparent(nodes, newParent);
	}


	@Override
	public void reparent(Collection<Node> nodes) {
		groupImpl.reparent(nodes);
	}
	
	@Override
	public Node customHitTest(Point2D point) {
		Point2D pointInLocalSpace = eval(parentToLocalTransform()).transform(point, null);
		for(Node vn : eval(children()))
			if (vn instanceof VisualNode)
				if(eval(((VisualNode)vn).shape()).hitTest(pointInLocalSpace))
					return vn;
		
		if(eval(shape()).hitTest(point))
			return this;
		else
			return null;
	}
	
	public VisualContact addInput(String name, VisualContact.Direction dir) {
		
		if (dir==null) dir=VisualContact.Direction.WEST;
		
		Contact c = new Contact(IoType.INPUT, name);
		
		VisualContact vc = new VisualContact(c, dir);
		addContact(vc);
		
		return vc;
	}
	
	public VisualContact addOutput(String name, VisualContact.Direction dir) {
		if (dir==null) dir=VisualContact.Direction.EAST;
		
		Contact c = new Contact(IoType.OUTPUT, name);
		VisualContact vc = new VisualContact(c, dir);
		addContact(vc);
		return vc;
	}
	
	public ModifiableExpression<String> name() {
		return ((CircuitComponent)getReferencedComponent()).name();
	}
}
