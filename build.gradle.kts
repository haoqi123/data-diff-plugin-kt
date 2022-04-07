import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.4.0"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "1.3.1"
}
val pluginMajorVersion: String by project
val pluginVariantVersion: String by project
val variantVersionPart = pluginVariantVersion.takeIf { it.isNotBlank() }?.let { "-$it" } ?: ""
val pluginVersion = "$pluginMajorVersion$variantVersionPart"
val fullPluginVersion = pluginVersion

val versionRegex = Regex("v(\\d(\\.\\d+)+(-([0-9a-zA-Z]+(\\.[0-9a-zA-Z]+)*))?)")
if (!versionRegex.matches("v$fullPluginVersion")) {
    throw GradleException("Plugin version 'v$fullPluginVersion' does not match the pattern '$versionRegex'")
}


extra["pluginVersion"] = pluginVersion
extra["fullPluginVersion"] = fullPluginVersion


group = properties("pluginGroup")
version = fullPluginVersion

// Configure project's dependencies
repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
    }
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    val date = LocalDate.now(ZoneId.of("Asia/Shanghai"))
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

    version.set(pluginVersion)
    header.set(provider { "v${version.get()} (${date.format(formatter)})" })
    headerParserRegex.set(versionRegex)
    groups.set(emptyList())
}

tasks {
//    runIde {
//        jvmArgs = listOf("--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED")
//    }

    buildSearchableOptions {
        jvmArgs = listOf("--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED")
    }

    test {
        ignoreFailures = true
    }

    // Set the JVM compatibility versions
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = it
        }
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
        distributionType = Wrapper.DistributionType.ALL
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            projectDir.resolve("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(provider {
            val changelogUrl = "https://github.com/haoqi123/data-diff-plugin-kt/blob/main/CHANGELOG.md"
            changelog.run {
                getOrNull("v" + properties("pluginVersion")) ?: getLatest()
            }.toHTML() + "<br/><a href=\"${changelogUrl}\"><b>Full Changelog History</b></a>"
        })
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }


    // Set the JVM compatibility versions
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
            options.encoding = "UTF-8"
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = it
        }
    }

    withType<ProcessResources> {
        filesMatching("**/*.properties") {
            filter(org.apache.tools.ant.filters.EscapeUnicode::class)
        }
    }
}