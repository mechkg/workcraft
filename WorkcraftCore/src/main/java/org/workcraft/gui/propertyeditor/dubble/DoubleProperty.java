/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
* 
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.gui.propertyeditor.dubble;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.core.ModifiableExpressionCombinator;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.string.StringProperty;

public class DoubleProperty {

	public static EditableProperty create(String name, ModifiableExpression<Double> property) {
		return StringProperty.create(name, convertToString(property));
	}

	private static ModifiableExpression<String> convertToString(ModifiableExpression<Double> property) {
		return Expressions.bind(property, new ModifiableExpressionCombinator<Double, String>() {
			@Override
			public Expression<? extends String> get(Double arg) {
				return Expressions.constant(String.format("%.2f", arg));
			}

			@Override
			public Double set(String newVal) {
				try {
					return Double.parseDouble(newVal);
				} catch (NumberFormatException e) {
					// TODO: use checked exception, augmented return value, or haskell FFS :-\
					throw new RuntimeException(e);
				}
			}
		});
	}
}
