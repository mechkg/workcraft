package org.workcraft.gui.modeleditor
import java.awt.event.HierarchyEvent
import java.awt.event.HierarchyListener
import javax.swing.JPanel
import org.workcraft.dependencymanager.advanced.core.ExpressionBase
import org.workcraft.dependencymanager.advanced.core.GlobalCache
import javax.swing.Timer
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import org.workcraft.dependencymanager.advanced.user.Variable
import java.awt.event.FocusListener
import java.awt.event.FocusEvent
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import java.awt.BasicStroke
import org.workcraft.graphics.GraphicalContent
import scalaz._
import Scalaz._
import java.awt.geom.AffineTransform
import java.awt.RenderingHints
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.Rectangle2D
import org.workcraft.gui.CommonVisualSettings
import org.workcraft.gui.modeleditor.tools.ToolboxPanel
import org.workcraft.logging.Logger
import org.workcraft.gui.modeleditor.tools.ModelEditorTool
import org.workcraft.gui.modeleditor.tools.Toolbox
import org.workcraft.gui.modeleditor.tools.ToolEnvironment
import org.workcraft.services.ModelServiceProvider

class ModelEditorPanel (val model: ModelServiceProvider, val editor: ModelEditor) (implicit logger: () => Logger[IO]) extends JPanel {
  val panelDimensions = Variable.create((0, 0, getWidth, getHeight))
  val viewDimensions = panelDimensions.map { case (x,y,w,h) => (x + 15,y + 15, w - 15, h - 15) }

  object Repainter {
    class Image

    val repainter = graphicalContent.map(_ => { ModelEditorPanel.this.repaint(); new Image })

    val timer = new Timer(20, new ActionListener {
         override def actionPerformed(e: ActionEvent) = repainter.unsafeEval
      })
  }

  object Resizer extends ComponentAdapter {
    override def componentResized(e: ComponentEvent) = reshape
  }

  object FocusListener extends FocusListener {
    val value = Variable.create(false)

    override def focusGained(e: FocusEvent) = value.setValue(true)
    override def focusLost(e: FocusEvent) = value.setValue(false)
  }

  def hasfocus: Expression[Boolean] = FocusListener.value

  val view = new Viewport(viewDimensions)
  val grid = new Grid(view)
  val ruler = new Ruler(grid, view, panelDimensions)

  val borderStroke = new BasicStroke(2)

  //private Overlay overlay = new Overlay();

  var firstPaint = true

  /*def mouseListener: Option[GraphEditorTool] => GraphEditorMouseListener = {
    case Some(tool) => tool.mouseListener
    case None => DummyMouseListener
  }*/
  
   
  val toolbox = Toolbox(ToolEnvironment(view, hasfocus), editor.tools).unsafePerformIO
  
  val mListener = new ModelEditorMouseListener (view, hasfocus, toolbox.selectedToolMouseListener,() => {requestFocus()}.pure[IO])
  
  addMouseListener(mListener)
  addMouseMotionListener(mListener)
  addComponentListener(Resizer)
  addMouseWheelListener(mListener)
  addFocusListener(FocusListener)
  
  val kListener = new ModelEditorKeyListener(
      this,
      ModelEditorKeyListener.defaultBindings(this), 
      toolbox.selectedToolKeyBindings,
      toolbox.hotkeyBindings,
      logger 
      )
  
  addKeyListener(kListener)

  def reshape = panelDimensions.setValue(0, 0, getWidth, getHeight)
  
