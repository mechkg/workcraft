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

package org.workcraft.gui.propertyeditor.cpog;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ModifiableExpressionCombinator;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.string.StringProperty;
import org.workcraft.plugins.cpog.Encoding;

public class EncodingProperty {
	
	public static EditableProperty create(String propertyName, final ModifiableExpression<Encoding> encoding) {
		ModifiableExpression<String> stringEncoding = bind(encoding, new ModifiableExpressionCombinator<Encoding, String>() {
			@Override
			public Expression<? extends String> get(Encoding arg) {
				return constant(arg.toString());
			}

			@Override
			public Encoding set(String newVal) {
				Encoding enc = eval(encoding);
				return enc.updateEncoding(newVal);
			}
		});
		return StringProperty.create(propertyName, stringEncoding);
	}
}
