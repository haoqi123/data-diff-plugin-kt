# diff-generatesql

![Build](https://github.com/haoqi123/data-diff-plugin-kt/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/17961-diff-generatesql.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/17961-diff-generatesql.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

## Template ToDo list
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html).

<!-- Plugin description -->

This plugin can help you quickly get the difference in the table structure between the two databases,
thereby reducing communication time with developers

It can run in any product that uses the plug-in Database, such as IntelliJ IDEA, DataGrip

steps
1. click on Database
2. Choose two My SQL databases (If not, you need to add the My SQL data source first)
3. right click "Diff-GenerateSql"
4. Select which database is the source and which database is the target, and click OK
5. Wait a moment (a few seconds)
6. Get the difference between the source and target databases,can be executed directly in the database

PS: At this stage,Only Supports MYSQL Database

<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "diff-generatesql"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/haoqi123/data-diff-plugin-kt/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
