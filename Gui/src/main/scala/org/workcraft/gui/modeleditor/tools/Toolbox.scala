package org.workcraft.gui.modeleditor.tools

import org.workcraft.dependencymanager.advanced.user.Variable
import org.workcraft.gui.modeleditor.HotkeyBinding
import org.workcraft.scala.Expressions.convertModifiableExpression
import org.workcraft.scala.Expressions.monadicSyntaxV
import org.workcraft.scala.Expressions.Expression
import scalaz._
import Scalaz._
import org.workcraft.scala.Expressions.ModifiableExpression
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._

class Toolbox(val env: ToolEnvironment, val tools: NonEmptyList[ModelEditorTool], val selected: ModifiableExpression[ModelEditorTool], val selectedInstance: ModifiableExpression[ModelEditorToolInstance]) {
  val selectedTool: Expression[ModelEditorTool] = selected.expr
  val selectedToolInstance = selectedInstance.expr
  
  def selectTool(tool: ModelEditorTool) = ((tool.createInstance(env)) >>= (i => selectedInstance.set(i))) >>=| selected.set(tool)
  
  def selectToolWithInstance (tool: ModelEditorTool, instance: (ToolEnvironment => IO[ModelEditorToolInstance])) =
    (instance(env) >>= ( i => selectedInstance.set(i))) >>=| selected.set(tool)

  private val hotkeys = tools.list.flatMap(t => t.button.hotkey.map((t, _))).groupBy(_._2).mapValues(_.map(_._1))
  private val cyclicHotkeyIterator = hotkeys.mapValues(Stream.continually(_).flatten.iterator)

  val hotkeyBindings = hotkeys.keys.map(key => HotkeyBinding(key, selectTool(cyclicHotkeyIterator(key).next))).toList

  val selectedToolKeyBindings = selectedToolInstance.map(_.keyBindings)
  val selectedToolMouseListener = selectedToolInstance.map(_.mouseListener)
}

object Toolbox {
  def apply(env: ToolEnvironment, tools: NonEmptyList[ModelEditorTool]): IO[Toolbox] = for {
    inst <- tools.head.createInstance(env);
    selected <- newVar(tools.head);
    selectedInstance <- newVar(inst)
  } yield (new Toolbox(env, tools, selected, selectedInstance))
}

/*

public class ToolboxPanel extends JPanel implements GraphEditorKeyListener {
	private final ModifiableExpression<GraphEditorTool> selectedTool = Variable.<GraphEditorTool>create(null);

	private HashMap<GraphEditorTool, JToggleButton> buttons = new HashMap<GraphEditorTool, JToggleButton>();
	private HashMap<Integer, ToolTracker> hotkeyMap = new HashMap<Integer, ToolTracker>();

	public void addTool (final GraphEditorTool tool, boolean selected) {
		JToggleButton button = new JToggleButton();

		button.setFocusable(false);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setSelected(selected);
		button.setMargin(new Insets(0,0,0,0));
		
		Insets insets = button.getInsets();
		int iconSize = eval(CommonVisualSettings.iconSize); // TODO: make the size update appropriately
		int minSize = iconSize+Math.max(insets.left+insets.right, insets.top+insets.bottom);
		
		Button identification = tool.getButton();
		
		Icon icon = identification.getIcon();
		if(icon==null) {
			button.setText(identification.getLabel());
			button.setPreferredSize(new Dimension(120,minSize));
		}
		else {
			BufferedImage crop = new BufferedImage(iconSize, iconSize,
					BufferedImage.TYPE_INT_ARGB);
			icon.paintIcon(button, crop.getGraphics(), (iconSize-icon.getIconWidth())/2, (iconSize-icon.getIconHeight())/2);
			button.setIcon(new ImageIcon(crop));
			button.setPreferredSize(new Dimension(minSize,minSize));
		}
	
		
		int hotKeyCode = identification.getHotKeyCode(); 
		if ( hotKeyCode != -1)
			button.setToolTipText("["+Character.toString((char)hotKeyCode)+"] " + identification.getLabel());
		else
			button.setToolTipText(identification.getLabel());

		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectTool(tool);
			}
		});

		buttons.put(tool, button);
		
		if (hotKeyCode != -1) {
			ToolTracker tracker = hotkeyMap.get(hotKeyCode);
			if (tracker == null) {
				tracker = new ToolTracker();
				hotkeyMap.put(hotKeyCode, tracker);
			}
			tracker.addTool(tool);
		}

		this.add(button);
		
		if (selected)
			selectTool(tool);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends GraphEditorTool> T getToolInstance (Class<T> cls)
	{
		for (GraphEditorTool tool : buttons.keySet())
			if (cls.isInstance(tool))
				return (T)tool;
		return null;
	}
	
	public void selectTool(GraphEditorTool tool) {
		if (eval(selectedTool) != null) {
			ToolTracker oldTracker = hotkeyMap.get(eval(selectedTool).getButton().getHotKeyCode());
			if(oldTracker!=null)
				oldTracker.reset();
			
			eval(selectedTool).deactivated();
			buttons.get(eval(selectedTool)).setSelected(false);
		}

		ToolTracker tracker = hotkeyMap.get(tool.getButton().getHotKeyCode());
		if (tracker != null)
			tracker.track(tool);
		
		tool.activated();
		controlPanel.setTool(tool);
		buttons.get(tool).setSelected(true);
		selectedTool.setValue(tool);
	}

	public ToolboxPanel(Iterable<? extends GraphEditorTool> tools) {
		this.setFocusable(false);

		selectedTool.setValue(null);

		setLayout(new SimpleFlowLayout (5, 5));
		
		for(GraphEditorTool tool : tools)
			addTool(tool, false);
		selectTool(tools.iterator().next());
		
		doLayout();
		this.repaint();
	}

	public Expression<GraphEditorTool> selectedTool() {
		return selectedTool;
	}

	public void keyPressed(GraphEditorKeyEvent event) {
		if (!event.isAltDown() && !event.isCtrlDown() && !event.isShiftDown()) {
			int keyCode = event.getKeyCode();
			ToolTracker tracker = hotkeyMap.get(keyCode);
			if (tracker != null)
				selectTool(tracker.getNextTool());
			else
				eval(selectedTool).keyListener().keyPressed(event);
		} else
			eval(selectedTool).keyListener().keyPressed(event);
	}

	public void keyReleased(GraphEditorKeyEvent event) {
		eval(selectedTool).keyListener().keyReleased(event);
		
	}

	public void keyTyped(GraphEditorKeyEvent event) {
		eval(selectedTool).keyListener().keyTyped(event);
	}

	ToolInterfaceWindow controlPanel = new ToolInterfaceWindow();
	
	public ToolInterfaceWindow getControlPanel() {
		return controlPanel;
	}
}*/
