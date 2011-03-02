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
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dependencymanager.advanced.core.DummyEvaluationContext;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionWriteHandler;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.CustomTouchable;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.DrawableNew;
import org.workcraft.dom.visual.Label;
import org.workcraft.dom.visual.ReflectiveTouchable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.ExpressionPropertyDeclaration;
import org.workcraft.plugins.circuit.Contact.IoType;
import org.workcraft.plugins.circuit.VisualContact.Direction;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.shared.CommonVisualSettings;

@DisplayName("Abstract Component")
@Hotkey(KeyEvent.VK_A)
@SVGIcon("images/icons/svg/circuit-component.svg")

public class VisualCircuitComponent extends VisualComponent implements Container, CustomTouchable, DrawableNew, ReflectiveTouchable {
	
	private Color inputColor = VisualContact.inputColor;
	private Color outputColor = VisualContact.outputColor;
	
	protected static Font nameFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);

	double marginSize = 0.2;
	double contactLength = 1;
	double contactStep = 1;
	
	final ModifiableExpression<RenderType> renderType;
	
	private WeakReference<VisualContact> mainContact = null;
	
	public void setMainContact(VisualContact contact) {
		this.mainContact = new WeakReference<VisualContact>(contact);
	}

	public VisualContact getMainContact(EvaluationContext context) {
		VisualContact ret = null;
		if (mainContact!=null) ret=mainContact.get();
		if (ret==null) {
			for (Node  n : context.resolve(children())) {
				if (n instanceof VisualContact) {
					if (context.resolve(((VisualContact)n).ioType())==IoType.OUTPUT) {
						setMainContact((VisualContact)n);
						ret = (VisualContact)n;
						break;
					}
				}
			}
		}
		return ret;
	}
	
	public ModifiableExpression<RenderType> renderType() {
		return renderType;
	}

	final DefaultGroupImpl groupImpl;
	
	protected final Expression<Rectangle2D> contactLabelBB = createContactLabelBbExpression();
	protected final Expression<Rectangle2D> totalBB = createTotalBbExpression();
	
	final Label nameLabel;
	protected final StorageManager storage; 

	protected void drawNameInLocalSpace(DrawRequest r, EvaluationContext context) {

		Graphics2D g = r.getGraphics();
		g.setColor(Coloriser.colorise(CommonVisualSettings.getForegroundColor(), r.getColorisation().getColorisation()));
		
		g.setFont(nameFont);
		Rectangle2D contactLabelBB = context.resolve(this.contactLabelBB);
		if (contactLabelBB!=null) {
			AffineTransform oldTransform = g.getTransform();
			Rectangle2D textBB = context.resolve(nameLabel.centeredBB);
			g.translate(contactLabelBB.getMaxX() + 0.2 - textBB.getMinX(), 
					contactLabelBB.getMaxY() + 0.2 - textBB.getMinY());
			context.resolve(nameLabel.graphics).draw(r);
			g.setTransform(oldTransform);
		}
	}
	
	public VisualCircuitComponent(CircuitComponent component, StorageManager storage) {
		super(component, storage);
		this.storage = storage;
		
		renderType = new ModifiableExpressionWriteHandler<RenderType>(storage.create(RenderType.BOX)) {
			protected void afterSet(RenderType newValue) {
				updateStepPosition();
			};
		};
		
		groupImpl = new DefaultGroupImpl(this, storage);
		
		nameLabel = new Label(nameFont, component.name());
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Name", name(), String.class));
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Treat as environment", isEnvironment(), Boolean.class));
		
		addPropertyDeclaration(RenderType.EditorProperty.create("Render type", renderType()));
		
	}
	
	public ModifiableExpression<Boolean> isEnvironment() {
		return ((CircuitComponent)getReferencedComponent()).isEnvironment();
	}
	
	// updates sequential position of the contacts
	protected void updateStepPosition() {
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
	
	public static double snapP5(double x) {
		return (double)(Math.round((x)*2))/2;
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
				Rectangle2D result = context.resolve(contactLabelBB);
				if(true)throw new NotImplementedException("need custom touchable here");
				for(Node child : VisualCircuitComponent.this.getContacts(context))
					result = BoundingBoxHelper.union(result, context.resolve(((ReflectiveTouchable)child).shape()).getBoundingBox());
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
					switch(context.resolve(vc.direction())) {
					case EAST : east++; break;
					case SOUTH : south++; break;
					case NORTH : north++; break;
					case WEST : west++; break;
					}
				}
				
				Rectangle2D cur;
				double xx;
				double width_w=0;
				double width_e=0;
				double width_n=0;
				double width_s=0;
				
				for (VisualContact c: getContacts(context)) {
					GlyphVector gv = context.resolve(c.nameGlyphs());
					cur = gv.getVisualBounds();
					xx = cur.getWidth();
					xx = (double)(Math.round(xx*4))/4;
					
					
					switch (context.resolve(c.direction())) {
						case WEST:
							width_w = Math.max(xx, width_w);
							break;
						case EAST:
							width_e = Math.max(xx, width_e);
							break;
						case NORTH:
							width_n = Math.max(xx, width_n);
							break;
						case SOUTH:
							width_s = Math.max(xx, width_s);
							break;
					}
				}
				
				double height = Math.max(east, west)*contactStep+width_n+width_s+marginSize*4;
				double width = Math.max(north, south)*contactStep+width_e+width_w+marginSize*4;
			
				Rectangle2D result = new Rectangle2D.Double(-width/2, -height/2, width, height);
				//updateSidePosition(result, null); // dangerous TODO
			
				return result;
			}
		};
	}
	
	protected void drawContactConnections(DrawRequest r, EvaluationContext context) {
		Graphics2D g = r.getGraphics();
		Color colorisation = r.getColorisation().getColorisation();
		g.setStroke(new BasicStroke((float)CircuitSettings.getCircuitWireWidth()));
		
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
				g.draw(line);
			}
		}
		
	}

	private Collection<VisualContact> getContacts(EvaluationContext context) {
		ArrayList<VisualContact> result = new ArrayList<VisualContact>();
		for (Node n: context.resolve(children())) {
			if(n instanceof VisualContact)
				result.add((VisualContact)n);
		}
		return result;
	}
	
	protected void drawContacts(DrawRequest r, EvaluationContext context) {
		
		Graphics2D g = r.getGraphics();
		Color colorisation = r.getColorisation().getColorisation();
		
		Rectangle2D BB = context.resolve(contactLabelBB);
	
		
		for (VisualContact c: getContacts(context)) {
			GlyphVector gv = context.resolve(c.nameGlyphs());
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
	public Expression<? extends ColorisableGraphicalContent> graphicalContent() {
		return new ExpressionBase<ColorisableGraphicalContent>(){

			@Override
			protected ColorisableGraphicalContent evaluate(final EvaluationContext context) {
				return new ColorisableGraphicalContent() {
					
					@Override
					public void draw(DrawRequest r) {
						drawNameInLocalSpace(r, context);
		
						Graphics2D g = r.getGraphics();
						Color colorisation = r.getColorisation().getColorisation();
						
						drawContactConnections(r, context);
						
						Rectangle2D shape = context.resolve(contactLabelBB);
						
						g.setColor(Coloriser.colorise(CommonVisualSettings.getFillColor(), colorisation));
						g.fill(shape);
						g.setColor(Coloriser.colorise(CommonVisualSettings.getForegroundColor(), colorisation));

						if (!context.resolve(isEnvironment())) {
							g.setStroke(new BasicStroke((float) CircuitSettings.getComponentBorderWidth()));
						} else {
							float dash[] = { 0.25f, 0.25f };

							g.setStroke(new BasicStroke((float) CircuitSettings.getComponentBorderWidth(),
									BasicStroke.CAP_BUTT,
									BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f));
						}
						g.draw(shape);

						drawContacts(r, context);
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
	public Expression<? extends Collection<Node>> children() {
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
		for(VisualContact vc : getContacts(new DummyEvaluationContext()))
			if(eval(vc.shape()).hitTest(pointInLocalSpace))
				return vc;
		
		if(eval(shape()).hitTest(point))
			return this;
		else
			return null;
	}
	
			
	public VisualContact addInput(String name, VisualContact.Direction dir) {
		
		if (dir==null) dir=VisualContact.Direction.WEST;
		
		Contact c = new Contact(IoType.INPUT, name, storage);
		
		VisualContact vc = new VisualContact(c, dir, storage);
		addContact(vc);
		
		return vc;
	}
	
	public VisualContact addOutput(String name, VisualContact.Direction dir) {
		if (dir==null) dir=VisualContact.Direction.EAST;
		
		Contact c = new Contact(IoType.OUTPUT, name, storage);
		VisualContact vc = new VisualContact(c, dir, storage);
		addContact(vc);
		return vc;
	}
	
	public ModifiableExpression<String> name() {
		return ((CircuitComponent)getReferencedComponent()).name();
	}
}
