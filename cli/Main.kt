import java.io.File
import java.net.URL
import java.util.Scanner

fun runCommand(command: List<String>) {
    ProcessBuilder(command)
        .inheritIO()
        .start()
        .waitFor()
}

fun main(args: Array<String>) {
    if (args.size < 2 || args[0] != "install") {
        println("Usage: install <module>")
        return
    }

    val moduleName = args[1]
    val registryFile = File("modules.json")

    if (!registryFile.exists()) {
        println("modules.json not found")
        return
    }

    val content = registryFile.readText()
    val regex = """"$moduleName"\s*:\s*"([^"]+)"""".toRegex()
    val match = regex.find(content)

    if (match == null) {
        println("Module not found in registry")
        return
    }

    val repoUrl = match.groupValues[1]
    val targetDir = File("modules/$moduleName")

    if (targetDir.exists()) {
        println("Module already installed")
        return
    }

    runCommand(listOf("git", "clone", repoUrl, targetDir.path))
}