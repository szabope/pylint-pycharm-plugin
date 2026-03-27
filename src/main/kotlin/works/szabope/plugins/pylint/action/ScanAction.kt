package works.szabope.plugins.pylint.action

import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.common.action.AbstractScanAction
import works.szabope.plugins.common.action.AbstractScanJobRegistry
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService
import works.szabope.plugins.common.services.IncompleteConfigurationNotifier
import works.szabope.plugins.common.services.ToolExecutorConfiguration
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.common.toolWindow.ITreeService
import works.szabope.plugins.pylint.services.PylintIncompleteConfigurationNotifier
import works.szabope.plugins.pylint.services.PylintPluginPackageManagementService
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.services.ScanService
import works.szabope.plugins.pylint.services.parser.PylintMessageConverter
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel
import works.szabope.plugins.pylint.toolWindow.PylintTreeService

open class ScanAction : AbstractScanAction() {

    override fun getTreeService(project: Project): ITreeService = PylintTreeService.getInstance(project)
    override fun getSettings(project: Project): Settings = PylintSettings.getInstance(project)
    override fun getScanJobRegistry(project: Project): AbstractScanJobRegistry = PylintScanJobRegistryService.getInstance(project)
    override fun getToolWindowId(): String = PylintToolWindowPanel.ID
    override fun getIncompleteConfigurationNotifier(project: Project): IncompleteConfigurationNotifier = PylintIncompleteConfigurationNotifier.getInstance(project)
    override fun getPackageManagementService(project: Project): AbstractPluginPackageManagementService = PylintPluginPackageManagementService.getInstance(project)

    override suspend fun scanAndAdd(
        project: Project,
        targets: Collection<VirtualFile>,
        configuration: ToolExecutorConfiguration,
        treeService: ITreeService
    ) {
        ScanService.getInstance(project).scanAsync(targets, configuration).forEach {
            val message = PylintMessageConverter.convert(it)
            withContext(Dispatchers.EDT) {
                treeService.add(message)
            }
        }
    }

    companion object {
        const val ID = "works.szabope.plugins.pylint.action.ScanAction"
    }
}
