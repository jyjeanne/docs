// Gradle build for the sample DITA-OT plug-in com.example.sample-html5.
// It uses the io.github.jyjeanne.dita-ot-gradle Gradle plugin to download DITA-OT, install the plug-in,
// validate and transform the test content, and check links. List the tasks with: ./gradlew tasks --group dita

import com.github.jyjeanne.DitaLinkCheckTask
import com.github.jyjeanne.DitaOtDownloadTask
import com.github.jyjeanne.DitaOtInstallPluginTask
import com.github.jyjeanne.DitaOtTask
import com.github.jyjeanne.DitaOtValidateTask
import org.gradle.api.tasks.bundling.Zip

plugins {
    id("io.github.jyjeanne.dita-ot-gradle") version "2.8.8"
}

// DITA-OT version to test against. CI passes -PditaOtVersion=<version>; local builds use the default.
val ditaOtVersion: String = providers.gradleProperty("ditaOtVersion").getOrElse("4.4.2")
val pluginId = "com.example.sample-html5"
// DitaOtDownloadTask extracts DITA-OT to <destinationDir>/dita-ot-<version>.
val ditaOtHome = layout.buildDirectory.dir("dita-ot/dita-ot-$ditaOtVersion")

// 1. Download and extract a pinned DITA-OT distribution.
val downloadDitaOt by tasks.registering(DitaOtDownloadTask::class) {
    group = "dita"
    version.set(ditaOtVersion)
    destinationDir.set(layout.buildDirectory.dir("dita-ot"))
    retries.set(3)
}

// 2. Package the plug-in files (not the build or test files) as a ZIP.
val packagePlugin by tasks.registering(Zip::class) {
    group = "dita"
    description = "Packages the plug-in as a ZIP file."
    archiveFileName.set("$pluginId.zip")
    destinationDirectory.set(layout.buildDirectory.dir("plugin"))
    from(projectDir) {
        include("plugin.xml", "build_sample-html5.xml", "css/**")
    }
}

// 3. Install the ZIP into the downloaded DITA-OT, replacing any earlier installation.
val installPlugin by tasks.registering(DitaOtInstallPluginTask::class) {
    group = "dita"
    dependsOn(downloadDitaOt, packagePlugin)
    ditaOtDir.set(ditaOtHome)
    plugins.set(listOf(layout.buildDirectory.file("plugin/$pluginId.zip").get().asFile.absolutePath))
    force.set(true)
}

// 4. Validate the test content with the downloaded DITA-OT.
val validateDita by tasks.registering(DitaOtValidateTask::class) {
    group = "dita"
    dependsOn(downloadDitaOt)
    ditaOtDir.set(ditaOtHome)
    input("test-content/sample.ditamap")
}

// 5. Check the links in the test content. This reads the source files, so it does not need DITA-OT.
val checkLinks by tasks.registering(DitaLinkCheckTask::class) {
    group = "dita"
    input("test-content/sample.ditamap")
    failOnBroken.set(true)
}

// 6. Transform the test content with the plug-in's transformation type.
val buildSampleHtml5 by tasks.registering(DitaOtTask::class) {
    group = "dita"
    description = "Transforms the test content with the sample-html5 transformation type."
    dependsOn(installPlugin)
    mustRunAfter(validateDita, checkLinks)
    ditaOt(ditaOtHome.get().asFile)
    input("test-content/sample.ditamap")
    // One output directory per DITA-OT version, so runs for different versions do not reuse each other's output.
    output(layout.buildDirectory.dir("output/sample-html5-$ditaOtVersion").get().asFile.path)
    transtype("sample-html5")
}

// Run the whole chain with: ./gradlew dev
tasks.register("dev") {
    group = "dita"
    description = "Downloads DITA-OT, installs the plug-in, validates and transforms the test content, and checks links."
    dependsOn(validateDita, checkLinks, buildSampleHtml5)
}
