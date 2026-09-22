
plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

extra["modVersion"] = "0.1.0"

// Explicit opt-in test harness. Never included in the normal distributable.
if (providers.gradleProperty("qa").isPresent) {
    sourceSets.main { java.srcDir("src/qa/java") }
}
