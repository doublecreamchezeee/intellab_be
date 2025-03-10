import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web-services")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("org.postgresql:postgresql:42.5.0")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.hibernate:hibernate-core:6.2.10.Final")
    implementation("com.google.firebase:firebase-admin:8.1.0")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("com.cloudinary:cloudinary-http44:1.35.0")
    implementation("com.nimbusds:nimbus-jose-jwt:9.15.2")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("com.google.api-client:google-api-client:2.0.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.apis:google-api-services-people:v1-rev20220531-2.0.0")
    implementation("com.google.code.gson:gson:2.12.1")

}

tasks.withType<BootJar> {
    enabled = true
}
