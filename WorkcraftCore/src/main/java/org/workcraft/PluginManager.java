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

package org.workcraft;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.interop.GlobalService;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.util.Function0;
import org.workcraft.util.Initialiser;
import org.workcraft.util.Maybe;
import org.workcraft.util.XmlUtil;

import pcollections.PVector;
import pcollections.TreePVector;

public class PluginManager implements PluginProvider {
	public static final File DEFAULT_MANIFEST = new File("config"+File.separator+"plugins.xml");
	public static final String VERSION_STAMP = "4cfa9423-5a19-41ac-aafa-3f0863a9bd65";

	private Framework framework;
	
	PluginCollection plugins;

	public static class PluginInstanceHolder<T> implements PluginInfo<T>
	{
		private final Initialiser<? extends T> initialiser;

		public PluginInstanceHolder(Initialiser<? extends T> initialiser)
		{
			this.initialiser = initialiser;
		}
		
		T instance = null;
		
		@Override
		public T newInstance() {
			return initialiser.create();
		}

		@Override
		public T getSingleton() {
			if(instance == null)
				instance = newInstance();
			return instance;
		}
	}
	
	
	public PluginManager(Framework framework) {
		this.framework = framework;
	}

	public void loadManifest() throws IOException, FormatException, PluginInstantiationException {
		loadManifest(DEFAULT_MANIFEST);
	}

	public static Maybe<PVector<Module>> tryLoadManifest(File file)
	{
		if(!file.exists()) {
			System.out.println("Plugin manifest \"" + file.getPath() + "\" does not exist.");
			return Maybe.Util.nothing();
		}

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc;
		DocumentBuilder db;

		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(file);
		} catch(Exception e) {
			e.printStackTrace();
			return Maybe.Util.nothing();
		}

		Element xmlroot = doc.getDocumentElement();
		if (!xmlroot.getNodeName().equals("workcraft-plugins"))
		{
			System.out.println("Bad plugin manifest: root tag should be 'workcraft-plugins'.");
			return Maybe.Util.nothing();
		}
		
		final Element versionElement = XmlUtil.getChildElement("version", xmlroot);
		
		if(versionElement == null || !XmlUtil.readStringAttr(versionElement, "value").equals(VERSION_STAMP))
		{
			System.out.println("Old plugin manifest version detected. Will reconfigure.");
			return Maybe.Util.nothing();
		}

		PVector<Module> modules = TreePVector.empty();
		
		for(Element pluginElement : XmlUtil.getChildElements("plugin", xmlroot)) {
			ModuleInfo info = new ModuleInfo(pluginElement);
			try {
				modules = modules.plus(info.create());
			} catch (RuntimeException e)
			{
				System.err.println("! WARNING: One of the modules listed in manifest could not be instantiated. Please reconfigure plugins.");
				continue;
			}
		}
		
		return Maybe.Util.just(modules);
	}
	
	public void loadManifest(File file) throws IOException, FormatException, PluginInstantiationException {
		
		List<Module> modules = Maybe.Util.orElseDo(tryLoadManifest(file), new Function0<PVector<Module>>() {
			@Override
			public PVector<Module> apply() {
				return reconfigure();
			}
		});
		initModules(modules);
	}

	private void initModules(List<Module> modules) {
		for(Module module : modules) {
			try{
				System.out.println("Loading module: " + module.getDescription());
				module.init(framework);
			}
			catch(Throwable e) {
				System.err.println("Error during initialisation of module " + module.toString());
			}
		}
		System.out.println("Modules initialisation finished.");
	}

	public static void saveManifest(List<ModuleInfo> plugins) throws IOException {
		saveManifest(DEFAULT_MANIFEST, plugins);
	}

	public static void saveManifest(File file, List<ModuleInfo> plugins) throws IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		org.w3c.dom.Document doc;
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			doc = db.newDocument();
		} catch (ParserConfigurationException e) {
			System.err.println(e.getMessage());
			return;
		}

		Element root = doc.createElement("workcraft-plugins");
		doc.appendChild(root);
		root = doc.getDocumentElement();

		for(ModuleInfo info : plugins) {
			Element e = doc.createElement("plugin");
			info.toXml(e);
			root.appendChild(e);
		}
		
		final Element versionElement = doc.createElement("version");
		versionElement.setAttribute("value", VERSION_STAMP);
		root.appendChild(versionElement);

		XmlUtil.saveDocument(doc, file);
	}

	public static PVector<Module> reconfigure() {
		System.out.println("Reconfiguring plugins...");
		
		String[] classPathLocations = System.getProperty("java.class.path").split(System.getProperty("path.separator"));

		List<Class<?>> classes = new ArrayList<Class<?>>();
		ArrayList<ModuleInfo> pluginInfos = new ArrayList<ModuleInfo>();
		
		for (String s: classPathLocations) {
			System.out.println ("Processing class path entry: " + s);
			classes.addAll(PluginFinder.search(new File(s)));
		}

		System.out.println("" + classes.size() + " plugin(s) found.");
		PVector<Module> modules = TreePVector.empty();
		
		for(Class<?> cls : classes) {
			ModuleInfo moduleInfo = new ModuleInfo(cls);
			pluginInfos.add(moduleInfo);
			modules = modules.plus(moduleInfo.create());
		}
		
		try {
			saveManifest(pluginInfos);
			System.out.println("Reconfiguration complete.");
		} catch(IOException e) {
			System.err.println(e.getMessage());
		}
		
		return modules;
	}
	
	public <T> void registerClass(GlobalService<T> interf, T implementation) {
		plugins = plugins.plus(interf, implementation);
	}

	@Override
	public <T> Collection<T> getPlugins(GlobalService<T> interfaceType) {
		return plugins.getImplementations(interfaceType);
	}

	public void doReconfigure() {
		initModules(reconfigure());
	}
}
