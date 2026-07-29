plugins {
    id("published")
}

publishing {
    publications {
        create("java", MavenPublication::class) {
            from(components["java"])
        }
    }
}
