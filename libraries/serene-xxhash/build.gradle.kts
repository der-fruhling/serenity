plugins {
    id("multiplatform-base")
    id("net.derfruhling.serenity.convention")
    id("published")
}

group = "net.derfruhling.serene"

kotlin {
    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        jvmTest {
            dependencies {
                compileOnly(libs.junit.api)
                runtimeOnly(libs.junit.engine)
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
