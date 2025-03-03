@file:Suppress("UnstableApiUsage")

package works.szabope.plugins.pylint.services

import com.jetbrains.python.packaging.PyRequirementParser
import works.szabope.plugins.common.services.PackageManagementFacade

object PylintPackageManagementFacade : PackageManagementFacade(PyRequirementParser.fromLine("pylint~=3.0")!!)