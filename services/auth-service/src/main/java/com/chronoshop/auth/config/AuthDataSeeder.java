package com.chronoshop.auth.config;

import com.chronoshop.auth.domain.User;
import com.chronoshop.auth.repository.UserRepository;
import com.chronoshop.domain.enums.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeduje demo naloge (admin@chronoshop.rs, kupac@chronoshop.rs) koji moraju da rade
 * i posle dekompozicije monolita. Deo kataloga iz starog DataSeeder-a ide u catalog-service.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class AuthDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AuthDataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User admin = new User();
        admin.setFirstName("Filip");
        admin.setLastName("Administrator");
        admin.setEmail("admin@chronoshop.rs");
        admin.setPassword(passwordEncoder.encode("Admin123!"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        User customer = new User();
        customer.setFirstName("Petar");
        customer.setLastName("Petrović");
        customer.setEmail("kupac@chronoshop.rs");
        customer.setPassword(passwordEncoder.encode("Kupac123!"));
        customer.setRole(Role.CUSTOMER);
        userRepository.save(customer);

        log.info("Seed: kreirani nalozi admin@chronoshop.rs / Admin123! i kupac@chronoshop.rs / Kupac123!");
    }
}
