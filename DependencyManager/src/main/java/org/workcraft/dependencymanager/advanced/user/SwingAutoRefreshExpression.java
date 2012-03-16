package org.workcraft.dependencymanager.advanced.user;

import javax.swing.SwingUtilities;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.Handle;
import org.workcraft.dependencymanager.util.listeners.Listener;
import org.workcraft.util.Action;

/**
 * This class class is to be overridden by clients who wish to receive
 * Observer-like notifications every time this expression is re-evaluated.
 * 
 * The notification is sent using the onEvaluate method, which has to process
 * the event and re-establish the expression dependencies using the provided
 * EvaluationContext.
 * 
 * @author Arseniy Alekseyev
 */
public abstract class SwingAutoRefreshExpression extends ExpressionBase<Null> {

	Handle handle; // to make sure it is not garbage collected
	Listener l = new Listener() {
		@Override
		public Action changed() {
			return new Action() {
				@Override
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eval();
						}
					});
				}
			};
		}
	};

	private void eval() {
		handle = SwingAutoRefreshExpression.this.getValue(l).handle;
	}

	public SwingAutoRefreshExpression() {
		eval();
	}

	protected abstract void onEvaluate(EvaluationContext context);

	@Override
	protected final Null evaluate(EvaluationContext context) {
		onEvaluate(context);
		return null;
	}

}