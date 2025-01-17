package com.example.courseservice;

import com.example.courseservice.filter.UserUidFilter;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableFeignClients
public class CourseServiceApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(CourseServiceApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean<UserUidFilter> userUidFilter() {
        FilterRegistrationBean<UserUidFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new UserUidFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

}
