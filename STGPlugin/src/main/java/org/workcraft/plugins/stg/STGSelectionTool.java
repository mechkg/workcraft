package org.workcraft.plugins.stg;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;
import static org.workcraft.util.Maybe.Util.*;

import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.TouchableProvider;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.EditorOverlay;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.HitTester;
import org.workcraft.gui.graph.tools.selection.SelectionTool;
import org.workcraft.gui.graph.tools.selection.SelectionToolConfig;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.util.Function;

public class STGSelectionTool extends SelectionTool
{
	private final TouchableProvider<Node> touchableProvider;
	private final HitTester<VisualNode> hitTester;
	private final GraphEditor editor;
	private STG stg;

	public STGSelectionTool(STG stg, final GraphEditor editor, TouchableProvider<Node> touchableProvider, HitTester<VisualNode> hitTester, StgEditorState editorState) {
		super(new SelectionToolConfig.Default(hitTester, new Function<Point2D, Point2D>() {
			@Override
			public Point2D apply(Point2D argument) {
				return editor.snap(argument);
			}
		}, editorState.selection, editorState.currentLevel));
		this.stg = stg;
		this.editor = editor;
		this.touchableProvider = touchableProvider;
		this.hitTester = hitTester;
	}

	
	private static void editInPlace (final EditorOverlay overlay, final STG model, final Viewport viewport, final TouchableProvider<Node> touchable, final VisualComponent t, String initialText) {
		Rectangle2D bb = eval(TouchableProvider.Util.podgonHideMaybe(touchable).apply(t)).getBoundingBox();
		Rectangle r = viewport.userToScreen(bb);

		final JTextField text = new JTextField();

		if (initialText != null)
			text.setText(initialText);
		else
			text.setText(eval(model.referenceManager()).getNodeReference(t.getReferencedComponent()));

		text.setFont(text.getFont().deriveFont( Math.max(12.0f, (float)r.getHeight()*0.7f)));
		text.selectAll();

		text.setBounds(r.x, r.y, Math.max(r.width, 60), Math.max(r.height, 18));

		overlay.add(text);
		text.requestFocusInWindow();
		
		final boolean [] cancelEdit = new boolean [1];;
		cancelEdit[0] = false;
		


		text.addKeyListener( new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					cancelEdit[0] = false;
					text.getParent().remove(text);
				}
				else if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
					cancelEdit[0] = true;
					text.getParent().remove(text);
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
			}

		});

		text.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				if (text.getParent() != null)
					text.getParent().remove(text);

				final String newName = text.getText();

				if (!cancelEdit[0])
					try {
						model.setName(t.getReferencedComponent(), newName);
					} catch (ArgumentException e) {
						JOptionPane.showMessageDialog(null, e.getMessage());
						editInPlace(overlay, model, viewport, touchable, t, newName);
					}
				
				//editor.repaint();
			}
		});
	}

	@Override
	public void mouseClicked(GraphEditorMouseEvent e)
	{
		super.mouseClicked(e);

		if(e.getButton()==MouseEvent.BUTTON1 && e.getClickCount() > 1) {
			VisualNode node = hitTester.hitTest(e.getPosition());
			if (node != null)
			{
				if(node instanceof VisualPlace)
				{
					VisualPlace place = (VisualPlace) node;
					if (eval(place.tokens())==1)
						place.tokens().setValue(0);
					else if (eval(place.tokens())==0)
						place.tokens().setValue(1);
				} else if (node instanceof VisualImplicitPlaceArc) {
					STGPlace place = ((VisualImplicitPlaceArc) node).getImplicitPlace();
					if (eval(place.tokens())==1)
						place.tokens().setValue(0);
					else if (eval(place.tokens())==0)
						place.tokens().setValue(1);
				} else if (node instanceof VisualSignalTransition || node instanceof VisualDummyTransition) {
					editInPlace(editor.getOverlay(), stg, editor.getViewport(), touchableProvider, (VisualComponent)node, null);
				}

			}

		} else if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
			VisualNode node = hitTester.hitTest(e.getPosition());
			JPopupMenu popup = createPopupMenu(node);
			if (popup!=null)
				popup.show(e.getSystemEvent().getComponent(), e.getSystemEvent().getX(), e.getSystemEvent().getY());
		}
	}
	
	@Override
	public void keyPressed(GraphEditorKeyEvent e) {
		super.keyPressed(e);
	}

	private JPopupMenu createPopupMenu(VisualNode node) {
		JPopupMenu popup = new JPopupMenu();
		
		if (node instanceof VisualPlace) {
			popup.setFocusable(false);
			popup.add(new JLabel("Place"));
			popup.addSeparator();
			popup.add(new JMenuItem("Add token"));
			popup.add(new JMenuItem("Remove token"));
			return popup;
		}
		
		return null; 
	}
}
