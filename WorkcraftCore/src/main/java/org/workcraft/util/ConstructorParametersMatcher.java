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

package org.workcraft.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class ConstructorParametersMatcher
{
	public static <T> T construct(Class<T> cls, Object... parameters) throws NoSuchMethodException {
		if(cls.isInterface() || Modifier.isAbstract(cls.getModifiers()))
			throw new RuntimeException("Unable to construct an abstract type \"" + cls + "\"");
		try {
			Class<?>[] parameterTypes = new Class<?>[parameters.length];
			for(int i=0;i<parameters.length;i++)
				parameterTypes[i] = parameters[i].getClass();
			return new ConstructorParametersMatcher().match(cls, parameterTypes).newInstance((Object[])parameters);
		}
		catch (NoSuchMethodException e) {
			throw e;
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static class ConstructorInfo<T> implements MethodParametersMatcher.MethodInfo
	{
		public ConstructorInfo (Constructor<? extends T> constructor)
		{
			this.constructor = constructor;
			this.parameterTypes = constructor.getParameterTypes();
		}
		public final Constructor<? extends T> constructor;
		private final Class<?>[] parameterTypes;
		
		public Class<?>[] getParameterTypes() {
			return parameterTypes;
		}
	}
	
	@SuppressWarnings("unchecked") // java sucks
	public <T> Constructor<? extends T> match(Class<? extends T> c, Class<?>... parameters) throws NoSuchMethodException
	{
		ArrayList<ConstructorInfo<T>> constructors = new ArrayList<ConstructorInfo<T>>();
		for(Constructor<?> constructor : c.getConstructors())
			constructors.add(new ConstructorInfo<T>((Constructor<? extends T>)constructor));
		
		try
		{
			return MethodParametersMatcher.match(constructors, parameters).constructor;
		}
		catch(NoSuchMethodException e)
		{
			String s = "";
			for(Class<?> parameter : parameters)
			{
				if(s.length()>0)
					s+=", ";
				s += parameter.getCanonicalName();
			}
			throw new NoSuchMethodException("Unable to find a constructor for class " + c.getCanonicalName() + " with parameters (" + s + ")");
		}
	}
}
