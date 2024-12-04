plugins {
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.0.5.RELEASE"
    id("com.bmuschko.docker-spring-boot-application") version "9.4.0"
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
        enabled = false;
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

// Task to build Docker image for API Gateway
task("buildApiGatewayImage") {
    dependsOn(":api-gateway:build") // Ensure API Gateway is built before Docker image creation
    group = "docker"
    description = "Build the Docker image for api-gateway"

    doLast {
        exec {
            commandLine = listOf("docker", "build", "-t", "api-gateway-image:latest", "./api-gateway")
        }
        println("Built Docker image for api-gateway")
    }
}

// Task to build Docker image for Identity Service
task("buildIdentityServiceImage") {
    dependsOn(":identity-service:build") // Ensure Identity Service is built before Docker image creation
    group = "docker"
    description = "Build the Docker image for identity-service"

    doLast {
        exec {
            commandLine = listOf("docker", "build", "-t", "identity-service-image:latest", "./identity-service")
        }
        println("Built Docker image for identity-service")
    }
}

// Task to build Docker image for Course Service
task("buildCourseServiceImage") {
    dependsOn(":course-service:build") // Ensure Course Service is built before Docker image creation
    group = "docker"
    description = "Build the Docker image for course-service"

    doLast {
        exec {
            commandLine = listOf("docker", "build", "-t", "course-service-image:latest", "./course-service")
        }
        println("Built Docker image for course-service")
    }
}

// Task to bring up all Docker services using Docker Compose
task("dockerComposeUp") {
    dependsOn("buildApiGatewayImage", "buildIdentityServiceImage", "buildCourseServiceImage") // Ensure all images are built
    group = "docker"
    description = "Start all Docker services using docker-compose"

    doLast {
        exec {
            commandLine = listOf("docker-compose", "up", "--build")
        }
        println("All Docker containers are up!")
    }
}
