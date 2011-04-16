package org.workcraft.gui.graph.tools.selection;

import java.awt.Color;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.gui.graph.tools.Colorisation;
import org.workcraft.gui.graph.tools.Colorisator;
import org.workcraft.gui.graph.tools.DecorationProvider;
import org.workcraft.gui.graph.tools.DummyKeyListener;
import org.workcraft.gui.graph.tools.GraphEditorKeyListener;
import org.workcraft.gui.graph.tools.GraphEditorMouseListener;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.HierarchicalColorisator;
import org.workcraft.util.GUI;

public class SelectionTool implements GraphEditorTool, DecorationProvider<Colorisator> {

	private final GenericSelectionTool<Node> selectionTool;
	private final Expression<? extends Node> currentLevel;
	
	@Override
	public GraphEditorMouseListener mouseListener() {
		return selectionTool.getMouseListener();
	}
	
	public SelectionTool(SelectionToolConfig<Node> config) {
		this.currentLevel = config.currentEditingLevel();
		selectionTool = new GenericSelectionTool<Node>(
				config.selection(),
				config.hitTester(),
				new MoveDragHandler<Node>(config.selection(), config.movableController(), config.snap())
			);
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
						
						if(context.resolve(selectionTool.effectiveSelection()).contains(node))
							return selectedDecoration;
						else
							return null;
					}
				};
			}
		};
	}
	
	public static Identification identification = new Identification() {
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
	};
	
	protected static Color selectionColor = new Color(99, 130, 191).brighter();

	@Override
	public Expression<? extends GraphicalContent> userSpaceContent(Viewport viewport, final Expression<Boolean> hasFocus) {
		return selectionTool.userSpaceContent(viewport);
	}

	@Override
	public Expression<? extends GraphicalContent> screenSpaceContent(final Viewport viewport, final Expression<Boolean> hasFocus) {
		return Expressions.constant(GraphicalContent.EMPTY);
	}

	@Override
	public GraphEditorKeyListener keyListener() {
		return DummyKeyListener.INSTANCE;
	}

	@Override
	public void activated() {
	}

	@Override
	public void deactivated() {
	}

	@Override
	public JPanel getInterfacePanel() {
		return null;
	}

	@Override
	public Identification getIdentification() {
		return identification;
	}
}
