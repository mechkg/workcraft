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
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.interop.ModelService;
import org.workcraft.interop.ModelServices;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.plugins.serialisation.XMLModelDeserialiser;
import org.workcraft.plugins.serialisation.XMLModelSerialiser;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;
import org.workcraft.serialisation.DeserialisationResult;
import org.workcraft.serialisation.ModelDeserialiser;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.tasks.DefaultTaskManager;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.ProgressMonitorArray;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.DataAccumulator;
import org.workcraft.util.FileUtils;
import org.workcraft.util.Maybe;
import org.workcraft.util.XmlUtil;
import org.workcraft.workspace.Workspace;


public class Framework {

	
	
	public static final String FRAMEWORK_VERSION_MAJOR = "2";
	public static final String FRAMEWORK_VERSION_MINOR = "dev";
	
	class ExecuteScriptAction implements ContextAction {
		private String script;
		private Scriptable scope;

		public ExecuteScriptAction(String script, Scriptable scope) {
			this.script = script;
			this.scope = scope;
		}

		public Object run(Context cx) {
			return cx.evaluateString(scope, script, "<string>", 1, null);
		}
	}

	class ExecuteCompiledScriptAction implements ContextAction {
		private Script script;
		private Scriptable scope;

		public ExecuteCompiledScriptAction(Script script, Scriptable scope) {
			this.script = script;
			this.scope = scope;
		}

		public Object run(Context cx) {
			return script.exec(cx, scope);
		}
	}

	class CompileScriptFromReaderAction implements ContextAction {
		private String sourceName;
		private BufferedReader reader;

		public CompileScriptFromReaderAction(BufferedReader reader, String sourceName) {
			this.sourceName = sourceName;
			this.reader = reader;
		}

		public Object run(Context cx) {
			try {
				return cx.compileReader(reader, sourceName, 1, null);
			} catch (IOException e) {
				throw new RuntimeException (e);
			}
		}
	}

	class CompileScriptAction implements ContextAction {
		private String source, sourceName;

		public CompileScriptAction(String source, String sourceName) {
			this.source = source;
			this.sourceName = sourceName;
		}

		public Object run(Context cx) {
			return cx.compileString(source, sourceName, 1, null);
		}
	}

	class SetArgs implements ContextAction {
		Object[] args;

		public void setArgs (Object[] args) {
			this.args = args;
		}

		public Object run(Context cx) {
			Object scriptable = Context.javaToJS(args, systemScope);
			ScriptableObject.putProperty(systemScope, "args", scriptable);
			systemScope.setAttributes("args", ScriptableObject.READONLY);
			return null;

		}
	}

	private PluginManager pluginManager;
	private TaskManager taskManager;
	private Config config ;
	private Workspace workspace;

	private ScriptableObject systemScope;
	private ScriptableObject globalScope;

	private boolean inGUIMode = false;
	private boolean shutdownRequested = false;
	private boolean GUIRestartRequested = false;

	private ContextFactory contextFactory = new ContextFactory();

	private boolean silent = false;

	private MainWindow mainWindow;

	public Framework() {
		pluginManager = new PluginManager(this);
		taskManager = new DefaultTaskManager()
		{
			public <T> Result<? extends T> execute(Task<T> task, String description, ProgressMonitor<? super T> observer) {
				if(SwingUtilities.isEventDispatchThread())
				{
					OperationCancelDialog<T> cancelDialog = new OperationCancelDialog<T>(mainWindow, description);
					
					ProgressMonitorArray<T> observers = new ProgressMonitorArray<T>();
					if(observer != null)
						observers.add(observer);
					observers.add(cancelDialog);
					
					this.queue(task, description, observers);
					
					cancelDialog.setVisible(true);
					
					return cancelDialog.result;
				}
				else
					return super.execute(task, description, observer);
			};
		};
		config = new Config();
		workspace = new Workspace(this);
	}

	public void loadConfig(String fileName) {
		config.load(fileName);

		throw new RuntimeException("Deprecated");
		/*for (SettingsPage page : pluginManager.getPlugins(SettingsPage.SERVICE_HANDLE)) {
			page.load(config);
		}*/
	}

	public void saveConfig(String fileName) {
		if(true)throw new RuntimeException("Deprecated");
		/*
		for (SettingsPage page : pluginManager.getPlugins(SettingsPage.SERVICE_HANDLE)) {
			page.save(config);
		}*/

		config.save(fileName);
	}

	public void setConfigVar (String key, String value) {
		config.set(key, value);
	}

	public void setConfigVar (String key, int value) {
		config.set(key, Integer.toString(value));
	}

	public void setConfigVar (String key, boolean value) {
		config.set(key, Boolean.toString(value));
	}

	public String getConfigVar (String key) {
		return config.get(key);
	}

