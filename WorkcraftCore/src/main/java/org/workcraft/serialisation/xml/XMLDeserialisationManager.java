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

import java.util.HashMap;

import org.w3c.dom.Element;
import org.workcraft.PluginProvider;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.util.ConstructorParametersMatcher;
import org.workcraft.util.XmlUtil;

public class XMLDeserialisationManager implements DeserialiserFactory, NodeInitialiser, NodeFinaliser {
	private HashMap<String, XMLDeserialiser> deserialisers = new HashMap<String, XMLDeserialiser>();
	private DefaultNodeDeserialiser nodeDeserialiser = new DefaultNodeDeserialiser(this, this, this);
	
	private XMLDeserialiserState state = null;

	private void registerDeserialiser (XMLDeserialiser deserialiser) {
		deserialisers.put(deserialiser.getClassName(), deserialiser);
	}
	
	 public XMLDeserialiser getDeserialiserFor(String className) throws InstantiationException, IllegalAccessException {
		return deserialisers.get(className);
	}
	 
	public void begin(ReferenceResolver externalReferenceResolver, StorageManager storage) {
		state = new XMLDeserialiserState(externalReferenceResolver, storage);		
	}
	
	public ReferenceResolver end() {
		XMLDeserialiserState result = state;
		state = null;
		return result;
	}

	public void processPlugins(PluginProvider manager) {
		for (XMLDeserialiser deserialiser : manager.getPlugins(XMLDeserialiser.SERVICE_HANDLE))
			registerDeserialiser(deserialiser);
	}

	public Object initInstance (Element element, Object ... constructorParameters) throws DeserialisationException
	{
		Object instance = nodeDeserialiser.initInstance(element, state.getExternalReferences(), state.storage, constructorParameters);
		
		state.setInstanceElement(instance, element);
		state.setObject(element.getAttribute("ref"), instance);

		if (instance instanceof Container) {
			for (Element subNodeElement : XmlUtil.getChildElements("node", element)) {
				Object subNode = initInstance (subNodeElement, state.storage);
								
				 if (subNode instanceof Node)
					 state.addChildNode((Container)instance, (Node)subNode);
			}
		}
		return instance;
	}
	
	public Model createModel (Element element, Node root) throws DeserialisationException {

		String className = element.getAttribute("class");

		if (className == null || className.isEmpty())
			throw new DeserialisationException("Class name attribute is not set\n" + element.toString());
		
		Model result;
		Class<? extends Model> cls;

		try {
			org.workcraft.serialisation.xml.XMLDeserialiser deserialiser  = getDeserialiserFor(className);
			cls = Class.forName(className).asSubclass(Model.class);
			
			Object underlyingModel;
			if (state.getExternalReferences() != null)
				underlyingModel = state.getExternalReferences().getObject("$model");
			else
				underlyingModel = null;

			if (deserialiser instanceof ModelXMLDeserialiser) {
				result = ((ModelXMLDeserialiser)deserialiser).deserialise(element, (Model)underlyingModel, root, state.getInternalReferences(), state.getExternalReferences());
			} else if (deserialiser != null) {
				throw new DeserialisationException ("Deserialiser for model class must implement ModelXMLDesiraliser interface");
			} else {
				if (underlyingModel == null) {
					try {
						result = ConstructorParametersMatcher.construct(cls, root, state.getInternalReferences(), state.storage);
					} catch (NoSuchMethodException e) {
						result = ConstructorParametersMatcher.construct(cls, root, state.storage);
					}
					
				}
				else {
					try {
						result = ConstructorParametersMatcher.construct(cls, underlyingModel, root, state.getInternalReferences(), state.storage);
					} catch (NoSuchMethodException e) {
						result = ConstructorParametersMatcher.construct(cls, underlyingModel, root, state.storage);
					}
				}
			}
		} catch (InstantiationException e) {
			throw new DeserialisationException(e);		
		} catch (IllegalAccessException e) {
			throw new DeserialisationException(e);
		} catch (ClassNotFoundException e) {
			throw new DeserialisationException(e);			
		} catch (NoSuchMethodException e) {
			throw new DeserialisationException("Missing appropriate constructor for model deserealisation.", e);
		} catch (IllegalArgumentException e) {
			throw new DeserialisationException(e);			
		}
		
		nodeDeserialiser.doInitialisation(element, result, cls, state.getExternalReferences());
		nodeDeserialiser.doFinalisation(element, result, state.getInternalReferences(), state.getExternalReferences(), cls.getSuperclass());
		
		state.setObject("$model", result);
		
		return result;
	}
	
	public void finaliseInstances() throws DeserialisationException {
		// finalise all instances
		for (Object o : state.instanceElements.keySet())
			finaliseInstance(o);
		
		// now add children to their respective containers
		for (Object o : state.instanceElements.keySet()) {
			if (o instanceof Container) {
				Container c = (Container)o;
				c.add(state.getChildren(c));
			}
		}
	}
	
	public void finaliseInstance(Object instance) throws DeserialisationException {
		nodeDeserialiser.finaliseInstance(state.getInstanceElement(instance), instance, state.getInternalReferences(), state.getExternalReferences());		
	}
}
