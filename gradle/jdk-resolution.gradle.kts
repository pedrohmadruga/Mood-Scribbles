import java.io.File
import java.util.Properties

/**
 * Resolves a full JDK (with [jlink]) for Gradle when JAVA_HOME points at an IDE JRE.
 * Skips override when the current JDK already has jlink so other developers are unaffected.
 */
fun File.hasJlink(): Boolean = File(this, "bin/jlink").exists()

fun loadProperties(fileName: String): Properties? {
    val file = File(settings.rootDir, fileName)
    if (!file.exists()) return null
    return Properties().apply { file.inputStream().use { load(it) } }
}

fun jdkFromProperties(props: Properties, key: String): File? {
    return props.getProperty(key)?.trim()?.takeIf { it.isNotEmpty() }?.let { File(it) }?.takeIf { it.hasJlink() }
}

fun discoverFallbackJdk(): File? {
    val os = System.getProperty("os.name").lowercase()
    val candidates = buildList {
        if (os.contains("mac") || os.contains("darwin")) {
            add("/Applications/Android Studio.app/Contents/jbr/Contents/Home")
            add("/Applications/Android Studio Preview.app/Contents/jbr/Contents/Home")
            add("/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home")
            add("/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home")
            add("/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home")
            add("/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home")
        }
        if (os.contains("linux")) {
            add("/opt/android-studio/jbr")
            add("/usr/lib/jvm/java-21-openjdk")
            add("/usr/lib/jvm/java-17-openjdk")
        }
        if (os.contains("win")) {
            System.getenv("ProgramFiles")?.let { add("$it\\Android\\Android Studio\\jbr") }
            System.getenv("LOCALAPPDATA")?.let { add("$it\\Programs\\Android Studio\\jbr") }
        }
    }
    return candidates.map(::File).firstOrNull { it.hasJlink() }
}

fun resolveGradleJdk(): File? {
    loadProperties("jdk.local.properties")
        ?.let { jdkFromProperties(it, "org.gradle.java.home") }
        ?.let { return it }

    loadProperties("local.properties")
        ?.let { jdkFromProperties(it, "java.home") }
        ?.let { return it }

    System.getenv("JAVA_HOME")?.let { File(it) }?.takeIf { it.hasJlink() }?.let { return it }

    val javaHome = System.getProperty("java.home") ?: return discoverFallbackJdk()
    val normalized = if (javaHome.endsWith("${File.separator}jre")) {
        File(javaHome).parentFile?.absolutePath ?: javaHome
    } else {
        javaHome
    }
    File(normalized).takeIf { it.hasJlink() }?.let { return it }

    return discoverFallbackJdk()
}

resolveGradleJdk()?.absolutePath?.let { jdkHome ->
    System.setProperty("org.gradle.java.home", jdkHome)
}
