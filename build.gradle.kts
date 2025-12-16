plugins {
    java
    id("com.gradleup.shadow") version "9.3.0"
}

group = "modexplorer"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<Jar> {
    manifest {
        attributes(
                "Main-Class" to "modexplorer.Main"
        )
    }
}

dependencies {
    val asmVersion = "9.9.1"
    implementation("org.ow2.asm:asm:${asmVersion}")
    implementation("org.ow2.asm:asm-commons:${asmVersion}")
    implementation("org.ow2.asm:asm-tree:${asmVersion}")
    implementation("org.ow2.asm:asm-analysis:${asmVersion}")
    implementation("org.ow2.asm:asm-util:${asmVersion}")
}
