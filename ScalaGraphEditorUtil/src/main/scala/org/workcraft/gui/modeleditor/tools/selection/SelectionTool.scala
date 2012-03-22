package org.workcraft.gui.modeleditor.tools.selection

import java.awt.Color
import java.awt.event.KeyEvent

import javax.swing.Icon
import javax.swing.JPanel

import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.ExpressionBase
import org.workcraft.dependencymanager.advanced.core.Expressions
/*@Deprecated // this needs to be thrown  out
public class SelectionTool implements GraphEditorTool, DecorationProvider[Colorisator] {

  private final GenericSelectionTool[Node] selectionTool
  private final Expression[? extends Node] currentLevel
  
  override def GraphEditorMouseListener mouseListener {
    return selectionTool.getMouseListener
  }
  
  public SelectionTool(SelectionToolConfig[Node] config) {
    this.currentLevel = config.currentEditingLevel
    selectionTool = new GenericSelectionTool[Node](
        config.selection,
        config.hitTester,
        new MoveDragHandler[Node](config.selection, config.movableController, config.snap)
      )
  }

  protected Color grayOutColor = Color.LIGHT_GRAY

  
  override def Colorisator getDecoration {
    return getColorisator
  }
  
  public Colorisator getColorisator {

    Colorisation greyOutColourisation = new Colorisation{
      override def Color getColorisation {
        return grayOutColor
      }

      override def Color getBackground {
        return null
      }
    }
    return new HierarchicalColorisator(greyOutColourisation) {
      
      override def Expression[Colorisation] getSimpleColorisation(final Node node) {
        return new ExpressionBase[Colorisation]{

          @Override
          protected Colorisation evaluate(final EvaluationContext context) {
            if(node == context.resolve(currentLevel))
              return Colorisation.EMPTY
            
            Colorisation selectedDecoration = new Colorisation {
              override def Color getColorisation {
                return selectionColor
              }
  
              override def Color getBackground {
                return null
              }
            }
            
            if(context.resolve(selectionTool.effectiveSelection).contains(node))
              return selectedDecoration
            else
              return null
          }
        }
      }
    }
  }
  
  
  
  protected static Color selectionColor = new Color(99, 130, 191).brighter

  override def Expression[? extends GraphicalContent] userSpaceContent(Viewport viewport, final Expression[Boolean] hasFocus) {
    return selectionTool.userSpaceContent(viewport)
  }

  override def Expression[? extends GraphicalContent] screenSpaceContent(final Viewport viewport, final Expression[Boolean] hasFocus) {
    return Expressions.constant(GraphicalContent.EMPTY)
  }

  override def GraphEditorKeyListener keyListener {
    return DummyKeyListener.INSTANCE
  }

  override def void activated {
  }

  override def void deactivated {
  }

  override def JPanel getInterfacePanel {
    return null
  }

  override def Button getButton {
    throw new RuntimeException ("This class is deprecated")
  }
}
*/