package org.workcraft.plugins.circuit;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.DummyEvaluationContext;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.circuit.Contact.IoType;
import org.workcraft.plugins.circuit.renderers.BufferRenderer;
import org.workcraft.plugins.circuit.renderers.CElementRenderer;
import org.workcraft.plugins.circuit.renderers.CElementRenderingResult;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.circuit.renderers.GateRenderer;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.Hierarchy;

public class VisualFunctionComponent extends VisualCircuitComponent implements ContactPositioner {
	
	ComponentRenderingResult renderingResult = null;
	
	private ComponentRenderingResult getRenderingResult(EvaluationContext context) {
		
		if (context.resolve(renderType())==RenderType.BOX) return null;
		
		if (context.resolve(children()).isEmpty()) return null;
		
		if (renderingResult==null) {
			// derive picture from the first output contact available
			for (Node n: context.resolve(children())) {
				if (n instanceof VisualFunctionContact) {
					VisualFunctionContact vc = (VisualFunctionContact)n;
					
					if (context.resolve(vc.getFunction().setFunction())==null) return null;
					
					if (context.resolve(vc.ioType())==IoType.OUTPUT) {
						switch (context.resolve(renderType())) {
						case GATE:
							renderingResult = GateRenderer.renderGate(context.resolve(vc.getFunction().setFunction()));
							break;
						case BUFFER:
							renderingResult = BufferRenderer.renderGate(context.resolve(vc.getFunction().setFunction()));
							break;
						case C_ELEMENT:
							if (context.resolve(vc.getFunction().resetFunction())!=null) {
								renderingResult = CElementRenderer.renderGate(
										context.resolve(vc.getFunction().setFunction()), context.resolve(vc.getFunction().resetFunction()));
							} else {
								return null;
							}
							break;
						}
					}
				}
			}
			
			if (renderingResult!=null) {
				updateStepPosition(); // beware!
			}
		}
		return renderingResult;
	}
	
	
	public void resetRenderingResult() {
		renderingResult = null;
	}
	
	public VisualFunctionComponent(CircuitComponent component, StorageManager storage) {
		super(component, storage);
		
		if (eval(component.children()).isEmpty()) {
			this.addFunction("x", null, false);
		}
		
	}
	
	@Override
	public Expression<? extends Touchable> shape() {
		final Expression<? extends Touchable> superTouchable = super.shape();
		return new ExpressionBase<Touchable>(){

			@Override
			protected Touchable evaluate(final EvaluationContext context) {
				return new Touchable(){

					@Override
					public Rectangle2D getBoundingBox() {
						Rectangle2D result = getTotalBB(context);
						return result != null ? result : context.resolve(superTouchable).getBoundingBox();
					}

					@Override
					public Point2D getCenter() {
						return new Point2D.Double(0,0);
					}

					@Override
					public boolean hitTest(Point2D point) {
						if (getRenderingResult(context)!=null) {
							return getBoundingBox().contains(point);
						} else {
							return context.resolve(superTouchable).hitTest(point);
						}
					}
				};
			}};
	}
	
	
	public VisualFunctionContact addFunction(String name, IoType ioType, boolean allowShort) {
	
		name = Contact.getNewName(this.getReferencedComponent(), name, null, allowShort);
		
		VisualContact.Direction dir=null;
		if (ioType==null) ioType = IoType.OUTPUT;
		
		dir=VisualContact.Direction.WEST;
		if (ioType==IoType.OUTPUT)
			dir=VisualContact.Direction.EAST;
		
		FunctionContact c = new FunctionContact(ioType, name, storage);
		
		VisualFunctionContact vc = new VisualFunctionContact(c, dir, storage);
		
		addContact(vc);
		
		return vc;
	}
	
	public VisualFunctionContact getOrCreateInput(String arg) {

		for(VisualFunctionContact c : Hierarchy.filterNodesByType(eval(children()), VisualFunctionContact.class)) {
			if(eval(c.name()).equals(arg)) return c;
		}
		
		VisualFunctionContact vc = addFunction(arg, IoType.INPUT, true);
		
		vc.getFunction().setFunction().setValue(One.instance());
		vc.getFunction().resetFunction().setValue(One.instance());

		return vc;
	}
	
