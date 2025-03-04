package works.szabope.plugins.pylint.services

import com.intellij.openapi.project.Project
import com.jetbrains.python.packaging.PyRequirementParser
import works.szabope.plugins.common.services.PackageManagementFacade

class PylintPackageManagementFacade(override val project: Project) :
    PackageManagementFacade(PyRequirementParser.fromLine("pylint~=3.0")!!)
