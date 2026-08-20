plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dokka)
    `maven-publish`
    signing
}

android {
    namespace = "com.nfclocator.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    lint {
        // A published library's lint report is read by every consumer's own build (via the
        // AAR's bundled lint.jar / model) - CI should see every finding, not just the ones
        // severe enough to already fail a build, so warnings stay visible instead of silently
        // passing.
        warningsAsErrors = false
        abortOnError = true
        checkDependencies = false
        htmlReport = true
        xmlReport = true
    }
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)

    // Room (local catalog cache)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt annotations + DI bindings exposed for the host app to consume.
    // The Hilt Gradle plugin itself is applied only by :app (the final aggregator).
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Coroutines: required for the suspend/Flow resolver chain, not a networking framework.
    implementation(libs.kotlinx.coroutines.android)

    // Serialization: parses the bundled seed catalog asset and remote catalog DTOs.
    // Not a networking client - the HTTP call itself is injected by the host app.
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.room.testing)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.androidx.ui.test.manifest)
    testImplementation(libs.androidx.activity.compose)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// --- Maven Central publication ---
// See RELEASING.md for the full local-verification and publish walkthrough. Coordinates and
// POM metadata are read from gradle.properties (single source of truth for the version); no
// credentials or keys are ever read from a file here - only from environment variables, which
// CI supplies as secrets and a local release does via shell env, never a committed file.

val dokkaJavadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.named("dokkaJavadoc"))
    from(tasks.named("dokkaJavadoc").map { (it as org.jetbrains.dokka.gradle.DokkaTask).outputDirectory })
    archiveClassifier.set("javadoc")
}

val libraryGroup = providers.gradleProperty("GROUP").getOrElse("io.github.change-me")
val libraryArtifactId = providers.gradleProperty("POM_ARTIFACT_ID").getOrElse("nfc-locator-core")
val libraryVersionName = providers.gradleProperty("VERSION_NAME").getOrElse("0.0.0-SNAPSHOT")
val pomUrl = providers.gradleProperty("POM_URL").getOrElse("")
val pomScmUrl = providers.gradleProperty("POM_SCM_URL").getOrElse("")
val pomScmConnection = providers.gradleProperty("POM_SCM_CONNECTION").getOrElse("")
val pomScmDevConnection = providers.gradleProperty("POM_SCM_DEV_CONNECTION").getOrElse("")
val pomDeveloperId = providers.gradleProperty("POM_DEVELOPER_ID").getOrElse("")
val pomDeveloperName = providers.gradleProperty("POM_DEVELOPER_NAME").getOrElse("")

group = libraryGroup
version = libraryVersionName

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifact(dokkaJavadocJar)

                groupId = libraryGroup
                artifactId = libraryArtifactId
                version = libraryVersionName

                pom {
                    name.set("NFC Locator Core")
                    description.set(
                        "On-device Android library that resolves and displays a phone's NFC antenna " +
                            "location via a layered, confidence-aware resolver chain (OS-reported " +
                            "Android 14+ data, remote catalog, bundled seed catalog, heuristic fallback).",
                    )
                    url.set(pomUrl)
                    licenses {
                        license {
                            name.set("The MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set(pomDeveloperId)
                            name.set(pomDeveloperName)
                        }
                    }
                    scm {
                        url.set(pomScmUrl)
                        connection.set(pomScmConnection)
                        developerConnection.set(pomScmDevConnection)
                    }
                }
            }
        }

        repositories {
            maven {
                name = "CentralPortalStaging"
                // Central Portal's OSSRH-compatible staging endpoint. Generate a user token at
                // https://central.sonatype.com/account and export it as MAVEN_CENTRAL_USERNAME /
                // MAVEN_CENTRAL_PASSWORD - never commit real credentials here.
                url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                credentials {
                    username = System.getenv("MAVEN_CENTRAL_USERNAME")
                    password = System.getenv("MAVEN_CENTRAL_PASSWORD")
                }
            }
        }
    }

    signing {
        // In-memory ASCII-armored GPG key + passphrase, supplied only via env vars (CI secrets
        // or local shell env) - never written to a file in this repo. A SNAPSHOT build is
        // exempt (Central's staging repo doesn't require signed snapshots); any real release
        // version does, and `isRequired` makes that failure loud and immediate rather than
        // producing an unsigned artifact Central would silently reject later.
        val signingKey = System.getenv("SIGNING_KEY_IN_MEMORY")
        val signingPassword = System.getenv("SIGNING_PASSWORD")
        isRequired = !libraryVersionName.endsWith("SNAPSHOT")
        if (!signingKey.isNullOrBlank() && !signingPassword.isNullOrBlank()) {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications["release"])
        }
    }
}
