package org.workcraft.plugins.petri.tools;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.workcraft.Trace;
import org.workcraft.util.Function2;
import org.workcraft.util.Nothing;
import org.workcraft.util.Pair;

import pcollections.TreePVector;

public class SimulationTraceTable<State> {

	private TracePair traces = TracePair.createEmpty();

	Pair<Trace, Integer> getCurrentTrace() {
		if(traces.branchTrace.isEmpty())
			return Pair.of(traces.trace, traces.traceStep);
		else
			return Pair.of(traces.branchTrace, traces.branchStep);
	}
	
	public SimulationControl<SimulationState<State>> getSimControl() {
		return new SimulationControl<SimulationState<State>>() {
	
			@Override
			public boolean canFire(Nothing event) {
				Pair<Trace, Integer> curTrace = getCurrentTrace();
				Trace trace = curTrace.getFirst();
				int step = curTrace.getSecond();
				
				return step < trace.size() && model.canFire(trace.get(step));
			}
	
			@Override	
			public void fire(Nothing event) {
				quietStep();
			}
	
			@Override
			public boolean canUnfire(Nothing event) {
				Pair<Trace, Integer> curTrace = getCurrentTrace();
				Trace trace = curTrace.getFirst();
				int step = curTrace.getSecond();

				return  step > 0 && model.canUnfire(trace.get(step-1));
			}

			@Override
			public void unfire(Nothing event) {
				quietStepBack();
			}

			
			@Override
			public SimulationState<State> saveState() {
				return new SimulationState<State>(model.saveState(), traces.clone());
			}

			@Override
			public void loadState(SimulationState<State> state) {
				model.loadState(state.modelState);
				traces = state.traces.clone();
			}
		};
	};

	
	public SimulationTraceTable(SimulationModel<String, State> model, Function2<String, Boolean, Component> cellRenderer) {
		this.model = model;
		traceTable = create();
		
		loadFromClipboardButton = new JButton("from Clipb");
		saveToClipboardButton = new JButton("to Clipb");
		

		saveToClipboardButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TracesClipboardOwner.saveToClipboard(traces);
			}

		});

		loadFromClipboardButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				traces = TracesClipboardOwner.loadFromClipboard();
			}
		});
		
		this.cellRenderer = cellRenderer;
	}
	
	
	final SimulationModel<String, State> model;
	
	private final JTable traceTable;
	private JButton saveToClipboardButton, loadFromClipboardButton;

	public List<Component> components() {
		return TreePVector.<Component>empty()
			.plus(traceTable)
			.plus(saveToClipboardButton)
			.plus(loadFromClipboardButton);
	}

	private boolean quietStepBack() {
		if (traces.branchStep > 0) {
			String event = traces.branchTrace.get(traces.branchStep - 1);

			if(!model.canUnfire(event))
				return false;
			
			model.unfire(event);
			
			traces.branchStep--;

			if (traces.branchStep == 0 && !traces.trace.isEmpty())
				traces.branchTrace.clear();
			return true;
		}

		if (traces.traceStep == 0)
			return false;

		String event = traces.trace.get(traces.traceStep - 1);
		
		if(!model.canUnfire(event))
			return false;
		model.unfire(event);
		
		traces.traceStep--;
		
		return true;
	}

	private boolean quietStep() {
		if(!traces.branchTrace.isEmpty()) { // If we have a branch-trace, use that. Otherwise, use the main trace.
			if (traces.branchStep < traces.branchTrace.size()) {
				String event = traces.branchTrace.get(traces.branchStep);
				
				if(!model.canFire(event))
					return false;
				model.fire(event);
				traces.branchStep++;

				return true;
			}
			else
				return false;
		}
		else {
			if (traces.traceStep == traces.trace.size())
				return false;
	
			String event = traces.trace.get(traces.branchStep);
			
			if(!model.canFire(event))
				return false;
			model.fire(event);
	
			traces.traceStep++;
			return true;
		}
	}
	
	public void userRequestedFire(String event) {
		String nextEvent = getNextEvent();
		
		if (!event.equals(nextEvent)) {
			while (traces.branchStep < traces.branchTrace.size())
				traces.branchTrace.remove(traces.branchStep);
	
			traces.branchTrace.add(event);
		}
		
		quietStep();
	}

	final Function2<String, Boolean, Component> cellRenderer;
	
	private JTable create() {
		final JTable traceTable = new JTable(new AbstractTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public int getColumnCount() {
				return 2;
			}

			@Override
			public String getColumnName(int column) {
				if (column == 0)
					return "Trace";
				return "Branch";
			}

			@Override
			public int getRowCount() {
				int tnum = traces.trace.size();
				int bnum = traces.branchTrace.size();

				return Math.max(tnum, bnum + traces.traceStep);
			}

			@Override
			public Object getValueAt(int row, int col) {
				if (col == 0) {
					if (!traces.trace.isEmpty() && row < traces.trace.size())
						return traces.trace.get(row);
				} else {
					if (!traces.branchTrace.isEmpty() && row >= traces.traceStep && row < traces.traceStep + traces.branchTrace.size()) {
						return traces.branchTrace.get(row - traces.traceStep);
					}
				}
				return "";
			}
		});
		
		traceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		traceTable.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				int column = traceTable.getSelectedColumn();
				int row = traceTable.getSelectedRow();

				if (column == 0) {
					if (traces.trace != null && row < traces.trace.size()) {

						boolean work = true;

						while (traces.branchStep > 0 && work)
							work = quietStepBack();
						while (traces.traceStep > row && work)
							work = quietStepBack();
						while (traces.traceStep < row && work)
							work = quietStep();
					}
				} else {
					if (traces.branchTrace != null && row >= traces.traceStep && row < traces.traceStep + traces.branchTrace.size()) {

						boolean work = true;
						while (traces.traceStep + traces.branchStep > row && work)
							work = quietStepBack();
						while (traces.traceStep + traces.branchStep < row && work)
							work = quietStep();
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {

			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}

		});

		traceTable.setDefaultRenderer(Object.class, new TableCellRenderer() {
			boolean isActive(int row, int column) {
				if (column == 0) {
					if (traces.branchTrace.isEmpty())
						return row == traces.traceStep;
				} else {
					if (row >= traces.traceStep && row < traces.traceStep + traces.branchTrace.size()) {
						return (row - traces.traceStep) == traces.branchStep;
					}
				}

				return false;
			}

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				if (!(value instanceof String))
					return null;
				return cellRenderer.apply((String) value, isActive(row, column));
			}
		});
		return traceTable;
	}

	public void setTraces(TracePair traces) {
		this.traces = traces;
	}


	public String getNextEvent() {
		Pair<Trace, Integer> currentTrace = getCurrentTrace();
		int i = currentTrace.getSecond();
		Trace t = currentTrace.getFirst();
		if(t.size() == i)
			return null;
		else
			return t.get(i);
	}
	
	public SimControl<String> asSimControl() {
		return new SimControl<String>() {
			
			@Override
			public void unfire() {
				SimulationTraceTable.this.quietStepBack();
			}
			
			@Override
			public String getNextEvent() {
				return SimulationTraceTable.this.getNextEvent();
			}
			
			@Override
			public void fire(String event) {
				userRequestedFire(event);
			}
			
			@Override
			public boolean canFire(String event) {
				return model.canFire(event);
			}
		};
	}
}
