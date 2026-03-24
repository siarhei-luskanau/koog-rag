plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    mainClass.set("ai.advent.MainKt")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.jdkVersion.get())
    }
}

dependencies {
    implementation(libs.koog.agents)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.ktor.client.apache5)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.slf4j.simple)
    implementation(project.dependencies.platform(libs.ktor.bom))
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
