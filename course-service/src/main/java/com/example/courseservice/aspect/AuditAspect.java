package com.example.courseservice.aspect;


import com.example.courseservice.utils.UserContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {
    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;


    @Transactional
    @Before("@annotation(org.springframework.transaction.annotation.Transactional)")
    public void setAuditUser() {
        String context = UserContext.getCurrentUser();

        if (context == null || context.isEmpty()) {
            System.out.println("No user context found, skipping audit user setting.");
            return;
        }
        String userId = context.split(",")[0];
        String userRole = context.split(",")[1];
        System.out.println("Aspect: "+userId);

        if (userId != null) {
            userId = userId.replace("'", "''"); // Remove curly braces if present
            jdbcTemplate.execute("SET LOCAL audit.user_id = '" + userId + "'");
        }
        if (userRole != null) {
            userRole = userRole.replace("'", "''"); // Remove curly braces if present
            jdbcTemplate.execute("SET LOCAL audit.user_role = '" + userRole + "'");
        }
    }
}
