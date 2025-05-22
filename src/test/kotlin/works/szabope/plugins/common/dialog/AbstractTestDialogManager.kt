package works.szabope.plugins.common.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.testFramework.requireIs
import org.junit.Assert.assertNull

abstract class AbstractTestDialogManager : IDialogManager {
    
    private val myHandlers = hashMapOf<Class<out DialogWrapper>, (TestDialogWrapper) -> Int>()
    private var myAnyHandler: ((TestDialogWrapper) -> Int)? = null

    override fun showDialog(dialog: PluginDialog) {
        val testDialog = dialog.requireIs<TestDialogWrapper>()
        testDialog.show()
        var exitCode: Int? = null
        try {
            exitCode = myHandlers[testDialog.getWrappedClass()]?.invoke(testDialog) ?: myAnyHandler?.invoke(testDialog)
            if (exitCode == null) {
                throw IllegalStateException("The dialog is not expected here: " + dialog.javaClass)
            }
        } finally {
            testDialog.close(exitCode ?: DialogWrapper.OK_EXIT_CODE)
        }
    }

    fun onDialog(dialogClass: Class<out DialogWrapper>, handler: (TestDialogWrapper) -> Int) {
        assertNull(myHandlers.put(dialogClass, handler))
    }

    fun onAnyDialog(handler: (TestDialogWrapper) -> Any) {
        myAnyHandler = fun(h: TestDialogWrapper): Int {
            val res = handler.invoke(h)
            return if (res is Int) {
                res
            } else {
                DialogWrapper.OK_EXIT_CODE
            }
        }
    }

    fun cleanup() {
        myHandlers.clear()
        myAnyHandler = null
    }
}