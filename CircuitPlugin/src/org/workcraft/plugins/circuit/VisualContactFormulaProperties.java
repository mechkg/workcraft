package org.workcraft.plugins.circuit;

import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionFilter;
import org.workcraft.gui.propertyeditor.ExpressionPropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.javacc.BooleanParser;
import org.workcraft.plugins.cpog.optimisation.javacc.ParseException;
import org.workcraft.util.Func;

public class VisualContactFormulaProperties {
	VisualCircuit circuit;
	
	public VisualContactFormulaProperties(VisualCircuit circuit) {
		this.circuit = circuit;
	}
	
	private BooleanFormula parseFormula(final VisualFunctionContact contact, String resetFunction) {
		try {
			return BooleanParser.parse(resetFunction,
					new Func<String, BooleanFormula>() {
						@Override
						public BooleanFormula eval(String name) {
							if (GlobalCache.eval(contact.parent()) instanceof VisualFunctionComponent) {
								return ((VisualFunctionComponent)GlobalCache.eval(contact.parent())).getOrCreateInput(name)
								.getReferencedContact();
							}
							
							else
								return circuit.getOrCreateOutput(name, GlobalCache.eval(contact.x())+1, GlobalCache.eval(contact.y())+1).getReferencedContact();
						}
					});
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private ModifiableExpression<String> createStringExpression(final VisualFunctionContact contact, ModifiableExpression<BooleanFormula> reset) {
		return new ModifiableExpressionFilter<String, BooleanFormula>(reset) {

			@Override
			protected BooleanFormula setFilter(String newValue) {
				if (!newValue.equals("")) {
					return parseFormula(contact, newValue);
				} else {
					return null;
				}
			}

			@Override
			protected String getFilter(BooleanFormula value) {
				return FormulaToString.toString(value);
			}
		};
	}
	
	public PropertyDescriptor getSetProperty(final VisualFunctionContact contact) {
		ModifiableExpression<BooleanFormula> set = contact.getFunction().setFunction();
		return ExpressionPropertyDeclaration.create("setProperty", createStringExpression(contact, set), String.class);
	}
	
	public PropertyDescriptor getResetProperty(final VisualFunctionContact contact) {
		ModifiableExpression<BooleanFormula> reset = contact.getFunction().resetFunction();
		return ExpressionPropertyDeclaration.create("resetProperty", createStringExpression(contact, reset), String.class);
	}

}
