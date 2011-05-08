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

package org.workcraft.serialisation.xml;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Element;
import org.workcraft.annotations.Annotations;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.DependentNode;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.ReferenceProducer;

import com.googlecode.gentyref.GenericTypeReflector;

public class DefaultNodeSerialiser {
	private SerialiserFactory fac;
	private NodeSerialiser serialiser;
	
	public DefaultNodeSerialiser(SerialiserFactory factory, NodeSerialiser serialiser) {
		this.fac = factory;
		this.serialiser = serialiser;
	}
	
	static Collection<Method> getProperties(Class<?> type, boolean auto) {
		ArrayList<Method> result = new ArrayList<Method>();
		for(Method method : type.getMethods()) {
			boolean hasZeroArguments = method.getParameterTypes().length == 0;
			boolean isModifiableExpression = ModifiableExpression.class.isAssignableFrom(method.getReturnType()); 
			boolean markedAsAuto = Annotations.doAutoSerialisation(method);
			boolean markedAsSuppress = Annotations.suppressAutoSerialisation(method);
			if (markedAsAuto && !isModifiableExpression) {
				System.err.println("Warning! The method " + type.getName() + "." +  method + " is marked as auto-serialised, but does not return a ModifiableExpression.");
			}
			if (markedAsAuto && !hasZeroArguments) {
				System.err.println("Warning! The method " + type.getName() + "." + method + " is marked as auto-serialised, but does have some parameters. Auto-serialisation of parameterised expressions is not supported.");
			}
			if(!markedAsSuppress && (markedAsAuto || (auto && isModifiableExpression && hasZeroArguments))) {
				result.add(method);
			}
		}
		if(result.size() == 0)
			System.err.println("Warning! The class " + type+ " is marked as auto-serialised, but does not contain any methods returning a ModifiableExpression.");
		return result;
	}
 	
	private void autoSerialiseProperties(Element element, Object object, Class<?> type) throws IntrospectionException, InstantiationException, IllegalAccessException, SerialisationException, InvocationTargetException {
		// type explicitly requested to be auto-serialised
		boolean autoSerialisedClass = Annotations.doAutoSerialisation(type); 
		
		Collection<Method> properties = getProperties(type, autoSerialisedClass);

		System.out.println("auto-serialising " + properties.size() + " properties of " + type);
		
		for (Method property : properties)
		{
			// the property is writable and is not of array type, try to get a serialiser
			Class<?> propertyType = getPropertyType(property);
			XMLSerialiser serialiser = fac.getSerialiserFor(propertyType);
								
			if (!(serialiser instanceof BasicXMLSerialiser))
			{
				System.out.println("no serialiser for " + propertyType + " :(");
				// no serialiser, try to use the special case enum serialiser
				if (propertyType.isEnum())
				{
					serialiser = fac.getSerialiserFor(Enum.class);
					if (serialiser == null)
						continue;
				} else
					continue;
			}
			
			Element propertyElement = element.getOwnerDocument().createElement("property");
			element.appendChild(propertyElement);
			propertyElement.setAttribute("class", propertyType.getName());
			propertyElement.setAttribute("name", property.getName());
			
			final ModifiableExpression<?> expr;
			try {
				expr = (ModifiableExpression<?>)property.invoke(object);
			}
			catch(IllegalArgumentException e){
				System.err.println(String.format("property %s %s of %s can not be acquired: ", propertyType.getName(), property.getName(), type.getName()));
				e.printStackTrace();
				continue ;
			}
			((BasicXMLSerialiser)serialiser).serialise(propertyElement, eval(expr));
		}
	}
	
	public static Class<?> getPropertyType(Method property) {
		Type returnType = property.getGenericReturnType();
		
		Type baseType = GenericTypeReflector.getExactSuperType(returnType, ModifiableExpression.class);

		if (baseType instanceof Class<?>) {
			return null;
		}
		else {
			ParameterizedType pt = (ParameterizedType)baseType;
			Type[] actualTypeArguments = pt.getActualTypeArguments();
			if(actualTypeArguments.length != 1)
				throw new RuntimeException("length must be 1");
			return getRawType(actualTypeArguments[0]);
		}
	}

	private static Class<?> getRawType(Type type) {
		return type instanceof Class<?> ? (Class<?>)type : (Class<?>)((ParameterizedType)type).getRawType();
	}

	private void doSerialisation(Element parentElement, Object object,
			ReferenceProducer internalReferences,
			ReferenceProducer externalReferences, Class<?> currentLevel)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, IntrospectionException,
			SerialisationException, InvocationTargetException {
		
		
		final Element curLevelElement = parentElement.getOwnerDocument()
				.createElement(currentLevel.getSimpleName());
		
		autoSerialiseProperties(curLevelElement, object, currentLevel);
		
		XMLSerialiser serialiser = fac.getSerialiserFor(currentLevel);
		
		if (serialiser != null) {
			if (serialiser instanceof BasicXMLSerialiser)
				((BasicXMLSerialiser)serialiser).serialise(curLevelElement, object);
			else if (serialiser instanceof CustomXMLSerialiser)
				((CustomXMLSerialiser)serialiser).serialise(curLevelElement, object, internalReferences, externalReferences, this.serialiser);
		} else {
			if (object instanceof DependentNode && object.getClass().equals(currentLevel)) {
				Collection<MathNode> refs = ((DependentNode)object).getMathReferences();
				if (refs.size() == 1)
					curLevelElement.setAttribute("ref", externalReferences.getReference(refs.iterator().next()));
			}
		}
		
		if (curLevelElement.getAttributes().getLength() > 0 || curLevelElement.getChildNodes().getLength() > 0)
			parentElement.appendChild(curLevelElement);
		
		if (currentLevel.getSuperclass() != Object.class)
			doSerialisation(parentElement, object, internalReferences, externalReferences, currentLevel.getSuperclass());
	}
	
	public void serialise(Element parentElement, Object object,
			ReferenceProducer internalReferences,
			ReferenceProducer externalReferences) throws SerialisationException {
		try {
			doSerialisation(parentElement, object, internalReferences, externalReferences, object.getClass());
			
			parentElement.setAttribute("ref", internalReferences.getReference(object));
		} catch (IllegalArgumentException e) {
			throw new SerialisationException(e);
		} catch (InstantiationException e) {
			throw new SerialisationException(e);
		} catch (IllegalAccessException e) {
			throw new SerialisationException(e);
		} catch (IntrospectionException e) {
			throw new SerialisationException(e);
		} catch (SerialisationException e) {
			throw new SerialisationException(e);
		} catch (InvocationTargetException e) {
			throw new SerialisationException(e);
		}		
	}
}
