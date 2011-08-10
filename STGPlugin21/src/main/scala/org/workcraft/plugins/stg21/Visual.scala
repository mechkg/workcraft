package org.workcraft.plugins.stg21
import java.awt.Color
import java.awt.Font
import org.workcraft.graphics.Label
import org.workcraft.plugins.stg21.types.SignalType
import org.workcraft.util.Graphics

object Visual {
  object VisualSignalTransition {
    val inputsColor = Color.RED.darker
    val outputsColor = Color.BLUE.darker
    val internalsColor = Color.GREEN.darker

    val font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.75f);

	
  def color(t : SignalType) =
    t match {
      case SignalType.Internal => internalsColor
      case SignalType.Output => outputsColor
      case SignalType.Input => inputsColor
	}
	
	def graphicalContent(text : String) = {
	  val color = color()
	  val label = Graphics.label(text, font, color)
	}
	public Expression<? extends ColorisableGraphicalContent> getGraphicalContent(final Expression<? extends String> text) {
		final Label label = label(text);
		final Expression<Touchable> shapeExpr = localSpaceTouchable(text);
		final Expression<Color> colorExpr = color();
		return new ExpressionBase<ColorisableGraphicalContent>() {
			@Override
			protected ColorisableGraphicalContent evaluate(final EvaluationContext context) {
				return new ColorisableGraphicalContent() {
					@Override
					public void draw(DrawRequest r) {
						
						final ColorisableGraphicalContent labelGraphics = context.resolve(labelGraphics());
						final ColorisableGraphicalContent nameLabelGraphics = context.resolve(label.graphics);
						final Color color = context.resolve(colorExpr);
						final Touchable shape = context.resolve(shapeExpr);
						
						labelGraphics.draw(r);
						
						Graphics2D g = r.getGraphics();
						
						Color background = r.getColorisation().getBackground();
						if(background!=null)
						{
							g.setColor(background);
							g.fill(shape.getBoundingBox());
						}
						
						g.setColor(Coloriser.colorise(color, r.getColorisation().getColorisation()));
						
						nameLabelGraphics.draw(r);
					}
				};
			}
		};
	}

	private Label label(final Expression<? extends String> text) {
		return new Label(font, text);
	}
	
	public Expression<Touchable> localSpaceTouchable(final Expression<? extends String> text) {
		final Label label = label(text);
		return new ExpressionBase<Touchable>() {
			@Override
			protected Touchable evaluate(final EvaluationContext context) {
				return new Touchable() {
					@Override
					public Rectangle2D getBoundingBox() {
						return context.resolve(label.centeredBB);
					}
					
					@Override
					public Point2D getCenter() {
						return new Point2D.Double(0, 0);
					}
					
					@Override
					public boolean hitTest(Point2D point) {
						return getBoundingBox().contains(point);
					}
				};
			}
		};
	}

	@NoAutoSerialisation
	public SignalTransition getReferencedTransition() {
		return (SignalTransition)getReferencedComponent();
	}
	
	@NoAutoSerialisation
	public ModifiableExpression<Type> signalType() {
		return getReferencedTransition().signalType();
	}
}
}