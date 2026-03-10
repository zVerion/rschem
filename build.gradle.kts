plugins {
  id("java-library")
}

version = "0.0.2-SNAPSHOT"
group = "me.verion.rschem"

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
