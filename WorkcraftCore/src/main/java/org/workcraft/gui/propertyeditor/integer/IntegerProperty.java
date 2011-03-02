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

package org.workcraft.gui.propertyeditor.integer;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.core.ModifiableExpressionCombinator;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.string.StringProperty;

public class IntegerProperty {

	public static EditableProperty create(String name, ModifiableExpression<Integer> property) {
		return StringProperty.create(name, convertToString(property));
	}

	private static ModifiableExpression<String> convertToString(ModifiableExpression<Integer> property) {
		return Expressions.bind(property, new ModifiableExpressionCombinator<Integer, String>() {
			@Override
			public Expression<? extends String> get(Integer value) {
				return Expressions.constant(String.format("%d", value));
			}

			@Override
			public Integer set(String newVal) {
				try {
					return Integer.parseInt(newVal);
				} catch (NumberFormatException e) {
					// TODO: use checked exception, augmented return value, or haskell FFS :-\
					throw new RuntimeException(e);
				}
			}
		});
	}
}
