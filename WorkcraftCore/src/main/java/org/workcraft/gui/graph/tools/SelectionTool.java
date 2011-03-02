package org.workcraft.gui.graph.tools;

import java.awt.Color;
import java.awt.event.KeyEvent;

import javax.swing.Icon;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.util.GUI;

public class SelectionTool extends AbstractTool implements DecorationProvider<Colorisator> {

	private final GenericSelectionTool<Node> selectionTool;
	private final Expression<Node> currentLevel;

	
	@Override
	public void mouseClicked(GraphEditorMouseEvent e) {
		selectionTool.mouseClicked(e);
	}
	
	public SelectionTool(SelectionToolConfig<Node> config) {
		this.currentLevel = config.currentEditingLevel();
		selectionTool = new GenericSelectionTool<Node>(
				config.selection(),
				config.hitTester(),
				config.movableController(), 
				config.snap());
	}
	

	protected Color grayOutColor = Color.LIGHT_GRAY; 

	
	@Override
	public Colorisator getDecoration() {
		return getColorisator();
	}
	
	public Colorisator getColorisator() {

		Colorisation greyOutColourisation = new Colorisation(){
			@Override
			public Color getColorisation() {
				return grayOutColor;
			}

			@Override
			public Color getBackground() {
				return null;
			}
		};
		return new HierarchicalColorisator(greyOutColourisation) {
			
			@Override
			public Expression<Colorisation> getSimpleColorisation(final Node node) {
				return new ExpressionBase<Colorisation>(){

					@Override
					protected Colorisation evaluate(final EvaluationContext context) {
						if(node == context.resolve(currentLevel))
							return Colorisation.EMPTY;
						
						Colorisation selectedDecoration = new Colorisation() {
	
							@Override
							public Color getColorisation() {
								return selectionColor;
							}
	
							@Override
							public Color getBackground() {
								return null;
							}
						};
						
						if(context.resolve(selectionTool.effectiveSelection).contains(node))
							return selectedDecoration;
						else
							return null;
					}
				};
			}
		};
	}
	
	@Override
	public String getLabel() {
		return "Select";
	}
	
	@Override
	public int getHotKeyCode() {
		return KeyEvent.VK_S;
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
	public Expression<? extends GraphicalContent> userSpaceContent(Viewport viewport, final Expression<Boolean> hasFocus) {
		return selectionTool.userSpaceContent(viewport);
	}

	@Override
	public Expression<? extends GraphicalContent> screenSpaceContent(final Viewport viewport, final Expression<Boolean> hasFocus) {
		return Expressions.constant(GraphicalContent.EMPTY);
	}
}
