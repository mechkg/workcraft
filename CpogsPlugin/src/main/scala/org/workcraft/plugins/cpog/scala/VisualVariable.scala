import org.workcraft.dom.visual.BoundedColorisableGraphicalContent
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.ExpressionBase
import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import org.workcraft.plugins.cpog.scala.nodes._
import org.workcraft.plugins.cpog.scala.Graphics._
import org.workcraft.plugins.shared.CommonVisualSettings
import org.workcraft.plugins.cpog.scala.formularendering.FormulaToGraphics
import org.workcraft.plugins.cpog.LabelPositioning
import pcollections.TreePVector
import java.awt.geom.Rectangle2D
import java.awt.BasicStroke
import org.workcraft.plugins.cpog.scala.Expressions._

package org.workcraft.plugins.cpog.scala {

  object VisualVariable {
    val size = 1;
    private val strokeWidth = 0.08f;

    private val nameFont = FormulaToGraphics.fancyFont;
    private val valueFont = nameFont.deriveFont(0.75f);

    def image(variable: Variable): Expression[RichGraphicalContent] =
      for (
        state <- variable.state;
        label <- variable.visualProperties.label;
        fillColor <- CommonVisualSettings.fillColor;
        foreColor <- CommonVisualSettings.foregroundColor
      ) yield {
        val frame = rectangle(size, size, new BasicStroke(strokeWidth), fillColor, foreColor)
        val valueLabel = formulaLabel(state.toString, valueFont, foreColor)
        val nameLabel = formulaLabel(label, nameFont, foreColor)

        valueLabel `aligned to` (frame, HorizontalAlignment.Center, VerticalAlignment.Center) over
        nameLabel `adjacent to` (frame, LabelPositioning.BOTTOM) over
        frame
      }
  }
}
/*

public class VisualVariableGui
{
	private static double size = 1;
	private static float strokeWidth = 0.08f;
	
	private static Font nameFont = FormulaRenderer.fancyFont;
	private static Font valueFont = nameFont.deriveFont(0.75f);
	
	public static PVector<EditableProperty> getProperties(Variable var) {
		PVector<Pair<String, VariableState>> states = TreePVector.<Pair<String, VariableState>>empty()
		.plus(Pair.of("[1] true", VariableState.TRUE))
		.plus(Pair.of("[0] false", VariableState.FALSE))
		.plus(Pair.of("[?] undefined", VariableState.UNDEFINED));
		
		return VisualComponent.getProperties(var.visualVar)
			.plus(ChoiceProperty.create("State", states, var.state));
	}
	
	private static BoundedColorisableGraphicalContent makeLabel(final String formula) {
		return FormulaToGraphics.print(formula, valueFont, Label.podgonFontRenderContext()).asBoundedColorisableImage();
	}
	
	private static Function<String, BoundedColorisableGraphicalContent> makeLabel =
		new Function<String, BoundedColorisableGraphicalContent>() {
			@Override
			public BoundedColorisableGraphicalContent apply(String argument) {
				return makeLabel(argument);
			}
		};
	
	private static Function<Object, String> toString = new Function<Object, String>(){
		@Override
		public String apply(Object argument) {
			return argument.toString();
		}
	};
	
	public static void toggle(Variable var)
	{
		var.state.setValue(eval(var.state).toggle());
	}	
	
	public static Expression<BoundedColorisableGraphicalContent> getImage(Variable var)  {
		Function2<BoundedColorisableGraphicalContent, GraphicsAlignment, BoundedColorisableGraphicalContent> labelNamePositioner = new Function2<BoundedColorisableGraphicalContent, GraphicsAlignment, BoundedColorisableGraphicalContent> (){
			@Override
			public BoundedColorisableGraphicalContent apply(BoundedColorisableGraphicalContent image, GraphicsAlignment positioning) {
				return positionRelative(visualBox, positioning, image);
			}
		};
		
		final Expression<BoundedColorisableGraphicalContent> valueLabel = fmap(composition(toString, makeLabel, centerToZero), var.state);
		final Expression<BoundedColorisableGraphicalContent> nameLabel = fmap(labelNamePositioner, fmap(makeLabel, var.label), var.visualVar.labelPosition);
		final Expression<BoundedColorisableGraphicalContent> box = simpleColorisableRectangle(visualBox);
		return fmap(composeFunc, fmap(composeFunc, box, nameLabel), valueLabel);
	}
	
	public static Expression<BoundedColorisableGraphicalContent> simpleColorisableRectangle(final Rectangle2D rect) {
		return fmap(new Function2<Color, Color, BoundedColorisableGraphicalContent>(){

			@Override
			public BoundedColorisableGraphicalContent apply(final Color fillColor, final Color foreColor) {
				return new BoundedColorisableGraphicalContent(new ColorisableGraphicalContent(){

					@Override
					public void draw(DrawRequest r) {
						Graphics2D g = r.getGraphics();
						Color colorisation = r.getColorisation().getColorisation();
						
						Shape shape = BoundingBoxHelper.expand(rect, -strokeWidth, -strokeWidth);

						g.setStroke(new BasicStroke(strokeWidth));

						g.setColor(Coloriser.colorise(fillColor, colorisation));
						g.fill(shape);
						g.setColor(Coloriser.colorise(foreColor, colorisation));
						g.draw(shape);
					}
				}, rect);
			}
		}, CommonVisualSettings.fillColor, CommonVisualSettings.foregroundColor);
	}

	public static Rectangle2D visualBox = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
}
*/