  val graphicalContent = for {
    settings <- CommonVisualSettings.settings;
    grid <- grid.graphicalContent;
    ruler <- ruler.graphicalContent;
    viewTransform <- view.transform
    tool <- toolbox.selectedToolInstance;
    hasFocus <- hasfocus;
    userSpaceContent <- tool.userSpaceContent
    screenSpaceContent <- tool.screenSpaceContent
  } yield GraphicalContent(g => {
    
    val screenTransform = new AffineTransform(g.getTransform)

    if (firstPaint) {
      reshape
      firstPaint = false
    }

    g.setBackground(settings.backgroundColor)
    g.clearRect(0, 0, getWidth(), getHeight())
    grid.draw(g)
    g.setTransform(screenTransform)

    g.transform(viewTransform)

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    // g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

    userSpaceContent.draw(GraphicalContent.cloneGraphics(g))

    g.setTransform(screenTransform)

    ruler.draw(g)
    
    if (hasFocus) {
      screenSpaceContent.draw(g)
      g.setTransform(screenTransform)

      g.setStroke(borderStroke)
      g.setColor(settings.foregroundColor)
      g.drawRect(0, 0, getWidth() - 1, getHeight() - 1)
    }

  })
  
   addComponentListener(new ComponentAdapter {
     override def componentShown (e:ComponentEvent) = { println ("shown!"); Repainter.timer.start; }
     override def componentHidden (e:ComponentEvent) = { println ("hidden"); Repainter.timer.stop; }
   })

  addHierarchyListener ( new HierarchyListener {
    override def hierarchyChanged (e: HierarchyEvent) = 
      if ((e.getChangeFlags & HierarchyEvent.SHOWING_CHANGED) != 0) {
	if (isShowing)
	  Repainter.timer.start
	else
	  Repainter.timer.stop
      }
  })

  override def paint(g: Graphics) = {
    val g2d = g.asInstanceOf[Graphics2D] // woohoo
    graphicalContent.unsafeEval.draw(g2d)
  }

  def fitView: IO[Unit] = {
    ioPure.pure { println ("BLA BLA") } >>=|
    view.fitAround (new Rectangle2D.Double (-5, -5, 10, 10))
  }
}

object ModelEditorPanel {

}
	
/*	public GraphEditorPanel(MainWindow mainWindow, WorkspaceEntry workspaceEntry) throws ServiceNotAvailableException {
		super (new BorderLayout());
		this.mainWindow = mainWindow;
		this.workspaceEntry = workspaceEntry;
		
		GraphEditable graphEditable = workspaceEntry.getModelEntry().getImplementation(GraphEditable.SERVICE_HANDLE);
		
		Repainter.start

		toolboxPanel = new ToolboxPanel(graphEditable.createTools(this));

		GraphEditorPanelMouseListener mouseListener = new GraphEditorPanelMouseListener(this, fmap(mouseListenerGetter, toolboxPanel.selectedTool()));
		GraphEditorPanelKeyListener keyListener = new GraphEditorPanelKeyListener(this, toolboxPanel);

		addMouseMotionListener(mouseListener);
		addMouseListener(mouseListener);
		addMouseWheelListener(mouseListener);
		
		GraphEditorFocusListener focusListener = new GraphEditorFocusListener();
		addFocusListener(focusListener);
		hasFocus = focusListener;
		
		addComponentListener(new Resizer());

		addKeyListener(keyListener);
		
		add(overlay, BorderLayout.CENTER);
		
		updatePropertyView(graphEditable.properties());
	}

	

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		
		GlobalCache.eval(graphicalContent).draw(g2d);
		
		paintChildren(g2d);
	}

	public Viewport getViewport() {
		return view;
	}

	public Point2D snap(Point2D point) {
		return new Point2D.Double(grid.snapCoordinate(point.getX()), grid.snapCoordinate(point.getY()));
	}
	
	public MainWindow getMainWindow() {
		return mainWindow;
	}
	
	private void updatePropertyView(Expression<? extends PVector<EditableProperty>> properties) {
		final PropertyEditorWindow propertyWindow = mainWindow.getPropertyView();
		
		propertyWindow.propertyObject.setValue(properties);
	}

	@Override
	public EditorOverlay getOverlay() {
		return overlay;
	}

	public ToolboxPanel getToolBox() {
		return toolboxPanel;
	}

	public WorkspaceEntry getWorkspaceEntry() {
		return workspaceEntry;
	}

	@Override
	public Function<Point2D, Point2D> snapFunction() {
		return new Function<Point2D, Point2D>() {
			@Override
			public Point2D apply(Point2D argument) {
				return snap(argument);
			}
		};	
	} 
}*/
