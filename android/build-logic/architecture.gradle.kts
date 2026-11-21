import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedDependencyResult

tasks.register("checkArchitecture") {
    group = "verification"
    doLast {
        val violations = mutableSetOf<String>()
        val classpaths = Regex("(?i)(.*(?:debug|release))?(compile|runtime)Classpath")
        subprojects.forEach { module ->
            module.configurations.filter { it.isCanBeResolved && classpaths.matches(it.name) }.forEach { config ->
                val graph = config.incoming.resolutionResult
                graph.allComponents.mapNotNull { (it.id as? ProjectComponentIdentifier)?.projectPath }.forEach { path ->
                    if (path == ":core:testing" && path != module.path) {
                        violations += "${module.path}:${config.name} contains test-only $path"
                    }
                    if (module.path != ":app" && path != module.path && path.startsWith(":feature:") &&
                        path != ":feature:search-api"
                    ) {
                        violations += "${module.path}:${config.name} reaches feature implementation $path"
                    }
                }
                if (config.name.startsWith("prod")) {
                    graph.allDependencies.filterIsInstance<ResolvedDependencyResult>().forEach { dependency ->
                        val attributes = dependency.resolvedVariant.attributes
                        val isDemo =
                            attributes.keySet().any { key ->
                                key.name.endsWith("ProductFlavor:backend") && attributes.getAttribute(key).toString() == "demo"
                            }
                        if (isDemo) {
                            violations += "${module.path}:${config.name} resolves demo ${dependency.selected.id}"
                        }
                    }
                }
            }
        }

        val forbiddenImport = Regex("""(?m)^\s*import\s+(?:android\.|androidx\.|retrofit2\.|okhttp3\.|.*\.dto\.)""")
        fileTree(rootDir) {
            include("core/model/src/main/**/*.kt", "core/contracts/src/main/**/*.kt", "feature/*/src/**/domain/**/*.kt")
        }.forEach { source ->
            if (forbiddenImport.containsMatchIn(source.readText())) {
                violations += "Platform or data import in ${source.relativeTo(rootDir)}"
            }
        }

        check(violations.isEmpty()) { violations.joinToString("\n") }
    }
}
