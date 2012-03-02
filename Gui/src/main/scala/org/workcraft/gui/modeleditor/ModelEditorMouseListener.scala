package org.workcraft.gui.modeleditor
import java.awt.event.MouseMotionListener
import java.awt.event.MouseEvent
import java.awt.Point
import java.awt.geom.Point2D
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import scalaz._
import Scalaz._
import java.awt.event.MouseWheelEvent

class ModelEditorMouseListener(val viewport: Viewport, val hasFocus: Expression[Boolean], val requestFocus: () => IO[Unit])
  extends IOMouseMotionListener with IOMouseListener with IOMouseWheelListener {
  private var panDrag = false

  private var prevMouseCoords = new Point
  private var prevPosition = new Point2D.Double

  def rememberPosition(p: Point) =
    eval(viewport.screenToUser) >>= (f => {
      prevMouseCoords = p
      prevPosition.setLocation(f(new Point2D.Double(p.getX, p.getY)))
    }.pure[IO])

  def setPanDrag(value: Boolean) = { panDrag = value }.pure[IO]

  def mouseWheelMovedAction(e: MouseWheelEvent) = {
    val p = new Point2D.Double(e.getPoint.getX, e.getPoint.getY)
    viewport.zoomTo(-e.getWheelRotation(), p)
  }

  def mouseMovedAction(e: MouseEvent) = {
    val currentMouseCoords = e.getPoint

    (if (panDrag) {
      viewport.pan(currentMouseCoords.x - prevMouseCoords.x, currentMouseCoords.y - prevMouseCoords.y)
    } else {
      /* GraphEditorMouseListener toolMouseListener = eval(this.toolMouseListener);
			if(!toolMouseListener.isDragging() && startPosition!=null) {
				toolMouseListener.startDrag(adaptEvent(e));
			}
			toolMouseListener.mouseMoved(adaptEvent(e));*/
    }.pure[IO]) >>=| rememberPosition(currentMouseCoords)
  }

  def mouseDraggedAction(e: MouseEvent) = mouseMovedAction(e)

  def mouseClickedAction(e: MouseEvent) = {}.pure[IO]

  def mouseEnteredAction(e: MouseEvent) = {}.pure[IO]

  def mouseExitedAction(e: MouseEvent) = {}.pure[IO]

  def mousePressedAction(e: MouseEvent) =
    eval(hasFocus) >>=
      (focus => if (!focus) requestFocus() else {}.pure[IO]) >>=
      (_ => if (e.getButton() == MouseEvent.BUTTON2) setPanDrag(true) else {}.pure[IO])

  def mouseReleasedAction(e: MouseEvent) =
    if (e.getButton() == MouseEvent.BUTTON2)
      setPanDrag(false)
    else {}.pure[IO]

  /*eval(hasFocus) >>= ( focus => 
		if (!focus)
			requestFocus
		 
		if (e.getButton() != MouseEvent.BUTTON2)
			eval(toolMouseListener).mouseClicked(adaptEvent(e));
	}  */
}

/*
class GraphEditorPanelMouseListener implements MouseMotionListener, MouseListener, MouseWheelListener {
	protected GraphEditor editor;
	protected boolean panDrag = false;
	private Expression<GraphEditorMouseListener> toolMouseListener;

	protected Point lastMouseCoords = new Point();
	private Point2D prevPosition = new Point2D.Double(0, 0);
	private Point2D startPosition = null;
	
	public GraphEditorPanelMouseListener(GraphEditor editor, Expression<GraphEditorMouseListener> toolMouseListener) {
		this.editor = editor;
		this.toolMouseListener = toolMouseListener;
	}
import org.workcraft.scala.effects.IO

	private GraphEditorMouseEvent adaptEvent(MouseEvent e) {
		return new GraphEditorMouseEvent(editor, e, startPosition, prevPosition);
	}
	
	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}
	
	public void mouseMoved(MouseEvent e) {
		Point currentMouseCoords = e.getPoint();
		if (panDrag) {
			editor.getViewport().pan(currentMouseCoords.x - lastMouseCoords.x, currentMouseCoords.y - lastMouseCoords.y);
		} else {
			GraphEditorMouseListener toolMouseListener = eval(this.toolMouseListener);
			if(!toolMouseListener.isDragging() && startPosition!=null) {
				toolMouseListener.startDrag(adaptEvent(e));
			}
			toolMouseListener.mouseMoved(adaptEvent(e));
		}
		prevPosition = editor.getViewport().screenToUser(currentMouseCoords);
		lastMouseCoords = currentMouseCoords;
	}

	public void mouseClicked(MouseEvent e) {
		if (!editor.hasFocus())
			editor.getMainWindow().requestFocus((GraphEditorPanel)editor);
		 
		if (e.getButton() != MouseEvent.BUTTON2)
			eval(toolMouseListener).mouseClicked(adaptEvent(e));
	}

	public void mouseEntered(MouseEvent e) {
		if (editor.hasFocus()) {
			eval(toolMouseListener).mouseEntered(adaptEvent(e));
		}
	}

	public void mouseExited(MouseEvent e) {
		if (editor.hasFocus())
			eval(toolMouseListener).mouseExited(adaptEvent(e));
	}

	public void mousePressed(MouseEvent e) {
		if (!editor.hasFocus())
			editor.getMainWindow().requestFocus((GraphEditorPanel)editor);
		
		if (e.getButton() == MouseEvent.BUTTON2)
			panDrag = true;
		else {
			GraphEditorMouseListener toolML = eval(toolMouseListener);
			if(!toolML.isDragging())
				startPosition = editor.getViewport().screenToUser(e.getPoint());
			toolML.mousePressed(adaptEvent(e));
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2)
			panDrag = false;
		else {
			GraphEditorMouseListener toolML = eval(toolMouseListener);
			if(toolML.isDragging())
				toolML.finishDrag(adaptEvent(e));
			toolML.mouseReleased(adaptEvent(e));
			startPosition = null;
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		if (editor.hasFocus()) {
			editor.getViewport().zoom(-e.getWheelRotation(), e.getPoint());
		}
	}
}
*/
 
//object DummyMouseListener extends GraphEditorMouseListener