package works.szabope.plugins.pylint.toolWindow

import com.intellij.openapi.components.Service
import works.szabope.plugins.common.toolWindow.AbstractTreeService
import works.szabope.plugins.pylint.services.pylintSeverityConfigs

@Service(Service.Level.PROJECT)
class PylintTreeService : AbstractTreeService(pylintSeverityConfigs.keys)