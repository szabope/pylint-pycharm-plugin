package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.components.service
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.jps.entities.ContentRootEntity
import com.intellij.platform.workspace.jps.entities.ExcludeUrlEntity
import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.url.VirtualFileUrl
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.waitUntilAssertSucceeds
import com.intellij.testFramework.workspaceModel.updateProjectModel
import com.intellij.ui.tree.TreeTestUtil
import kotlinx.coroutines.runBlocking
import works.szabope.plugins.pylint.AbstractToolWindowTestCase
import works.szabope.plugins.pylint.dialog.IDialogManager
import works.szabope.plugins.pylint.services.PylintPackageUtil
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.testutil.TestDialogManager
import works.szabope.plugins.pylint.testutil.scan
import java.net.URL
import java.nio.file.Paths
import javax.swing.event.HyperlinkEvent
import kotlin.io.path.absolutePathString

@TestDataPath("\$CONTENT_ROOT/testData/action/scan_sdk")
class ScanSdkTest : AbstractToolWindowTestCase() {

    private val treeUtil = TreeTestUtil(tree)
    private lateinit var dialogManager: TestDialogManager

    override fun getTestDataPath() = "src/test/testData/action/scan_sdk"

    override fun setUp() {
        super.setUp()
        dialogManager = service<IDialogManager>() as TestDialogManager
    }

    override fun tearDown() {
        if (::dialogManager.isInitialized) dialogManager.cleanup()
        super.tearDown()
    }

    fun testManualScan() = withMockSdk("${Paths.get(testDataPath).absolutePathString()}/MockSdk") {
        myFixture.copyDirectoryToProject("/", "/")
        runBlocking {
            PylintPackageUtil.install(project)
        }
        setUpSettings()
        val workspaceModel = WorkspaceModel.getInstance(project)
        val excludedDir = workspaceModel.currentSnapshot.entities(ContentRootEntity::class.java).first().url.append(
            "/excluded_dir"
        )
        val excludedEntity = ExcludeUrlEntity(excludedDir, object : EntitySource {
            override val virtualFileUrl: VirtualFileUrl?
                get() = excludedDir
        })
        runWriteAction { workspaceModel.updateProjectModel { model -> model.addEntity(excludedEntity) } }

        toolWindowManager.onBalloon {
            it.listener?.hyperlinkUpdate(
                HyperlinkEvent(
                    "dumb", HyperlinkEvent.EventType.ACTIVATED, URL("http://localhost")
                )
            )
        }
        dialogManager.onAnyDialog {
            fail(it.toString())
        }
        val target =
            workspaceModel.currentSnapshot.entities(ContentRootEntity::class.java).first().url.virtualFile!!
        scan(getContext { it.add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(target)) })
        runBlocking {
            waitUntilAssertSucceeds {
                treeUtil.assertStructure("+Found 2 issue(s) in 1 file(s)\n")
            }.also {
                treeUtil.expandAll()
                treeUtil.assertStructure(
                    """|-Found 2 issue(s) in 1 file(s)
                   | -/src/action/scan_cli/manualScan.py
                   |  [disallowed-name] Disallowed name "tata"
                   |  [disallowed-name] Disallowed name "tutu"
                   |""".trimMargin()
                )
            }
        }
    }

    private fun setUpSettings() {
        with(PylintSettings.getInstance(project)) {
            executablePath = null
            projectDirectory = Paths.get(testDataPath).absolutePathString()
            useProjectSdk = true
            configFilePath = null
            isScanBeforeCheckIn = false
            arguments = null
            isExcludeNonProjectFiles = true
        }
    }
}
