package org.workcraft.plugins.petri.tools;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.workcraft.util.GUI;
import org.workcraft.util.Nothing;

import pcollections.TreePVector;

interface SimulationControl<State> extends SimulationModel<Nothing, State> {
}

public class SimulationControlPanel<State> {
	private final JButton resetButton, autoPlayButton, stopButton, backButton, stepButton, loadTraceButton, saveMarkingButton, loadMarkingButton;
	private final JSlider speedSlider;

	private Timer timer = null;
	
	final SimulationControl<State> simControl;
	
	State savedState = null;

	final double DEFAULT_SIMULATION_DELAY = 0.3;
	final double EDGE_SPEED_MULTIPLIER = 10;
	private State initialState;

	private int getAnimationDelay() {
		return (int) (1000.0 * DEFAULT_SIMULATION_DELAY * Math.pow(EDGE_SPEED_MULTIPLIER, -speedSlider.getValue() / 1000.0));
	}
	
	public List<Component> components() {
		return TreePVector.<Component>empty()
			.plus(resetButton)
			.plus(speedSlider)
			.plus(autoPlayButton)
			.plus(stopButton)
			.plus(backButton)
			.plus(stepButton)
			.plus(loadTraceButton)
			.plus(saveMarkingButton)
			.plus(loadMarkingButton);
	}
	
	public SimulationControlPanel(final SimulationControl<State> simControl) {
		this.simControl = simControl;
		resetButton = new JButton("Reset");

		speedSlider = new JSlider(-1000, 1000, 0);
		autoPlayButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/start.svg"), "Automatic simulation");
		stopButton = new JButton("Stop");
		backButton = new JButton("Step <");
		stepButton = new JButton("Step >");
		loadTraceButton = new JButton("Load trace");
		saveMarkingButton = new JButton("Save marking");
		loadMarkingButton = new JButton("Load marking");

		speedSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (timer != null) {
					timer.stop();
					timer.setInitialDelay(getAnimationDelay());
					timer.setDelay(getAnimationDelay());
					timer.start();
				}
				update();
			}
		});
		
		initialState = simControl.saveState();

		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
		
		autoPlayButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timer = new Timer(getAnimationDelay(), new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						simControl.fire(Nothing.VALUE);
					}
				});
				timer.start();
				update();
			}
		});

		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timer.stop();
				timer = null;
				update();
			}
		});

		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				simControl.unfire(Nothing.VALUE);
			}
		});

		stepButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				simControl.fire(Nothing.VALUE);
			}
		});

		saveMarkingButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				savedState = simControl.saveState();
			}
		});

		loadMarkingButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				simControl.loadState(savedState);
			}
		});
	}
	
	public void reset() {
		simControl.loadState(initialState);
	}
	
	public void rememberInitialState() {
		initialState = simControl.saveState();
	}
	
	void update() {
		boolean atEnd = !simControl.canFire(Nothing.VALUE);
		boolean atStart = !simControl.canUnfire(Nothing.VALUE);
		
		if (timer != null && atEnd) {
			timer.stop();
			timer = null;
		}

		if (timer != null)
			timer.setDelay(getAnimationDelay());
		
		resetButton.setEnabled(!atStart);
		autoPlayButton.setEnabled(!atEnd);
		stopButton.setEnabled(timer != null);

		backButton.setEnabled(!atStart);

		stepButton.setEnabled(!atEnd);

		loadTraceButton.setEnabled(true);

		saveMarkingButton.setEnabled(true);
		loadMarkingButton.setEnabled(savedState != null);
	}

	public SimStateControl asStateControl() {
		return new SimStateControl() {
			
			@Override
			public void reset() {
				SimulationControlPanel.this.reset();
			}
			
			@Override
			public void rememberInitialState() {
				SimulationControlPanel.this.rememberInitialState();
			}
		};
	}
}
