package com.ticketsystem.config;

import com.ticketsystem.model.KnowledgeArticle;
import com.ticketsystem.model.User;
import com.ticketsystem.model.enums.Category;
import com.ticketsystem.model.enums.Role;
import com.ticketsystem.repository.KnowledgeArticleRepository;
import com.ticketsystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final KnowledgeArticleRepository knowledgeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      KnowledgeArticleRepository knowledgeRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.knowledgeRepository = knowledgeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedKnowledge();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;

        userRepository.save(User.builder()
                .name("IT Admin")
                .email("it.admin@company.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .department("IT")
                .build());

        userRepository.save(User.builder()
                .name("HR Admin")
                .email("hr.admin@company.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .department("HR")
                .build());

        userRepository.save(User.builder()
                .name("John Employee")
                .email("john@company.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.EMPLOYEE)
                .department("Engineering")
                .build());
    }

    private void seedKnowledge() {
        if (knowledgeRepository.count() > 0) return;

        knowledgeRepository.save(KnowledgeArticle.builder()
                .title("How to reset your password")
                .content("Go to Settings > Security > Reset Password. You will receive an email with instructions.")
                .category(Category.IT)
                .keywords("password reset login forgot")
                .build());

        knowledgeRepository.save(KnowledgeArticle.builder()
                .title("VPN setup guide")
                .content("Download the VPN client from the company portal. Use your AD credentials. Contact IT if issues persist.")
                .category(Category.IT)
                .keywords("vpn connection remote network")
                .build());

        knowledgeRepository.save(KnowledgeArticle.builder()
                .title("How to apply for leave")
                .content("Submit a leave request through the HR portal. Your manager will approve via email.")
                .category(Category.HR)
                .keywords("leave vacation time off pto")
                .build());

        knowledgeRepository.save(KnowledgeArticle.builder()
                .title("Reporting a bug effectively")
                .content("Include OS, browser version, steps to reproduce, and screenshots. The more detail, the faster the fix.")
                .category(Category.BUG)
                .keywords("bug report steps reproduce error")
                .build());

        knowledgeRepository.save(KnowledgeArticle.builder()
                .title("Payroll FAQ")
                .content("Payroll is processed on the last working day of every month. Discrepancies must be reported within 7 days.")
                .category(Category.HR)
                .keywords("payroll salary pay slip")
                .build());
    }
}
