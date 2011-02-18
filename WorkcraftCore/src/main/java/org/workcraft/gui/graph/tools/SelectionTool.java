package org.workcraft.gui.graph.tools;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.Collection;

import javax.swing.Icon;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.MovableHelper;
import org.workcraft.dom.visual.MovableNew;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualModelTransformer;
import org.workcraft.dom.visual.connections.DefaultAnchorGenerator;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.gui.graph.tools.GenericSelectionTool.DragHandler;
import org.workcraft.util.GUI;
import org.workcraft.util.Hierarchy;

import pcollections.HashTreePSet;
import pcollections.PSet;

public class SelectionTool extends AbstractTool {

	private final GenericSelectionTool<Node> selectionTool;
	private DefaultAnchorGenerator anchorGenerator = new DefaultAnchorGenerator();
	private final GraphEditor editor;
	
	@Override
	public void mouseClicked(GraphEditorMouseEvent e) {
		selectionTool.mouseClicked(e);
		anchorGenerator.mouseClicked(e);
	}
	
	public SelectionTool(final GraphEditor editor) {
		this.editor = editor;
		selectionTool = new GenericSelectionTool<Node>(
				editor.getModel().selection(),
				new GenericSelectionTool.HitTester<Node>() {
			@Override
			public Node hitTest(Point2D point) {
				return HitMan.hitTestForSelection(point, editor.getModel());
			}

			@Override
			public PSet<Node> boxHitTest(Point2D boxStart, Point2D boxEnd) {
				return HashTreePSet.from(editor.getModel().boxHitTest(boxStart, boxEnd));
			}
		}, new DragHandler() {

			@Override
			public void drag(double dx, double dy) {
				offsetSelection(dx, dy);
			}
		});
	}
	
	protected void currentLevelDown(VisualModel model) {
		Collection<? extends Node> selection = eval(model.selection());
		if(selection.size() == 1)
		{
			Node selectedNode = selection.iterator().next();
			if(selectedNode instanceof Container)
				model.currentLevel().setValue((Container)selectedNode);
		}
	}

	protected void currentLevelUp(VisualModel model) {
		Container level = eval(model.currentLevel());
		Container parent = Hierarchy.getNearestAncestor(GlobalCache.eval(level.parent()), Container.class);
		if(parent!=null)
		{
			model.currentLevel().setValue(parent);
			model.selection().setValue(HashTreePSet.<Node>singleton(level));
		}
	}

	private void offsetSelection(double dx, double dy) {
		
		for(Node node : GlobalCache.eval(selectionTool.selection)) {
			if(node instanceof MovableNew) {
				MovableHelper.translate((MovableNew) node, dx, dy);
			}
		}
	}

	protected Color grayOutColor = Color.LIGHT_GRAY; 
	
	
	@Override
	public String getLabel() {
		return "Select";
	}
	
	@Override
	public int getHotKeyCode() {
		return KeyEvent.VK_S;
	}
	
	@Override
	public void keyPressed(GraphEditorKeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			e.getModel().deleteSelection();
		}

		if (!e.isCtrlDown())
		{
			if (!e.isShiftDown()) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_PAGE_UP:
					currentLevelUp(e.getModel());
					break;
				case KeyEvent.VK_PAGE_DOWN:
					currentLevelDown(e.getModel());
					break;
				case KeyEvent.VK_OPEN_BRACKET:
					VisualModelTransformer.rotateSelection(e.getEditor(), e.getModel(),-Math.PI/2);
					break;
				case KeyEvent.VK_CLOSE_BRACKET:
					VisualModelTransformer.rotateSelection(e.getEditor(), e.getModel(),Math.PI/2);
					break;
				case KeyEvent.VK_LEFT:
					VisualModelTransformer.translateSelection(e.getModel(), -1,0);
					break;
				case KeyEvent.VK_RIGHT:
					VisualModelTransformer.translateSelection(e.getModel(), 1,0);
					break;
				case KeyEvent.VK_UP:
					VisualModelTransformer.translateSelection(e.getModel(),0,-1);
					break;
				case KeyEvent.VK_DOWN:
					VisualModelTransformer.translateSelection(e.getModel(),0,1);
					break;
				}
			} else { // Shift is pressed

				switch (e.getKeyCode()) {
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_RIGHT:
					VisualModelTransformer.scaleSelection(e.getModel(),-1,1);
					break;
				case KeyEvent.VK_UP:
				case KeyEvent.VK_DOWN:
					VisualModelTransformer.scaleSelection(e.getModel(),1,-1);
					break;
				}
			}
		}

		if (e.isCtrlDown()) {
			switch(e.getKeyCode()){
			case KeyEvent.VK_G: 
				e.getModel().groupSelection();
				break;
			case KeyEvent.VK_U:
				e.getModel().ungroupSelection();
				break;
			case KeyEvent.VK_C: 
				break;
			case KeyEvent.VK_X: 
				break;
			case KeyEvent.VK_V:
				e.getModel().selection().setValue(HashTreePSet.<Node>empty());
				//addToSelection(e.getModel(), e.getModel().paste(Toolkit.getDefaultToolkit().getSystemClipboard(), prevPosition));
				//e.getModel().fireSelectionChanged();
			case KeyEvent.VK_P:
				e.getEditor().getModelEntry().getStorage().checkpoint();
				break;
			case KeyEvent.VK_Z:
				e.getEditor().getModelEntry().getStorage().undo();
				break;
			case KeyEvent.VK_Y:
				e.getEditor().getModelEntry().getStorage().redo();
				break;
			}
		}
	}
	
	@Override
	public Icon getIcon() {
		return GUI.createIconFromSVG("images/icons/svg/select.svg");
	}

	protected static Color selectionColor = new Color(99, 130, 191).brighter();

	@Override
	public void finishDrag(GraphEditorMouseEvent e) {
		selectionTool.finishDrag(e);
	}
	
	@Override
	public boolean isDragging() {
		return selectionTool.isDragging();
	}
	
	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		selectionTool.mousePressed(e);
	}
	
	@Override
	public void mouseMoved(GraphEditorMouseEvent e) {
		selectionTool.mouseMoved(e);
	}
	
	@Override
	public void startDrag(GraphEditorMouseEvent e) {
		selectionTool.startDrag(e);
	}

	@Override
	public Expression<GraphicalContent> userSpaceContent(final Expression<Boolean> hasFocus) {
		return selectionTool.userSpaceContent(editor.getViewport());
	}

	@Override
	public Expression<? extends GraphicalContent> screenSpaceContent(final Viewport viewport, final Expression<Boolean> hasFocus) {
		return Expressions.constant(GraphicalContent.empty);
	}
}
