import java.io.File

fun runCommand(command: List<String>) {
    ProcessBuilder(command)
        .inheritIO()
        .start()
        .waitFor()
}

fun getAllModules(): Map<String, String> {
    val registryFile = File("modules.json")
    if (!registryFile.exists()) return emptyMap()

    val content = registryFile.readText()

    val regex = """([^"]+)"\s*:\s*"([^"]+)""".toRegex()
    return regex.findAll(content)
        .associate { it.groupValues[1] to it.groupValues[2] }
}

fun getRepoUrl(moduleName: String): String? {
    return getAllModules()[moduleName]
}

fun main(args: Array<String>) {

    if (args.size < 2) {
        println("Usage:")
        println(" install <module>")
        println(" install all")
        println(" update <module>")
        return
    }

    val command = args[0]
    val moduleName = args[1]

    when (command) {

        "install" -> {

            if (moduleName == "all") {

                val modules = getAllModules()

                if (modules.isEmpty()) {
                    println("No modules found in registry")
                    return
                }

                for ((name, url) in modules) {
                    val targetDir = File("modules/$name")

                    if (targetDir.exists()) {
                        println("$name already installed")
                        continue
                    }

                    println("Installing $name...")
                    runCommand(listOf("git", "clone", url, targetDir.path))
                }

                return
            }

            val repoUrl = getRepoUrl(moduleName)
            if (repoUrl == null) {
                println("Module not found in registry")
                return
            }

            val targetDir = File("modules/$moduleName")

            if (targetDir.exists()) {
                println("Module already installed")
                return
            }

            runCommand(listOf("git", "clone", repoUrl, targetDir.path))
        }

        "update" -> {

            if (moduleName == "all") {

                val modulesDir = File("modules")
                if (!modulesDir.exists()) {
                    println("No modules installed")
                    return
                }

                modulesDir.listFiles()?.forEach { dir ->
                    if (dir.isDirectory) {
                        println("Updating ${dir.name}...")
                        runCommand(listOf("git", "-C", dir.path, "pull"))
                    }
                }

                return
            }

            val targetDir = File("modules/$moduleName")

            if (!targetDir.exists()) {
                println("Module not installed")
                return
            }

            runCommand(listOf("git", "-C", targetDir.path, "pull"))
        }

        else -> println("Unknown command")
    }
}