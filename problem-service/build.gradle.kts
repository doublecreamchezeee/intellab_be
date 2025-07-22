import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("io.fabric8:kubernetes-client:6.8.1")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("org.zeroturnaround:zt-zip:1.15")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    runtimeOnly("org.postgresql:postgresql")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.8.3")
}

tasks.withType<BootJar> {
    enabled = true
}
