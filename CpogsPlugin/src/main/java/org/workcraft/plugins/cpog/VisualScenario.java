/*package org.workcraft.plugins.cpog;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.DrawableNew;
import org.workcraft.dom.visual.ReflectiveTouchable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.cpog.EncodingProperty;
import org.workcraft.gui.propertyeditor.string.StringProperty;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics;
import org.workcraft.util.Hierarchy;

import pcollections.PVector;
import pcollections.TreePVector;

public class VisualScenario extends VisualGroup implements ReflectiveTouchable, DrawableNew
{
	private static final class ReverseComparator implements Comparator<Variable>
	{
		@Override
		public int compare(Variable o1, Variable o2) {
			return -o1.compareTo(o2);
		}
	}

	private static final float frameDepth = 0.25f;
	private static final float strokeWidth = 0.03f;
	private static final float minVariableWidth = 0.7f;
	private static final float minVariableHeight = 0.85f;
	
	private final ModifiableExpression<Rectangle2D> contentsBB;
	private final ModifiableExpression<Rectangle2D> labelBB;
	private final ModifiableExpression<Rectangle2D> encodingBB;
	
	private Map<Rectangle2D, Variable> variableBBs = new HashMap<Rectangle2D, Variable>();
	
	private final ModifiableExpression<String> label;
	private final ModifiableExpression<Encoding> encoding;
	
	private static Font labelFont;
	
	static {
		try {
			labelFont = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/eurm10.pfb")).deriveFont(0.5f);
		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	@Override
	public PVector<EditableProperty> getProperties() {
		return TreePVector.<EditableProperty>empty()
			.plus(StringProperty.create("Label", label()))
			.plus(EncodingProperty.create("Encoding", encoding()));
	}
	
	public VisualScenario(StorageManager storage)
	{
		super(storage);
		contentsBB = storage.create(null);
		labelBB = storage.create(null);
		encodingBB = storage.create(null);
		label = storage.create("");
		encoding = storage.create(new Encoding());
		
	}

	@Override
	public Expression<Touchable> shape() {
		return new ExpressionBase<Touchable>() {

			@Override
			protected Touchable evaluate(final EvaluationContext context) {
				return new Touchable() {
					
					@Override
					public boolean hitTest(Point2D p) {
						return 
						getContentsBoundingBox(context).contains(p) ||
						getLabelBB(context).contains(p) ||
						context.resolve(encodingBB).contains(p);
					}
					
					@Override
					public Point2D getCenter() {
						return new Point2D.Double(getBoundingBox().getCenterX(), getBoundingBox().getCenterY());
					}
					
					@Override
					public Rectangle2D getBoundingBox() {
						Rectangle2D bb = getContentsBoundingBox(context);
						
						// Increase bb by the label height (to include the latter into the bb)
						if(context.resolve(labelBB) != null)
							bb.add(bb.getMinX(), bb.getMinY() - context.resolve(labelBB).getHeight());
						
						// Increase bb by the encoding height (to include the latter into the bb)
						if(context.resolve(encodingBB) != null)
							bb.add(bb.getMinX(), bb.getMaxY() + context.resolve(encodingBB).getHeight());
						
						return bb;
					}
				};
			}
		};
	}

	private Rectangle2D getContentsBoundingBox(EvaluationContext context) {
		Rectangle2D bb = null;
		
		// TODO: throw away everything?
		for(VisualVertex v : context.resolve(Hierarchy.childrenOfType(this, VisualVertex.class)))
			bb = BoundingBoxHelper.union(bb, context.resolve(v.shape()).getBoundingBox());

		for(VisualVariable v : context.resolve(Hierarchy.childrenOfType(this, VisualVariable.class)))
			bb = BoundingBoxHelper.union(bb, context.resolve(v.shape()).getBoundingBox());

		for(VisualArc a : context.resolve(Hierarchy.childrenOfType(this, VisualArc.class)))
			bb = BoundingBoxHelper.union(bb, context.resolve(a.getLabelBoundingBox()));

		if (bb == null) bb = context.resolve(contentsBB);
		else
		bb.setRect(bb.getMinX() - frameDepth, bb.getMinY() - frameDepth, 
				   bb.getWidth() + 2.0 * frameDepth, bb.getHeight() + 2.0 * frameDepth);
		
		if (bb == null) bb = new Rectangle2D.Double(0, 0, 1, 1);

		contentsBB.setValue( (Rectangle2D) bb.clone());
		
		return bb;
	}
	
	@Override
	public ExpressionBase<ColorisableGraphicalContent> graphicalContent() {
		return new ExpressionBase<ColorisableGraphicalContent>() {

			@Override
			protected ColorisableGraphicalContent evaluate(final EvaluationContext context) {
				return new ColorisableGraphicalContent(){
					@Override
					public void draw(DrawRequest r) {
						Graphics2D g = r.getGraphics();
						Color colorisation = r.getColorisation().getColorisation();
						
						Rectangle2D bb = getContentsBoundingBox(context);
					
						if (bb != null && context.resolve(parent()) != null)
						{
							g.setColor(Coloriser.colorise(Color.WHITE, colorisation));
							g.fill(bb);
							g.setColor(Coloriser.colorise(Color.BLACK, colorisation));
							g.setStroke(new BasicStroke(strokeWidth));
							g.draw(bb);
							
							// draw label
							
							FormulaRenderingResult result = FormulaToGraphics.print(context.resolve(label), labelFont, g.getFontRenderContext());
							
							Rectangle2D lbb = BoundingBoxHelper.expand(result.boundingBox, 0.4, 0.2);
							labelBB.setValue(lbb);
								
							Point2D labelPosition = new Point2D.Double(bb.getMaxX() - lbb.getMaxX(), bb.getMinY() - lbb.getMaxY());
					
							g.setColor(Coloriser.colorise(Color.WHITE, colorisation));
							g.fill(getLabelBB(context));
							g.setStroke(new BasicStroke(strokeWidth));
							g.setColor(Coloriser.colorise(Color.BLACK, colorisation));
							g.draw(getLabelBB(context));			
						
							AffineTransform transform = g.getTransform();
							g.translate(labelPosition.getX(), labelPosition.getY());
									
							result.draw(g, Coloriser.colorise(Color.BLACK, colorisation));
							
							g.setTransform(transform);
					
							// draw encoding
							
							Set<Variable> sortedVariables = new TreeSet<Variable>(new ReverseComparator());
							sortedVariables.addAll(context.resolve(encoding).getStates().keySet());
							
							double right = bb.getMaxX();
							double top = bb.getMaxY();
							
							Rectangle2D encBB = new Rectangle2D.Double(right, top, 0, 0);
							
							variableBBs.clear();			
							
							boolean perfectMatch = true;
							
							for(Variable var : sortedVariables) if (!context.resolve(var.state()).matches(context.resolve(encoding).getState(var))) perfectMatch = false;
							
							for(Variable var : sortedVariables)
							{
								String text = var.getLabel();
								
								result = FormulaToGraphics.print(text, labelFont, g.getFontRenderContext());
												
								bb = result.boundingBox;
								bb = BoundingBoxHelper.expand(bb, 0.4, 0.2);
								
								if (bb.getWidth() < minVariableWidth) bb = BoundingBoxHelper.expand(bb, minVariableWidth - bb.getWidth(), 0);
								if (bb.getHeight() < minVariableHeight) bb = BoundingBoxHelper.expand(bb, 0, minVariableHeight - bb.getHeight());
								
								labelPosition = new Point2D.Double(right - bb.getMaxX(), top - bb.getMinY());
								
								double left = right - bb.getWidth();
								double bottom = top + bb.getHeight();
								
								Rectangle2D tmpBB = new Rectangle2D.Double(left, top, bb.getWidth(), bb.getHeight()); 
								
								encBB = BoundingBoxHelper.union(encBB, tmpBB);
								
								g.setColor(Coloriser.colorise(Color.WHITE, colorisation));
								g.fill(tmpBB);
								g.setStroke(new BasicStroke(strokeWidth));
								g.setColor(Coloriser.colorise(Color.BLACK, colorisation));
								g.draw(tmpBB);
								
								transform = g.getTransform();
								g.translate(labelPosition.getX(), labelPosition.getY());
										
								result.draw(g, Coloriser.colorise(Color.BLACK, colorisation));
								
								g.setTransform(transform);
								
								variableBBs.put(tmpBB, var);
								
								text = context.resolve(encoding).getState(var).toString();
								if (text.equals("?")) text = "\u2013";
								
								result = FormulaToGraphics.print(text, labelFont, g.getFontRenderContext());
								
								bb = result.boundingBox;
								bb = BoundingBoxHelper.expand(bb, tmpBB.getWidth() - bb.getWidth(), tmpBB.getHeight() - bb.getHeight());				
								
								labelPosition = new Point2D.Double(right - bb.getMaxX(), bottom - bb.getMinY());
					
								tmpBB = new Rectangle2D.Double(left, bottom, bb.getWidth(), bb.getHeight()); 
								
								encBB = BoundingBoxHelper.union(encBB, tmpBB);
								
								g.setColor(Coloriser.colorise(Color.WHITE, colorisation));
								g.fill(tmpBB);
								g.setStroke(new BasicStroke(strokeWidth));
								g.setColor(Coloriser.colorise(Color.BLACK, colorisation));
								g.draw(tmpBB);
								
								transform = g.getTransform();
								g.translate(labelPosition.getX(), labelPosition.getY());
								
								Color color = Color.BLACK;
								if (!context.resolve(var.state()).matches(context.resolve(encoding).getState(var))) color = Color.RED;
								if (perfectMatch) color = Color.GREEN;
					
								result.draw(g, Coloriser.colorise(color, colorisation));
								
								g.setTransform(transform);
								
								variableBBs.put(tmpBB, var);
								
								right = left;
							}
							encodingBB.setValue(encBB);
						}
					}					
				};
			}
		};
	}
	
	public Variable getVariableAt(Point2D p)
	{
		Point2D q = new Point2D.Double();
		GlobalCache.eval(parentToLocalTransform()).transform(p, q);
		for(Rectangle2D rect : variableBBs.keySet())
			if (rect.contains(q)) return variableBBs.get(rect);
		
		return null;
	}
	
	private Rectangle2D getLabelBB(EvaluationContext context) {
		Rectangle2D bb = getContentsBoundingBox(context);
		Rectangle2D lbb = context.resolve(labelBB);
		return new Rectangle2D.Double(bb.getMaxX() - lbb.getWidth(), bb.getMinY() - lbb.getHeight(), lbb.getWidth(), lbb.getHeight());
	}

	public ModifiableExpression<String> label()
	{
		return label;
	}

	public ModifiableExpression<Encoding> encoding()
	{
		return encoding;
	}
}
*/
