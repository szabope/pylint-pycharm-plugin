package works.szabope.plugins.pylint.annotator

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import works.szabope.plugins.pylint.services.PylintSettings
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.pathString

@TestDataPath("\$CONTENT_ROOT/testData/annotation")
class AnnotatorTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData/annotation"

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(PylintInspection())
    }

    fun `test file annotated`() {
        with(PylintSettings.getInstance(myFixture.project)) {
            executablePath = Paths.get(myFixture.testDataPath).resolve("pylint").absolutePathString()
            projectDirectory = Paths.get(myFixture.testDataPath).pathString
            useProjectSdk = false
            arguments = null
            configFilePath = null
            isScanBeforeCheckIn = false
        }
        myFixture.configureByText("a.py", "tutu = 8")
        assertNotEmpty(myFixture.doHighlighting())
    }

    fun `test PylintAnnotator does not fail with incomplete settings`() {
        with(PylintSettings.getInstance(myFixture.project)) {
            executablePath = null
            useProjectSdk = false
        }
        myFixture.configureByText("a.py", "tutu = 8")
        myFixture.doHighlighting()
    }

    fun `test MypyAnnotator does not fail if mypy executable path has a space in it`() {
        with(PylintSettings.getInstance(project)) {
            executablePath = Paths.get(testDataPath).resolve("white space/pylint").absolutePathString()
            configFilePath = Paths.get(testDataPath).resolve("white space/configuration.toml").absolutePathString()
            projectDirectory = Paths.get(testDataPath).pathString
            arguments = null
            useProjectSdk = false
        }
        myFixture.configureByText("a.py", "tutu = 8")
        assertNotEmpty(myFixture.doHighlighting())
    }
}