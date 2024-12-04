dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-web-services")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    runtimeOnly("org.postgresql:postgresql")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}