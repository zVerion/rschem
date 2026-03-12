plugins {
  id("java-library")
  id("maven-publish")
}

group = project.findProperty("group") ?: "me.verion.rschem"
version = project.findProperty("version") ?: "0.0.2-SNAPSHOT"

repositories {
  mavenCentral()
  maven("https://repo.purpurmc.org/snapshots")
}

dependencies {
  // lombok
  "compileOnly"(libs.lombok)
  "annotationProcessor"(libs.lombok)
  // general
  "compileOnly"(libs.annotations)
  "compileOnly"(libs.purpur)
  "implementation"(libs.zstd.jni)
  // testing
  "testImplementation"(libs.bundles.junit)
}

tasks.withType<JavaCompile> {
  sourceCompatibility = JavaVersion.VERSION_21.toString()
  targetCompatibility = JavaVersion.VERSION_21.toString()
  // options
  options.encoding = "UTF-8"
  options.isIncremental = true
}

tasks.getByName<Test>("test") {
  useJUnitPlatform()
}

extensions.configure<JavaPluginExtension> {
  withSourcesJar()
  withJavadocJar()
}

extensions.configure<PublishingExtension> {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])

      pom {
        name.set("rschem")
        description.set("A high-performance schematic library for procedural Minecraft world generation.")
        url.set("https://github.com/zVerion/rschem")

        licenses {
          license {
            name.set("MIT License")
            url.set("https://opensource.org/licenses/MIT")
          }
        }

        developers {
          developer {
            name.set("verion")
            email.set("zverion.geschaeft@gmail.com")
          }
        }

        scm {
          url.set("https://github.com/zVerion/rschem")
          connection.set("scm:git:https://github.com/zVerion/rschem.git")
        }

        issueManagement {
          system.set("GitHub Issues")
          url.set("https://github.com/zVerion/rschem/issues")
        }
      }
    }
  }

  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/zVerion/rschem")
      credentials {
        username = project.findProperty("gpr.user")?.toString() ?: System.getenv("GITHUB_ACTOR")
        password = project.findProperty("gpr.token")?.toString() ?: System.getenv("GITHUB_TOKEN")
      }
    }
  }
}
