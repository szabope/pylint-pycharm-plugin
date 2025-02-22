# pylint-pycharm
[![Apache-2.0 license](https://img.shields.io/github/license/szabope/pylint-pycharm-plugin.svg?style=plastic)](https://github.com/szabope/pylint-pycharm-plugin/blob/master/LICENSE)

<!-- Plugin description -->
### Warning: *BETA* - should be working but some unexpected behavior is expected
This plugin provides PyCharm with both real-time and on-demand scanning capabilities using an external Pylint tool.\
It is the rework of [Roberto Leinardi](https://github.com/szabope/pylint-pycharm-plugin?tab=readme-ov-file#acknowledgements)'s [pylint-pycharm](https://github.com/leinardi/pylint-pycharm) plugin.[ Click here](https://github.com/szabope/pylint-pycharm-plugin?tab=readme-ov-file#differences-from-the-original-plugin) to see differences.

[Pylint](https://github.com/pylint-dev/pylint), as described by its authors:
>Pylint is a static code analyser for Python 2 or 3.
>
>Pylint analyses your code without actually running it. It checks for errors, enforces a coding standard, looks for code smells, and can make suggestions about how the code could be refactored.

![low resolution pylint plugin screenshot](https://raw.githubusercontent.com/szabope/pylint-pycharm-plugin/331ec0ca418012a6e222445cf541e037da664f81/art/results_lowres.png)
<!-- Plugin description end -->

## Installation steps
https://www.jetbrains.com/help/pycharm/managing-plugins.html#Managing_Plugins.topic

## Configuration
Configuration is done on a project basis. Regardless of whether it is set up via the automated way or manually, 
pylint executable and configuration setting validation **executes the candidate**.

### Automated configuration
Upon project load, the plugin looks for existing settings for Leinardi's pylint plugin and makes a copy of them. Executable only set if the version of pylint is supported.\
If such configuration was not found, project SDK is checked for pylint installation and configured if pylint was found. WSL is not supported, yet.\
If there is no project SDK with pylint installed, the plugin tries to detect the executable by running `where pylint.exe` on Windows, `which pylint` otherwise.\
Incomplete configuration triggers a notification (`Install pylint` only shown if applicable):\
![pylint plugin incomplete configuration screenshot](https://raw.githubusercontent.com/szabope/pylint-pycharm-plugin/331ec0ca418012a6e222445cf541e037da664f81/art/pylint_not_set.png)

### Manual configuration
You can modify settings at [Tools](https://www.jetbrains.com/help/pycharm/settings-tools.html#Settings_Tools.topic) / **Pylint**.
#### Native vs. IntelliJ execution
- `Pylint executable` - It's like you run it from CLI, no IntelliJ configuration is considered.
- `Use project SDK` - It's like you created an IntelliJ Run Configuration for it. Your SDK settings are considered: e.g. extra interpreter paths

![pylint plugin settings screenshot](https://raw.githubusercontent.com/szabope/pylint-pycharm-plugin/32000f4ef907f2f3d25eba4b00e09fefcf94a005/art/settings.png)

### Pre-Checkin hook
![pre-checkin hook](https://raw.githubusercontent.com/szabope/pylint-pycharm-plugin/340efa332a61ec522d253d297ccfb476056ab3e4/art/pre-checkin.png)

### Inspection severity
Pylint real-time scan severity level is set to `Warning` by default. You can change this in [inspection settings](https://www.jetbrains.com/help/pycharm/inspections-settings.html#Inspections_Settings.topic).

## Usage

**Scan with Pylint** ![](https://raw.githubusercontent.com/szabope/pylint-pycharm-plugin/331ec0ca418012a6e222445cf541e037da664f81/art/pylintScanAction.svg)
action is available in right-click menus for the Python file loaded into the editor, its tab,
and Python files and directories in the project and changes views. You may select multiple targets,
but all of them has to be either a Python file or a directory.\
**Rescan Latest** ![](https://raw.githubusercontent.com/szabope/pylint-pycharm-plugin/331ec0ca418012a6e222445cf541e037da664f81/art/refresh.svg)
action is available within Pylint toolwindow. It clears the results and re-runs pylint for the latest target.
Pylint configuration is not retained from the previous run.\
**Scan Editor** ![](https://raw.githubusercontent.com/szabope/pylint-pycharm-plugin/331ec0ca418012a6e222445cf541e037da664f81/art/execute.svg)
action is available within Pylint toolwindow. It clears the results and runs pylint for the one file that is open
and currently focused in the Editor.

![pylint plugin menu](https://raw.githubusercontent.com/szabope/pylint-pycharm-plugin/331ec0ca418012a6e222445cf541e037da664f81/art/menu.png)

![pylint plugin results](https://raw.githubusercontent.com/szabope/pylint-pycharm-plugin/331ec0ca418012a6e222445cf541e037da664f81/art/results.png)

## FAQ
### How can I prevent the code inspection from running on a specific folder?
[Exclude it](https://www.jetbrains.com/help/pycharm/configuring-folders-within-a-content-root.html#mark) from the project.

## Differences from the original plugin
- Toolbar actions were simplified:
    - Close toolbar: **removed**
    - Check module: **removed**
    - Check project: **removed**
    - Check all modified files: **removed**
    - Check files in the current changelist: **removed**
    - Clear all: **removed**
    - Severity filters: **grouped**
    - Rescan: **added**

- Scan can now be started from the right-click menu within the editor, on an editor tab, and on files or directories
  in the project and changes views.

## Known issues
- If Roberto's plugin is enabled when you install this one, then you get \
`Conflicting component name 'PylintConfigService': class com.leinardi.pycharm.pylint.PylintConfigService and class works.szabope.plugins.pylint.services.OldPylintSettings`\
But it still works.

## Acknowledgements
A huge thanks to [Roberto Leinardi](https://github.com/leinardi) for the creation and maintenance of the original plugin and for the support and guidance in the rework.

## License
```
Copyright 2024 Peter Szabo.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
```
