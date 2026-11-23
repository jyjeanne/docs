# Sample DITA-OT plug-in with a Gradle test build

This sample is a small DITA-OT plug-in, `com.example.sample-html5`, together with a Gradle build that tests it. The
plug-in adds one transformation type, `sample-html5`: HTML5 output with a custom stylesheet.

The build uses the [`io.github.jyjeanne.dita-ot-gradle`](https://github.com/jyjeanne/dita-ot-gradle) Gradle plugin,
version 2.8.8. That Gradle plugin is maintained outside the DITA-OT project. The DITA-OT plug-in does not depend on it:
only the build does.

The DITA-OT documentation explains this sample step by step in the topics _Testing plug-ins_, _Testing plug-ins with
Gradle_, _Testing plug-ins against several DITA-OT versions_ and _Gradle task types for DITA-OT_.

## Layout

```
dita-ot-gradle-plugin
├── plugin.xml                Plug-in configuration file
├── build_sample-html5.xml    Ant script for the transformation type
├── css
│   └── sample.css            Stylesheet copied to the output
├── test-content              Test maps and topics
│   ├── sample.ditamap
│   └── topics
├── build.gradle.kts          Build script that installs and tests the plug-in
├── settings.gradle.kts
├── gradle.properties
├── gradlew, gradlew.bat      Gradle wrapper (Gradle 9.3.0)
├── gradle/wrapper
└── ci
    └── dita-ot-matrix.yml    GitHub Actions matrix across DITA-OT versions
```

Only `plugin.xml`, `build_sample-html5.xml` and `css/` go into the plug-in ZIP file. The test content and the build
files stay in the repository.

## Tasks

The task types come from the Gradle plugin. The task names are defined in `build.gradle.kts`.

| Task               | Task type                 | What it does                                                                               |
| ------------------ | ------------------------- | ------------------------------------------------------------------------------------------ |
| `downloadDitaOt`   | `DitaOtDownloadTask`      | Downloads the DITA-OT version given by `-PditaOtVersion` (default 4.4.2)                   |
| `packagePlugin`    | `Zip` (Gradle)            | Packages the plug-in as `build/plugin/com.example.sample-html5.zip`                        |
| `installPlugin`    | `DitaOtInstallPluginTask` | Installs the ZIP file into the downloaded DITA-OT, replacing an earlier install            |
| `validateDita`     | `DitaOtValidateTask`      | Validates the test content with the downloaded DITA-OT                                     |
| `checkLinks`       | `DitaLinkCheckTask`       | Checks the links in the test content and fails the build if one is broken                  |
| `buildSampleHtml5` | `DitaOtTask`              | Transforms the test content with `sample-html5` into `build/output/sample-html5-<version>` |
| `dev`              | (aggregate)               | Runs all of the tasks above                                                                |

Java 17 or later is required. Run the whole chain:

```
./gradlew dev
```

On Windows, use `gradlew.bat dev`. To test another DITA-OT version, add `-PditaOtVersion=<version>`, for example
`./gradlew dev -PditaOtVersion=4.2.3`. List the tasks with `./gradlew tasks --group dita`.

## CI matrix across DITA-OT versions

`ci/dita-ot-matrix.yml` runs `./gradlew dev` once for each DITA-OT version in its matrix (4.2.3 and 4.4.2). To use it,
copy it to `.github/workflows/` in a repository that has the files of this sample at its root, and edit the version
list to match the DITA-OT versions that your plug-in supports.

The DITA-OT version is part of the output directory name, so that Gradle does not reuse the output of one version as
the result for another.

## Windows: paths with spaces

With version 2.8.8 of the Gradle plugin on Windows, `installPlugin` fails if the project path contains spaces. Move the
project to a path without spaces, or map it to a drive letter and run the build from there:

```
subst X: "C:\path with spaces\dita-ot-gradle-plugin"
cd /d X:\
gradlew.bat dev
```
