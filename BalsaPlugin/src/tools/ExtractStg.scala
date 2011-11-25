package tools
import org.workcraft.Tool
import org.workcraft.workspace.WorkspaceEntry
import org.workcraft.ToolJob
import org.workcraft.interop.ServiceNotAvailableException
import org.workcraft.plugins.balsa.BalsaCircuit
import org.workcraft.plugins.shared.presets.PresetManager
import org.workcraft.plugins.balsa.io.BalsaExportConfig
import java.io.File
import org.workcraft.util.GUI
import org.workcraft.Framework
import org.workcraft.plugins.stg.HistoryPreservingStorageManager
import org.workcraft.plugins.balsa.io.ExtractControlSTGTask
import org.workcraft.tasks.DummyProgressMonitor
import org.workcraft.plugins.balsa.io.StgExtractionResult
import org.workcraft.tasks.Result
import javax.swing.SwingUtilities
import org.workcraft.tasks.Result.Outcome
import org.workcraft.gui.workspace.Path
import org.workcraft.util.FileUtils
import org.workcraft.plugins.stg21.StgModelDescriptor
import org.workcraft.gui.ExceptionDialog
import javax.swing.JOptionPane
import java.lang.reflect.InvocationTargetException

class ExtractStg (framework : Framework) extends Tool {
	val getSection = "Synthesis"
	val getDisplayName = "Extract control STG"
	@throws(classOf[ServiceNotAvailableException])
	def applyTo(we : WorkspaceEntry) : ToolJob = {
		val balsaCircuit = we.getModelEntry().getImplementation(BalsaCircuit.SERVICE_HANDLE);
		
		return new ToolJob() {
			override def run {
				val presetManager = new PresetManager[BalsaExportConfig](new File("config/balsa_export.xml"), new BalsaConfigSerialiser());
				
				val dialog = new ExtractControlSTGDialog(framework.getMainWindow(), presetManager);
				GUI.centerAndSizeToParent(dialog, framework.getMainWindow);
				dialog.setVisible(true);
				
				if (dialog.getModalResult==1)
				{
					val storage = new HistoryPreservingStorageManager()
					
					val task = new ExtractControlSTGTask(framework, balsaCircuit, dialog.getSettingsFromControls(), storage)
					framework.getTaskManager().queue(task, "Extracting control STG", new DummyProgressMonitor[StgExtractionResult]
							{
								def finished(result : Result[_ <: StgExtractionResult], description : String ) {
									try {
										SwingUtilities.invokeAndWait(new Runnable()
										{
										  def run {
												if(result.getOutcome() == Outcome.FINISHED)
												{
													val path = we.getWorkspacePath
													val fileName = FileUtils.getFileNameWithoutExtension(new File(path.getNode()))
													
													val model = result.getReturnValue().getResult()
													val resolved = framework.getWorkspace().add(path.getParent(), fileName + "_resolved", StgModelDescriptor.newDocument(model), true)
													framework.getMainWindow().tryCreateEditorWindow(resolved)
												}
												else
												{
													if(result.getCause() != null)
														ExceptionDialog.show(framework.getMainWindow(), result.getCause())
													else {
														val pcompResult = result.getReturnValue().getPcompResult()
														if(pcompResult != null)
															JOptionPane.showMessageDialog(framework.getMainWindow(), "Parallel composition failed: \n\n" + new String(pcompResult.getErrors()), "Parallel composition failed", JOptionPane.WARNING_MESSAGE)
													}
												}
											}
										})
									} catch {
									  case (e : InterruptedException) => e.printStackTrace()
									  case (e : InvocationTargetException) => e.printStackTrace()
									}
								};
							}
					);
				}
			}
		};
	}
}
