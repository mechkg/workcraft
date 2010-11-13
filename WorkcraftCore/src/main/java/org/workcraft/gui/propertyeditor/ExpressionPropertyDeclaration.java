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

package org.workcraft.gui.propertyeditor;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;

public class ExpressionPropertyDeclaration<T extends Object> implements PropertyDescriptor {
	public String name;
	
	public ModifiableExpression<? super T> setter; 
	public Expression<? extends T> getter; 
 
	public Class<T> cls;
	public Map<String, Object> predefinedValues;
	public Map<Object, String> valueNames;
	
	private boolean choice;
	
	public boolean isChoice() {
		return choice;
	}

	public ExpressionPropertyDeclaration (String name, Expression<? extends T> getter, ModifiableExpression<? super T> setter, Class<T> cls) {
		if(cls.isPrimitive())
			throw new RuntimeException("Primitive types are not supported");
		this.name = name;
		this.getter = getter;
		this.setter = setter;
		this.cls = cls;
		this.predefinedValues = null;
		this.valueNames = null;
		
		choice = false;
	}

	public Map<Object, String> getChoice() {
		return valueNames;
	}

	public Object getValue() throws InvocationTargetException {
		return GlobalCache.eval(getter);
	}

	public void setValue(Object value) throws InvocationTargetException {
		GlobalCache.setValue(setter, cls.cast(value));
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return cls;
	}

	public boolean isWritable() {
		return setter != null;
	}
}