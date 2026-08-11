package com.chronoshop.config;

import com.chronoshop.domain.Brand;
import com.chronoshop.domain.Category;
import com.chronoshop.domain.User;
import com.chronoshop.domain.Watch;
import com.chronoshop.domain.WatchImage;
import com.chronoshop.domain.enums.Gender;
import com.chronoshop.domain.enums.MovementType;
import com.chronoshop.domain.enums.Role;
import com.chronoshop.repository.BrandRepository;
import com.chronoshop.repository.CategoryRepository;
import com.chronoshop.repository.UserRepository;
import com.chronoshop.repository.WatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    // Unsplash photo base URL
    private static final String U = "https://images.unsplash.com/photo-";

    private final UserRepository userRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final WatchRepository watchRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, BrandRepository brandRepository,
                      CategoryRepository categoryRepository, WatchRepository watchRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.watchRepository = watchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedCatalog();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;
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

    private void seedCatalog() {
        if (watchRepository.count() > 0) return;

        Map<String, Brand> brands = new HashMap<>();
        brands.put("Rolex",     brand("Rolex",     "Švajcarska", "Najpoznatiji proizvođač luksuznih satova."));
        brands.put("Omega",     brand("Omega",     "Švajcarska", "Zvanični merač vremena Olimpijskih igara."));
        brands.put("Seiko",     brand("Seiko",     "Japan",      "Japanski pionir kvarcnih i automatskih satova."));
        brands.put("TAG Heuer", brand("TAG Heuer", "Švajcarska", "Sportski i trkački hronografi."));
        brands.put("Tissot",    brand("Tissot",    "Švajcarska", "Pristupačni švajcarski satovi."));
        brands.put("Casio",     brand("Casio",     "Japan",      "Robusni i digitalni satovi, G-Shock linija."));

        Map<String, Category> cats = new HashMap<>();
        cats.put("Diver",       category("Diver",       "Ronilački satovi visoke vodootpornosti."));
        cats.put("Chronograph", category("Chronograph", "Satovi sa funkcijom štoperice."));
        cats.put("Dress",       category("Dress",       "Elegantni satovi za svečane prilike."));
        cats.put("Pilot",       category("Pilot",       "Pilotski satovi sa izraženom čitljivošću."));
        cats.put("Sport",       category("Sport",       "Robusni satovi za svakodnevnu i sportsku upotrebu."));

        seedWatch("Submariner Date", "126610LN", brands.get("Rolex"), cats.get("Diver"),
                "12150.00", 3, MovementType.AUTOMATIC, Gender.MENS, 41, 300,
                U + "1523275335684-37898b6baf30?w=800&q=80",
                U + "uTcETLUGbrI?w=800&q=80",
                U + "IIskeKAkSPU?w=800&q=80",
                U + "vRcSC-UN3yI?w=800&q=80");

        seedWatch("Speedmaster Professional", "310.30.42.50.01.001", brands.get("Omega"), cats.get("Chronograph"),
                "6400.00", 5, MovementType.MANUAL, Gender.MENS, 42, 50,
                U + "1614164185128-e4ec99c436d7?w=800&q=80",
                U + "LLOeIZFujCU?w=800&q=80",
                U + "TJrkkhdB39E?w=800&q=80",
                U + "1533139502658-0198f920d8e8?w=800&q=80");

        seedWatch("Seamaster Diver 300M", "210.30.42.20.03.001", brands.get("Omega"), cats.get("Diver"),
                "5500.00", 4, MovementType.AUTOMATIC, Gender.MENS, 42, 300,
                U + "1547996160-81dfa63595aa?w=800&q=80",
                U + "lvtJ9V_u-vo?w=800&q=80",
                U + "4eTzJaPVWjw?w=800&q=80",
                U + "1612817159949-195b6eb9e31a?w=800&q=80");

        seedWatch("Prospex Turtle", "SRPE05K1", brands.get("Seiko"), cats.get("Diver"),
                "520.00", 12, MovementType.AUTOMATIC, Gender.MENS, 45, 200,
                U + "1612817159949-195b6eb9e31a?w=800&q=80",
                U + "nOhRrpADKXk?w=800&q=80",
                U + "O5wdTV1NOQA?w=800&q=80",
                U + "X6N7UrDkKL0?w=800&q=80");

        seedWatch("Presage Cocktail", "SRPB43J1", brands.get("Seiko"), cats.get("Dress"),
                "430.00", 8, MovementType.AUTOMATIC, Gender.UNISEX, 40, 50,
                U + "1495856458515-0637185db551?w=800&q=80",
                U + "bFzNGTK4TxM?w=800&q=80",
                U + "1524805444758-089113d48a6d?w=800&q=80",
                U + "1523275335684-37898b6baf30?w=800&q=80");

        seedWatch("Carrera Chronograph", "CBN2A1B.BA0643", brands.get("TAG Heuer"), cats.get("Chronograph"),
                "5300.00", 2, MovementType.AUTOMATIC, Gender.MENS, 44, 100,
                U + "1533139502658-0198f920d8e8?w=800&q=80",
                U + "zwOt_4R1hjc?w=800&q=80",
                U + "tO9IXa92S_s?w=800&q=80",
                U + "1614164185128-e4ec99c436d7?w=800&q=80");

        seedWatch("PRX Powermatic 80", "T137.407.11.041.00", brands.get("Tissot"), cats.get("Sport"),
                "650.00", 20, MovementType.AUTOMATIC, Gender.UNISEX, 40, 100,
                U + "1434056886845-dac89ffe9b56?w=800&q=80",
                U + "jaJAE77sZWw?w=800&q=80",
                U + "1547996160-81dfa63595aa?w=800&q=80",
                U + "1508057198894-247b23fe5ade?w=800&q=80");

        seedWatch("Le Locle", "T006.407.11.033.00", brands.get("Tissot"), cats.get("Dress"),
                "390.00", 15, MovementType.AUTOMATIC, Gender.MENS, 39, 30,
                U + "1524805444758-089113d48a6d?w=800&q=80",
                U + "1495856458515-0637185db551?w=800&q=80",
                U + "r-UaSYUqF9o?w=800&q=80",
                U + "1523275335684-37898b6baf30?w=800&q=80");

        seedWatch("G-Shock Mudmaster", "GG-B100-1A", brands.get("Casio"), cats.get("Sport"),
                "320.00", 25, MovementType.QUARTZ, Gender.MENS, 53, 200,
                U + "1517336714731-489689fd1ca8?w=800&q=80",
                U + "tmiw3BYz-jI?w=800&q=80",
                U + "NCwc-9zRRUs?w=800&q=80",
                U + "MQs3wF6APLY?w=800&q=80");

        seedWatch("Pilot Chronograph", "CBN2A1B.FC6492", brands.get("TAG Heuer"), cats.get("Pilot"),
                "4800.00", 0, MovementType.AUTOMATIC, Gender.MENS, 43, 100,
                U + "1508057198894-247b23fe5ade?w=800&q=80",
                U + "zwOt_4R1hjc?w=800&q=80",
                U + "lXzHngQ0WPY?w=800&q=80",
                U + "1533139502658-0198f920d8e8?w=800&q=80");

        log.info("Seed: ubačeno {} satova, {} brendova, {} kategorija.",
                watchRepository.count(), brandRepository.count(), categoryRepository.count());
    }

    private Brand brand(String name, String country, String description) {
        Brand b = new Brand();
        b.setName(name);
        b.setCountry(country);
        b.setDescription(description);
        return brandRepository.save(b);
    }

    private Category category(String name, String description) {
        Category c = new Category();
        c.setName(name);
        c.setDescription(description);
        return categoryRepository.save(c);
    }

    private void seedWatch(String name, String ref, Brand brand, Category cat, String price, int stock,
                           MovementType movement, Gender gender, int diameter, int waterResistance,
                           String... images) {
        Watch w = new Watch();
        w.setName(name);
        w.setReferenceNumber(ref);
        w.setBrand(brand);
        w.setCategory(cat);
        w.setDescription(brand.getName() + " " + name + " — referenca " + ref + ".");
        w.setPrice(new BigDecimal(price));
        w.setStockQuantity(stock);
        w.setMovement(movement);
        w.setGender(gender);
        w.setCaseDiameterMm(diameter);
        w.setWaterResistanceM(waterResistance);
        w.setActive(true);
        w.setImageUrl(images.length > 0 ? images[0] : null);

        for (int i = 0; i < images.length; i++) {
            WatchImage img = new WatchImage();
            img.setWatch(w);
            img.setUrl(images[i]);
            img.setSortOrder(i);
            w.getImages().add(img);
        }

        watchRepository.save(w);
    }
}
