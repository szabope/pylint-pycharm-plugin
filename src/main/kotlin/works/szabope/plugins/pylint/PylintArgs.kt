package works.szabope.plugins.pylint

object PylintArgs {
    const val PYLINT_RECOMMENDED_COMMAND_ARGS = "--jobs 0 --recursive y"
    const val PYLINT_MANDATORY_COMMAND_ARGS = "--exit-zero --output-format json2"
}