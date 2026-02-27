import java.io.File
import java.net.URL
import java.util.Scanner

fun runCommand(command: List<String>) {
    ProcessBuilder(command)
        .inheritIO()
        .start()
        .waitFor()
}

fun getRepoUrl(moduleName: String): String? {
    val registryFile = File("modules.json")
    if (!registryFile.exists()) return null

    val content = registryFile.readText()
    val regex = """$moduleName"\s*:\s*"([^"]+)""".toRegex()
    val match = regex.find(content)

    return match?.groupValues?.get(1)
}


fun main(args: Array<String>) {

    if (args.size < 2) {
        println("Usage:")
        println(" install <module>")
        println(" update <module>")
        return
    }

    val command = args[0]
    val moduleName = args[1]
    val targetDir = File("modules/$moduleName")

    when (command) {

        "install" -> {
            val repoUrl = getRepoUrl(moduleName)
            if (repoUrl == null) {
                println("Module not found in registry")
                return
            }

            if (targetDir.exists()) {
                println("Module already installed")
                return
            }

            runCommand(listOf("git", "clone", repoUrl, targetDir.path))
        }

        "update" -> {
            if (!targetDir.exists()) {
                println("Module not installed")
                return
            }

            runCommand(listOf("git", "-C", targetDir.path, "pull"))
        }

        else -> println("Unknown command")
    }
}