package works.szabope.plugins.pylint.annotator

import com.intellij.testFramework.TestDataPath
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.pylint.AbstractPylintTestCase
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath("\$CONTENT_ROOT/testData/annotation")
class AnnotatorTest : AbstractPylintTestCase() {
    override fun getTestDataPath() = "src/test/testData/annotation"

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(PylintInspection())
    }

    fun `test file annotated`() {
        with(Settings.getInstance(myFixture.project)) {
            executablePath = Paths.get(testDataPath).resolve("pylint").absolutePathString()
            projectDirectory = Paths.get(testDataPath).absolutePathString()
            useProjectSdk = false
            arguments = null
            configFilePath = null
            scanBeforeCheckIn = false
        }
        myFixture.configureByText("a.py", "tutu = 8")
        assertNotEmpty(myFixture.doHighlighting())
    }

    fun `test PylintAnnotator does not fail with incomplete settings`() {
        with(Settings.getInstance(myFixture.project)) {
            executablePath = null
            useProjectSdk = false
        }
        myFixture.configureByText("a.py", "tutu = 8")
        myFixture.doHighlighting()
    }

    fun `test MypyAnnotator does not fail if mypy executable path has a space in it`() {
        with(Settings.getInstance(project)) {
            executablePath = Paths.get(testDataPath).resolve("white space/pylint").absolutePathString()
            configFilePath = Paths.get(testDataPath).resolve("white space/configuration.toml").absolutePathString()
            projectDirectory = Paths.get(testDataPath).absolutePathString()
            arguments = null
            useProjectSdk = false
        }
        myFixture.configureByText("a.py", "tutu = 8")
        assertNotEmpty(myFixture.doHighlighting())
    }
}