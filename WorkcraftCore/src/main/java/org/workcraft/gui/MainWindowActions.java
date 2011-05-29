package org.workcraft.gui;

import org.workcraft.Framework;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.actions.Action;
import org.workcraft.interop.ServiceNotAvailableException;

public class MainWindowActions {
	public static final Action CREATE_WORK_ACTION = new Action() {
		@Override public void run(Framework f) {
			try { f.getMainWindow().createWork(); } catch (OperationCancelledException e) { }
		}
		@Override public String getText() {
			return "Create work...";
		};
	};
	public static final Action OPEN_WORK_ACTION = new Action() {
		@Override public void run(Framework f) {
			try { f.getMainWindow().openWork(); } catch (OperationCancelledException e) { }
		}
		@Override public String getText() {
			return "Open work...";
		};
	};
	public static final Action SAVE_WORK_ACTION = new Action() {
		@Override public void run(Framework f) {
			try { f.getMainWindow().saveWork(); } catch (OperationCancelledException e) { } catch (ServiceNotAvailableException e2) {
				// TODO: something better
				e2.printStackTrace();
			}
		}
		@Override public String getText() {
			return "Save work";
		};
	};
	public static final Action SAVE_WORK_AS_ACTION = new Action() {
		@Override public void run(Framework f) {
			try { f.getMainWindow().saveWorkAs(); } catch (OperationCancelledException e) { } catch (ServiceNotAvailableException e2) {
				// TODO: something better
				e2.printStackTrace();
			}
		}
		public String getText() {
			return "Save work as...";
		};
	};
	public static final Action CLOSE_ACTIVE_EDITOR_ACTION = new Action() {
		@Override public void run(Framework f) {
			try { f.getMainWindow().closeActiveEditor(); } catch (OperationCancelledException e) { }
		}
		public String getText() {
			return "Close active work";
		};
	};	

	public static final Action CLOSE_ALL_EDITORS_ACTION = new Action() {
		@Override public void run(Framework f) {
			try { f.getMainWindow().closeEditorWindows(); } catch (OperationCancelledException e) { }
		}
		public String getText() {
			return "Close all works";
		};
	};	
	public static final Action EXIT_ACTION = new Action() {
		@Override public void run(Framework f) {
			f.shutdown();
		}
		public String getText() {
			return "Exit";
		};
	};
	public static final Action SHUTDOWN_GUI_ACTION = new Action() {
		@Override public void run(Framework f) {
			try { f.shutdownGUI(); } catch (OperationCancelledException e) { }
		}
		public String getText() {
			return "Switch to console mode";
		};
	};
	
	public static final Action RECONFIGURE_PLUGINS_ACTION = new Action() {
		public String getText() {
			return "Reconfigure plugins";
		}
		@Override
		public void run(Framework framework) {
			framework.getPluginManager().doReconfigure();
		};
	};

	public static final Action IMPORT_ACTION = new Action() {
		public String getText() {
			return "Import...";
		}
		@Override
		public void run(Framework framework) {
			framework.getMainWindow().importFrom();
		}
	};

	public static final Action EDIT_SETTINGS_ACTION = new Action() {
		public String getText() {
			return "Preferences...";
		}
		@Override
		public void run(Framework framework) {
			framework.getMainWindow().editSettings();
		}
	};
	
	public static final Action RESET_GUI_ACTION = new Action() {
		@Override
		public String getText() {
			return "Reset UI layout";
		}

		@Override
		public void run(Framework framework) {
			framework.getMainWindow().resetLayout();
		}
		
	};
}