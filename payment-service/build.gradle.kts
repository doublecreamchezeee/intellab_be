import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web-services")
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("jakarta.validation:jakarta.validation-api:3.0.2")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
	implementation("io.github.cdimascio:dotenv-java:3.0.0")
	implementation("org.hibernate:hibernate-core:6.2.10.Final")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("io.github.cdimascio:dotenv-java:2.2.4")
	implementation("com.google.code.gson:gson:2.12.1")

}

tasks.withType<BootJar>  {
	enabled = true
}
