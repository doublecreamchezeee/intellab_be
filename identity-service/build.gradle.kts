import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.google.firebase:firebase-admin:8.1.0")
    implementation("com.google.cloud:google-cloud-firestore")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("com.nimbusds:nimbus-jose-jwt:9.15.2")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
}

tasks.withType<BootJar> {
    enabled = true
}
