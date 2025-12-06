package works.szabope.plugins.pylint.annotator

import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.TestDataPath
import junit.framework.AssertionFailedError
import works.szabope.plugins.pylint.AbstractToolWindowTestCase
import works.szabope.plugins.pylint.services.PylintSettings
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath($$"$CONTENT_ROOT/testData/annotation")
class AnnotatorTest : AbstractToolWindowTestCase() {
    companion object {
        val DOESNT_MATTER = """|def<caret> lets_have_fun() -> [int]:
                                |   return 'fun'
                                |""".trimMargin()
    }

    override fun getTestDataPath() = "src/test/testData/annotation"

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(PylintInspection())
    }

    fun `test PylintAnnotator does not fail with incomplete settings`() {
        with(PylintSettings.getInstance(project)) {
            executablePath = ""
            useProjectSdk = false
        }
        myFixture.configureByText("a.py", DOESNT_MATTER)
        myFixture.doHighlighting()
    }

    fun `test PylintAnnotator does not fail if pylint executable path has a space in it`() {
        with(PylintSettings.getInstance(project)) {
            executablePath = Paths.get(testDataPath).resolve("white space/pylint").absolutePathString()
            configFilePath = Paths.get(testDataPath).resolve("white space/configuration.toml").absolutePathString()
            workingDirectory = Paths.get(testDataPath).absolutePathString()
            arguments = ""
            useProjectSdk = false
        }
        myFixture.configureByText("a.py", DOESNT_MATTER)
        myFixture.doHighlighting()
    }

    fun `test PylintAnnotator does not run for in-memory target`() {
        with(PylintSettings.getInstance(project)) {
            executablePath = Paths.get(testDataPath).resolve("does_not_exist").absolutePathString()
            workingDirectory = Paths.get(testDataPath).absolutePathString()
            arguments = ""
            useProjectSdk = false
        }
        var assertionError: Error? = null
        toolWindowManager.onBalloon {
            assertionError = AssertionFailedError("Should not happen: $it")
        }
        val inMemoryTarget = LightVirtualFile("file-in-memory.py", "")
        myFixture.configureFromExistingVirtualFile(inMemoryTarget)
        myFixture.doHighlighting()
        assertionError?.let { throw it }
    }
}