	private Rectangle2D getResBB(EvaluationContext context) {
		
		ComponentRenderingResult res = getRenderingResult(context);
		
		if (res==null) return null;
		Rectangle2D rec = new Rectangle2D.Double();
		
		rec.setRect(res.boundingBox());
		
		Point2D p1 = new Point2D.Double(rec.getMinX(), rec.getMinY());
		Point2D p2 = new Point2D.Double(rec.getMaxX(), rec.getMaxY());
		
		AffineTransform at = VisualContact.getDirectionTransform(context.resolve(getMainContact(context).direction()));
		
		at.transform(p1, p1);
		at.transform(p2, p2);
		
		double x1 = Math.min(p1.getX(), p2.getX());
		double y1 = Math.min(p1.getY(), p2.getY());
		double x2 = Math.max(p1.getX(), p2.getX());
		double y2 = Math.max(p1.getY(), p2.getY());
		
		rec.setRect(x1, y1, x2-x1, y2-y1);
				
		return rec;
	}
	
	private Rectangle2D getTotalBB(EvaluationContext context) {

		Rectangle2D rec = getResBB(context);
		
		Rectangle2D result = BoundingBoxHelper.mergeBoundingBoxes(context.resolve(Hierarchy.childrenOfType(this, Touchable.class)));
		return BoundingBoxHelper.union(rec, result);
	}
	
	@Override
	protected void updateStepPosition()
	{
		DummyEvaluationContext context = DummyEvaluationContext.INSTANCE;
		ComponentRenderingResult res = getRenderingResult(context);
		if (res!=null)
		{
			VisualContact v = getMainContact(context);  
			
			AffineTransform at = new AffineTransform();
			AffineTransform bt = new AffineTransform();
			
			if (v!=null) at = VisualContact.getDirectionTransform(context.resolve(v.direction()));
			
			for (Node n: context.resolve(children())) {
				
				bt.setTransform(at);
				
				if (n instanceof VisualFunctionContact) {
					VisualFunctionContact vc = (VisualFunctionContact)n;
					
					if (context.resolve(vc.ioType()) == IoType.OUTPUT) {
						bt.translate(
							snapP5(res.boundingBox().getMaxX() + GateRenderer.contactMargin),0);
						
						vc.transform().setValue(bt);
						continue;
					}
					
					if (context.resolve(vc.ioType()) != IoType.INPUT) continue;
					
					Point2D position = res.contactPositions().get(context.resolve(vc.name()));
					
					if (position != null)
					{
						bt.translate(
								snapP5(res.boundingBox().getMinX() - GateRenderer.contactMargin),
								position.getY());
						
						vc.transform().setValue(bt);
					}
					
				}
			}
		}
		else
		{
			super.updateStepPosition();
		}		
	}
	
/*	@Override
	public void notify(StateEvent e) {
		if (e instanceof PropertyChangedEvent) {
			
			PropertyChangedEvent pc = (PropertyChangedEvent)e;
			
			
			if (pc.getPropertyName().equals("direction")) {
				if (getMainContact()==pc.getSender()&&getRenderingResult()!=null)
					updateStepPosition();
			}
		}
	}*/
	
