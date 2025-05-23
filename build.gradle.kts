//import com.google.protobuf.gradle.id
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.0.5.RELEASE"
    id("com.bmuschko.docker-spring-boot-application") version "9.4.0"
    id("com.google.protobuf") version "0.9.3"
    kotlin("jvm") version "1.9.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2023.0.3"

subprojects {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "kotlin")

    group = "com.example"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    dependencies {
        // Common dependencies
        implementation("org.springframework.boot:spring-boot-starter")
        implementation("io.github.cdimascio:dotenv-java:3.0.0")
        implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")

        // Testing
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        }
    }

    tasks.withType<Test> {
        enabled = false
    }
}

// Docker Spring Boot application configuration
docker {
    springBootApplication {
        baseImage.set("openjdk:17-jdk-slim") // Using OpenJDK 17 as the base image
        maintainer.set("nguyen.thanhtri2112@gmail.com")
        ports.set(listOf(8080, 8081)) // Example ports to expose
        images.set(setOf("intellab_ops/intellab:latest")) // Image name for the built Docker image

        // Additional JVM arguments (e.g., for Spring profile)
        jvmArgs.set(listOf("-Dspring.profiles.active=prod"))
    }
}

/*
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.23.0"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.56.0"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")
            }
        }
    }
}
*/

tasks.withType<BootJar> {
    enabled = false
}

