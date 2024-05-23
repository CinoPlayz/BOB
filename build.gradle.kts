import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
}

group = "bob.zp.app"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    testImplementation(kotlin("test")) // test environment
    implementation("org.mindrot:jbcrypt:0.4") // User password encrypt
    implementation("androidx.compose.material:material-icons-extended:1.6.7") // Google Fonts Icons
    //implementation("com.google.code.gson:gson:2.10.1") // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3") // JSON
    implementation("com.github.kittinunf.fuel:fuel:2.3.1") // Fuel library (HTTP)
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1") // .env file support
    implementation("org.mongodb:mongodb-driver-sync:5.1.0") // MongoDB Driver
    implementation("org.slf4j:slf4j-api:2.0.13") // Logger for MongoDB Driver
    implementation("org.slf4j:slf4j-simple:2.0.13") // Logger for MongoDB Driver
    implementation("com.fleeksoft.ksoup:ksoup:0.1.2") //HTML extracter
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0") // Test coroutines
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "BOB-ZPDesktopApp"
            packageVersion = "1.0.0"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}