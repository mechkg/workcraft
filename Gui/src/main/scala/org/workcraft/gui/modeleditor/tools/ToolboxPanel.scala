package org.workcraft.gui.modeleditor.tools
import javax.swing.JPanel
import org.workcraft.dependencymanager.advanced.user.Variable
import javax.swing.JToggleButton
import javax.swing.SwingConstants
import java.awt.Insets
import org.workcraft.scala.Expressions._
import org.workcraft.gui.CommonVisualSettings
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import java.awt.Dimension
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import java.awt.FlowLayout
import java.awt.BorderLayout
import org.workcraft.gui.NotAvailablePanel
import org.workcraft.gui.modeleditor.HotkeyBinding

import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import scalaz._
import Scalaz._

class ToolboxPanel(toolbox: Toolbox) extends JPanel {
  private val buttons = toolbox.tools.map(t => (t, createButton(t)))
  
  private val refresh = swingAutoRefresh(toolbox.selectedTool, (tool: ModelEditorTool) => ioPure.pure {
    buttons.foreach(_._2.setSelected(false))
    buttons.list.find( _._1 == tool).foreach(_._2.setSelected(true))
  }) 
  
  setFocusable(false)
  
  setLayout(new FlowLayout(FlowLayout.LEADING))
  buttons.foreach(tb => add(tb._2))
  
  buttons.list.find( _._1 == toolbox.selectedTool.unsafeEval).foreach(_._2.setSelected(true))
  
  def selectTool (tool: ModelEditorTool) = {
    toolbox.selectTool(tool).unsafePerformIO    
  }

  def createButton(tool: ModelEditorTool) : JToggleButton = {
    val button = new JToggleButton()

    button.setFocusable(false);
    button.setHorizontalAlignment(SwingConstants.LEFT)
    button.setMargin(new Insets(0, 0, 0, 0))

    val insets = button.getInsets()
    val iconSize = CommonVisualSettings.settings.unsafeEval.iconSize // TODO: make the size update appropriately
    val minSize = iconSize + Math.max(insets.left + insets.right, insets.top + insets.bottom)

    tool.button.icon match {
      case Some(icon) => {
        val crop = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB)
        icon.paintIcon(button, crop.getGraphics(), (iconSize - icon.getIconWidth()) / 2, (iconSize - icon.getIconHeight()) / 2)
        button.setIcon(new ImageIcon(crop))
        button.setPreferredSize(new Dimension(minSize, minSize))
      }
      case None => {
        button.setText(tool.button.label)
        button.setPreferredSize(new Dimension(120, minSize))
      }
    }

    tool.button.hotkey match {
      case Some(key) => {
        button.setToolTipText("[" + Character.toString(key.toChar) + "] " + tool.button.label)
      }
      case None => {
        button.setToolTipText(tool.button.label)
      }
    }

    button.addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) {
        selectTool(tool)
      }
    })
    
    button
  }
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