	@Override
	public Expression<? extends ColorisableGraphicalContent> graphicalContent() {
		final Expression<? extends ColorisableGraphicalContent> superGraphicalContent = super.graphicalContent();
		return new ExpressionBase<ColorisableGraphicalContent>(){

			@Override
			protected ColorisableGraphicalContent evaluate(final EvaluationContext context) {
				return new ColorisableGraphicalContent() {
					
					@Override
					public void draw(DrawRequest r) {
						ComponentRenderingResult res = getRenderingResult(context);
						//System.out.println("rendering result bounding box: " + res.boundingBox());
						
						Graphics2D g = r.getGraphics();
						
						Color colorisation = r.getColorisation().getColorisation();
						Color col1 = Coloriser.colorise(CommonVisualSettings.getForegroundColor(), colorisation);
						Color col2 = Coloriser.colorise(CommonVisualSettings.getBackgroundColor(), colorisation);
						
						
						if (res!=null) {
							
							if (!context.resolve(isEnvironment())) {
								g.setStroke(new BasicStroke((float)CircuitSettings.getComponentBorderWidth()));
							} else {
								float dash[] = {0.05f, 0.05f};
								
								g.setStroke(
										new BasicStroke(
											(float)CircuitSettings.getComponentBorderWidth(),
											BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f,
											dash, 0.0f)
											);
							}
							
							
							Point2D mp=null, lp=null, pp=null;
							
							// draw component label
							r.getGraphics().setColor(Coloriser.colorise(CommonVisualSettings.getForegroundColor(), r.getColorisation().getColorisation()));
							drawNameInLocalSpace(r, context); // TODO: check if this is correct
							
							// draw the rest
							GateRenderer.foreground = col1;
							GateRenderer.background = col2;
							
							
							VisualContact v = getMainContact(context);  
							AffineTransform at = new AffineTransform();
							AffineTransform bt = new AffineTransform();
							
							if (res instanceof CElementRenderingResult) {
								CElementRenderingResult cr = (CElementRenderingResult)res;
								lp = cr.getLabelPosition();
								pp = cr.getPlusPosition();
								mp = cr.getMinusPosition();
							}
							
							if (v!=null) {
								switch (context.resolve(v.direction())) {
								case NORTH:
									at.quadrantRotate(3);
									bt.quadrantRotate(1);
									break;
								case SOUTH:
									at.quadrantRotate(1);
									bt.quadrantRotate(3);
									break;
								case WEST:
									at.quadrantRotate(2);
									bt.quadrantRotate(2);
									break;
								}
								g.transform(at);
									
								if (lp!=null) at.transform(lp, lp);
								if (mp!=null) at.transform(mp, mp);
								if (pp!=null) at.transform(pp, pp);
							}
							res.draw(g);
							
							// draw contact wires
							
							Stroke s = g.getStroke();
							g.setStroke(new BasicStroke((float)CircuitSettings.getCircuitWireWidth()));
							g.setColor(col1);
							
							Line2D line;

							
							// draw output and input lines
							
							for (Node n: context.resolve(VisualFunctionComponent.this.children())) {
								if (n instanceof VisualFunctionContact) {
									VisualFunctionContact vc = (VisualFunctionContact)n;
									
									if (context.resolve(vc.ioType()) == IoType.OUTPUT) {
										line = new Line2D.Double(
												res.boundingBox().getMaxX(), 0,
												snapP5(res.boundingBox().getMaxX()+GateRenderer.contactMargin), 0);
										g.draw(line);
										continue;
									}
									
									if (context.resolve(vc.ioType()) != IoType.INPUT) continue;
									
									Point2D position = res.contactPositions().get(context.resolve(vc.name()));
									
									if (position != null)
									{
										line = new Line2D.Double(snapP5(res.boundingBox().getMinX() - GateRenderer.contactMargin),
															position.getY(), position.getX(), position.getY());
										
										g.draw(line);
									}
								}
							}
							
							g.transform(bt);
							
							g.setStroke(s);
							
							// for C element draw letter C
							if (lp!=null) {
//								Line2D l = new Line2D.Double(lp.getX(),lp.getY(),lp.getX()+0.01,lp.getY()+0.01);
//								g.draw(l);
								
								r.getGraphics().drawString("C",(float)lp.getX()-(float)0.2,
											(float)lp.getY()+(float)0.2);
							}
							
							if (pp!=null) {
//								Line2D l = new Line2D.Double(pp.getX(),pp.getY(),pp.getX()+0.01,pp.getY()+0.01);
//								g.draw(l);
								r.getGraphics().drawString("+",(float)pp.getX()-(float)0.15,
								(float)pp.getY()+(float)0.15);
							}
						
							
							if (mp!=null) {
//								Line2D l = new Line2D.Double(mp.getX(),mp.getY(),mp.getX()+0.01,mp.getY()+0.01);
//								g.draw(l);
								r.getGraphics().drawString("-",(float)mp.getX()-(float)0.15,
								(float)mp.getY()+(float)0.15);
							}
							
							
							
						} else {
							context.resolve(superGraphicalContent).draw(r);
						}
					}
				};
			}
		};
	}

	@Override
	public Point2D position(VisualContact contact, Point2D wantedPosition, EvaluationContext context) {
		double x = wantedPosition.getX();
		double y = wantedPosition.getY();
		Rectangle2D bb = context.resolve(contactLabelBB);
		switch (context.resolve(contact.direction())) {
		case EAST:
			x = snapP5(bb.getMaxX() + contactLength);
			break;
		case WEST:
			x = snapP5(bb.getMinX() - contactLength);
			break;
		case NORTH:
			y = snapP5(bb.getMinY() - contactLength);
			break;
		case SOUTH:
			y = snapP5(bb.getMaxY() + contactLength);
			break;
		}
		return new Point2D.Double(x, y);
	}
}
