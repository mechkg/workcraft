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
import pcollections.PVector
import org.workcraft.plugins.cpog.VariableState
import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.plugins.cpog.VisualComponent
import org.workcraft.gui.propertyeditor.choice.ChoiceProperty
import org.workcraft.util.Pair

package org.workcraft.plugins.cpog.scala {

  object VisualVariable {
    val size = 1;
    private val strokeWidth = 0.08f;
    
    private val nameFont = FormulaToGraphics.fancyFont;
	private val valueFont = nameFont.deriveFont(0.75f);

    def image(variable: Variable): Expression[BoundedColorisableGraphicalContent] = {
      new ExpressionBase[BoundedColorisableGraphicalContent] {
        def evaluate(context: EvaluationContext) : BoundedColorisableGraphicalContent = {
          val state = context.resolve(variable.state)
          val label = context.resolve(variable.visualProperties.label)
          val fillColor = context.resolve(CommonVisualSettings.fillColor)
          val foreColor = context.resolve(CommonVisualSettings.foregroundColor)
          
          val frame      = boundedRectangle (size, size, new BasicStroke (strokeWidth), fillColor, foreColor)
          val valueLabel = boundedFormulaLabel (state.toString, valueFont, foreColor)
          val nameLabel  = boundedFormulaLabel (label, nameFont, foreColor)
          
          compose (
        		  frame ::
        		  aligned (valueLabel, frame, HorizontalAlignment.Center, VerticalAlignment.Center) ::
        		  sideways (nameLabel, frame, context.resolve(variable.visualProperties.labelPositioning)) ::
        		  Nil
                )
        }
      }
    }
    
	def getProperties(v : Variable) : PVector[EditableProperty] = {
		val states = (TreePVector.empty[Pair[String, VariableState]]
		.plus(Pair.of("[1] true", VariableState.TRUE))
		.plus(Pair.of("[0] false", VariableState.FALSE))
		.plus(Pair.of("[?] undefined", VariableState.UNDEFINED)))
		
		VisualComponent.getProperties(v.visualProperties)
			.plus(ChoiceProperty.create("State", states, v.state));
	}
    
  }
}
/*

public class VisualVariableGui
{
	
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