	public int getConfigVarAsInt (String key, int defaultValue)  {
		String s = config.get(key);

		try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public boolean getConfigVarAsBool (String key, boolean defaultValue)  {
		String s = config.get(key);

		if (s == null)
			return defaultValue;
		else
			return Boolean.parseBoolean(s);
	}

	public void initJavaScript() {
		if (!silent)
			System.out.println ("Initialising javascript...");
		contextFactory.call(new ContextAction() {
			public Object run(Context cx) {
				ImporterTopLevel importer = new ImporterTopLevel();
				importer.initStandardObjects(cx, false);
				systemScope = importer;

				Object frameworkScriptable = Context.javaToJS(Framework.this, systemScope);
				ScriptableObject.putProperty(systemScope, "framework", frameworkScriptable);
				//ScriptableObject.putProperty(systemScope, "importer", );
				systemScope.setAttributes("framework", ScriptableObject.READONLY);

				globalScope = (ScriptableObject) cx.newObject(systemScope);
				globalScope.setPrototype(systemScope);
				globalScope.setParentScope(null);

				return null;
			}
		});
	}

	public ScriptableObject getJavaScriptGlobalScope() {
		return globalScope;
	}

	public void setJavaScriptProperty (final String name, final Object object, final ScriptableObject scope, final boolean readOnly) {
		contextFactory.call(new ContextAction(){
			public Object run(Context arg0) {
				Object scriptable = Context.javaToJS(object, scope);
				ScriptableObject.putProperty(scope, name, scriptable);

				if (readOnly)
					scope.setAttributes(name, ScriptableObject.READONLY);

				return scriptable;
			}
		});
	}

	public void deleteJavaScriptProperty (final String name, final ScriptableObject scope) {
		contextFactory.call(new ContextAction(){
			public Object run(Context arg0) {
				return ScriptableObject.deleteProperty(scope, name);
			}
		});
		
	}

	public Object execJavaScript(File file) throws FileNotFoundException {
		BufferedReader reader = new BufferedReader (new FileReader(file));
		return execJavaScript(compileJavaScript(reader, file.getPath()));
	}

	public Object execJavaScript(Script script) {
		return execJavaScript (script, globalScope);
	}

	static class JavascriptPassThroughException extends RuntimeException
	{
		private static final long serialVersionUID = 8906492547355596206L;
		private final String scriptTrace;

		public JavascriptPassThroughException(Throwable wrapped, String scriptTrace)
		{
			super(wrapped);
			this.scriptTrace = scriptTrace;
		}

		@Override
		public String getMessage() {
			return String.format("Java %s was unhandled in javascript. \nJavascript stack trace: %s", getCause().getClass().getSimpleName(), getScriptTrace());
		}

		public String getScriptTrace() {
			return scriptTrace;
		}
	}

	public Object execJavaScript (String script) {
		return execJavaScript(script, globalScope);
	}

	public Object execJavaScript(Script script, Scriptable scope) {
		return doContextAction(new ExecuteCompiledScriptAction(script, scope));
	}

	public Object execJavaScript(String script, Scriptable scope) {
		return doContextAction(new ExecuteScriptAction(script, scope));
	}

	private Object doContextAction (ContextAction action) {
		try
		{
			return contextFactory.call(action);
		} catch(JavaScriptException ex)
		{
			System.out.println("Script stack trace: " + ex.getScriptStackTrace());
			Object value = ex.getValue();
			if(value instanceof NativeJavaObject)
			{
				Object wrapped = ((NativeJavaObject)value).unwrap();
				if(wrapped instanceof Throwable)
					throw new JavascriptPassThroughException((Throwable)wrapped, ex.getScriptStackTrace());
			}
			throw ex;
		}
	}

	public void execJSResource (String resourceName) throws IOException {
		execJavaScript(FileUtils.readAllTextFromSystemResource(resourceName));
	}

	public void execJSFile (String filePath) throws IOException {

		execJavaScript (FileUtils.readAllText(new File(filePath)), globalScope);
	}

	public Script compileJavaScript (String source, String sourceName) {
		return (Script) doContextAction(new CompileScriptAction(source, sourceName));
	}

	public Script compileJavaScript (BufferedReader source, String sourceName) {
		return (Script) doContextAction(new CompileScriptFromReaderAction(source, sourceName));	}

	public void startGUI() {
		if (inGUIMode) {
			System.out.println ("Already in GUI mode");
			return;
		}

		GUIRestartRequested = false;

		System.out.println ("Switching to GUI mode...");


		Runnable displayMainWindow = new Runnable() {
			public void run() {
				mainWindow = new MainWindow(Framework.this);
				mainWindow.startup();
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			displayMainWindow.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(displayMainWindow);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		contextFactory.call(new ContextAction() {
			public Object run(Context cx) {
				Object guiScriptable = Context.javaToJS(mainWindow, systemScope);
				ScriptableObject.putProperty(systemScope, "mainWindow", guiScriptable);
				systemScope.setAttributes("mainWindow", ScriptableObject.READONLY);
				return null;
			}
		});

		System.out.println ("Now in GUI mode.");
		inGUIMode = true;
	}

	public void shutdownGUI() throws OperationCancelledException {
		if (inGUIMode) {

			mainWindow.shutdown();
			mainWindow.dispose();
			mainWindow = null;
			inGUIMode = false;

			contextFactory.call(new ContextAction() {
				public Object run(Context cx) {
					ScriptableObject.deleteProperty(systemScope, "mainWindow");
					return null;
				}
			});

		}
		System.out.println ("Now in console mode.");
	}

	public void shutdown() {
		shutdownRequested = true;
	}

	public boolean shutdownRequested() {
		return shutdownRequested;
	}

	public void abortShutdown() {
		shutdownRequested = false;
	}

	public MainWindow getMainWindow() {
		return mainWindow;
	}
	
	public PluginManager getPluginManager() {
		return pluginManager;
	}
	

	public TaskManager getTaskManager() {
		return taskManager;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public boolean isInGUIMode() {
		return inGUIMode;
	}

	public boolean isSilent() {
		return silent;
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	public void setArgs(List<String> args) {
		SetArgs setargs = new SetArgs();
		setargs.setArgs(args.toArray());
		contextFactory.call(setargs);
	}

	public ModelServices load(String path) throws DeserialisationException {
		try {
			FileInputStream fis = new FileInputStream(path);
			return load(fis);
		} catch (FileNotFoundException e) {
			throw new DeserialisationException(e);
		}
	}

	private static InputStream getUncompressedEntry(String name, InputStream zippedData) throws IOException {
		ZipInputStream zis = new ZipInputStream(zippedData);

		ZipEntry ze;
		
		while ((ze = zis.getNextEntry()) != null) 
		{
			if (ze.getName().equals(name))
				return zis;

			zis.closeEntry();
		}
		
		zis.close();

		return null;
	}

	interface ModelFileMetadata {
		String descriptor();
		String mathFileName();
		String visualFileName();
	}
	
	static ModelFileMetadata getMetadata(final InputStream input) throws DeserialisationException {
		final Document metaDoc;
		try {
			final InputStream metadata = getUncompressedEntry("meta", input);
	
			if (metadata == null)
				throw new DeserialisationException("meta entry is missing in the ZIP file");
	
			metaDoc = XmlUtil.loadDocument(metadata);
			
			metadata.close();
		}
		catch(Throwable e) {
			throw new DeserialisationException(e);
		}
		final Element descriptorElement = XmlUtil.getChildElement("descriptor", metaDoc.getDocumentElement());
		final Element mathElement = XmlUtil.getChildElement("math", metaDoc.getDocumentElement());
		final Element visualElement = XmlUtil.getChildElement("visual", metaDoc.getDocumentElement());
		
		final String descriptorClass = XmlUtil.readStringAttr(descriptorElement, "class");
		return new ModelFileMetadata() {
			
			@Override
			public String visualFileName() {
				return visualElement == null ? null : visualElement.getAttribute("entry-name");
			}
			
			@Override
			public String mathFileName() {
				return mathElement.getAttribute("entry-name");
			}
			
			@Override
			public String descriptor() {
				return descriptorClass;
			}
		};
	}
	
	public ModelServices load(InputStream is) throws DeserialisationException   {
		byte[] bufferedInput;
		try {
			bufferedInput = DataAccumulator.loadStream(is);
		} catch (IOException e) {
			throw new DeserialisationException(e);
		}
		
		for(Loader loader : pluginManager.getPlugins(Loader.SERVICE_HANDLE)) {
			ModelServices nullableModelServices = Maybe.Util.toNullable(loader.load(bufferedInput));
			if(nullableModelServices != null)
				return nullableModelServices;
		}
		return loadOld(bufferedInput);
	}
	
	public ModelServices loadOld(byte [] bufferedInput)throws DeserialisationException {
		try {
			ModelFileMetadata metadata = getMetadata(new ByteArrayInputStream(bufferedInput));
			
			HistoryPreservingStorageManager storage = new HistoryPreservingStorageManager(); 
			
			
			ModelDescriptor descriptor = (ModelDescriptor) Class.forName(metadata.descriptor()).newInstance();
			
			InputStream mathData = getUncompressedEntry(metadata.mathFileName(), new ByteArrayInputStream(bufferedInput));
			ModelDeserialiser mathDeserialiser = new XMLModelDeserialiser(getPluginManager());
			DeserialisationResult mathResult = mathDeserialiser.deserialise(mathData, storage, null);
			mathData.close();

			// load visual model if present
			if (metadata.visualFileName() == null)
				return descriptor.createServiceProvider(mathResult.model, storage);

			InputStream visualData = getUncompressedEntry (metadata.visualFileName(), new ByteArrayInputStream(bufferedInput));
			XMLModelDeserialiser visualDeserialiser = new XMLModelDeserialiser(getPluginManager());
			DeserialisationResult visualResult = visualDeserialiser.deserialise(visualData, storage, mathResult.referenceResolver);
			return descriptor.createServiceProvider(visualResult.model, storage);

		} catch (IOException e) {
			throw new DeserialisationException(e);
		} catch (InstantiationException e) {
			throw new DeserialisationException(e);
		} catch (IllegalAccessException e) {
			throw new DeserialisationException(e);
		} catch (ClassNotFoundException e) {
			throw new DeserialisationException(e);
		} 
	}

	public void save(ModelServices model, String path) throws SerialisationException, ServiceNotAvailableException {
		File file = new File(path);
		try {
			FileOutputStream stream = new FileOutputStream(file);
			save (model, stream);
			stream.close();
		} catch (FileNotFoundException e) {
			throw new SerialisationException(e);
		} catch (IOException e) {
			throw new SerialisationException(e);			
		}
	}
	
	public interface CustomSaver {
		public static final ModelService<CustomSaver> SERVICE_HANDLE = ModelService.createNewService(CustomSaver.class, "A custom model saver");
		void write(OutputStream out);
	}

	public void save(ModelServices modelEntry, OutputStream out) throws SerialisationException, ServiceNotAvailableException {
		try {
			modelEntry.getImplementation(CustomSaver.SERVICE_HANDLE).write(out);
		} catch(ServiceNotAvailableException ex) {
			saveDefault(modelEntry, out);
		}
	}
	
	public void saveDefault(ModelServices modelEntry, OutputStream out) throws SerialisationException, ServiceNotAvailableException {
		Model mathModel = modelEntry.getImplementation(ModelService.LegacyMathModelService);

		ZipOutputStream zos = new ZipOutputStream(out);

		// TODO: get appropiate serialiser from config
		ModelSerialiser mathSerialiser = null;

		try {
			mathSerialiser = new XMLModelSerialiser(getPluginManager());


			String mathEntryName = "model" + mathSerialiser.getExtension();
			ZipEntry ze = new ZipEntry(mathEntryName);
			zos.putNextEntry(ze);
			ReferenceProducer refResolver = mathSerialiser.serialise(mathModel, zos, null);
			zos.closeEntry();

			String visualEntryName = null;
			ModelSerialiser visualSerialiser = null;

			Document doc;
			doc = XmlUtil.createDocument();

			Element root = doc.createElement("workcraft-meta");
			doc.appendChild(root);
			
			Element descriptor = doc.createElement("descriptor");
			descriptor.setAttribute("class", modelEntry.getImplementation(ModelDescriptor.SERVICE_HANDLE).getClass().getCanonicalName());
			root.appendChild(descriptor);

			Element math = doc.createElement("math");
			math.setAttribute("entry-name", mathEntryName);
			math.setAttribute("format-uuid", mathSerialiser.getFormat().toString());
			root.appendChild(math);

			try {
				VisualModel visualModel = modelEntry.getImplementation(ModelService.LegacyVisualModelService);
				visualSerialiser = new XMLModelSerialiser(getPluginManager());

				visualEntryName = "visualModel" + visualSerialiser.getExtension();
				ze = new ZipEntry(visualEntryName);
				zos.putNextEntry(ze);
				visualSerialiser.serialise(visualModel, zos, refResolver);
				zos.closeEntry();

				Element visual = doc.createElement("visual");
				visual.setAttribute("entry-name", visualEntryName);
				visual.setAttribute("format-uuid", visualSerialiser.getFormat().toString());
				root.appendChild(visual);
			} catch (ServiceNotAvailableException ex) {
			}

			ze = new ZipEntry("meta");
			zos.putNextEntry(ze);

			XmlUtil.writeDocument(doc, zos);

			zos.closeEntry();
			zos.close();
		} catch (ParserConfigurationException e) {
			throw new SerialisationException(e);
		} catch (IOException e) {
			throw new SerialisationException(e);
		} 
	}

	public void initPlugins() {
		if (!silent)
			System.out.println ("Loading plugins configuration...");

		try {
			pluginManager.loadManifest();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		} catch (PluginInstantiationException e) {
			e.printStackTrace();
		}
	}

	public void restartGUI() throws OperationCancelledException {
		GUIRestartRequested = true;
		shutdownGUI();
	}

	public boolean isGUIRestartRequested() {
		return GUIRestartRequested;
	}

	public void loadWorkspace(File file) throws DeserialisationException {
		workspace.load(file);		
	}

	public Config getConfig() {
		return config;
	}
	
	public void gavno(int x)
	{
		
	}

}
