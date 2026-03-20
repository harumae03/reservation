package com.cgi.reservation.config;

import com.cgi.reservation.model.*;
import com.cgi.reservation.repository.ReservationRepository;
import com.cgi.reservation.repository.RestaurantTableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final RestaurantTableRepository tableRepository;
    private final ReservationRepository reservationRepository;

    public DataSeeder(RestaurantTableRepository tableRepository,
                      ReservationRepository reservationRepository) {
        this.tableRepository = tableRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void run(String... args) {
        seedTables();
        seedRandomReservations();
    }

    private void seedTables() {
        // Layout: viewBox="0 0 1000 600"
        // Top: Terrace (y: 20-130)
        // Middle: Indoor Window (left/right edges) + Indoor Main (center) (y: 170-400)
        // Bottom-right: Private Room (y: 440-560)
        // Bottom-left: Near playground

        // === TERRACE (4 tables, top row) ===
        tableRepository.save(new RestaurantTable(1, 2, Zone.TERRACE,
                120, 40, 70, 70, false, false, false));
        tableRepository.save(new RestaurantTable(2, 4, Zone.TERRACE,
                320, 40, 80, 80, false, false, false));
        tableRepository.save(new RestaurantTable(3, 4, Zone.TERRACE,
                530, 40, 80, 80, false, false, false));
        tableRepository.save(new RestaurantTable(4, 2, Zone.TERRACE,
                740, 40, 70, 70, false, false, false));

        // === INDOOR WINDOW — left side (3 tables) ===
        tableRepository.save(new RestaurantTable(5, 2, Zone.INDOOR_WINDOW,
                60, 190, 70, 70, true, false, false));
        tableRepository.save(new RestaurantTable(6, 2, Zone.INDOOR_WINDOW,
                60, 290, 70, 70, true, false, false));
        tableRepository.save(new RestaurantTable(7, 4, Zone.INDOOR_WINDOW,
                60, 390, 80, 80, true, false, false));

        // === INDOOR MAIN — center (6 tables) ===
        tableRepository.save(new RestaurantTable(8, 4, Zone.INDOOR_MAIN,
                250, 200, 80, 80, false, false, false));
        tableRepository.save(new RestaurantTable(9, 6, Zone.INDOOR_MAIN,
                430, 200, 100, 80, false, false, false));
        tableRepository.save(new RestaurantTable(10, 4, Zone.INDOOR_MAIN,
                650, 200, 80, 80, false, false, false));
        tableRepository.save(new RestaurantTable(11, 2, Zone.INDOOR_MAIN,
                250, 340, 70, 70, false, false, false));
        tableRepository.save(new RestaurantTable(12, 8, Zone.INDOOR_MAIN,
                430, 340, 120, 80, false, false, false));
        tableRepository.save(new RestaurantTable(13, 4, Zone.INDOOR_MAIN,
                650, 340, 80, 80, false, false, false));

        // === INDOOR WINDOW — right side (2 tables) ===
        tableRepository.save(new RestaurantTable(14, 2, Zone.INDOOR_WINDOW,
                850, 200, 70, 70, true, false, false));
        tableRepository.save(new RestaurantTable(15, 4, Zone.INDOOR_WINDOW,
                850, 320, 80, 80, true, false, false));

        // === PRIVATE ROOM — bottom right (3 tables) ===
        tableRepository.save(new RestaurantTable(16, 6, Zone.PRIVATE_ROOM,
                620, 470, 100, 80, false, true, false));
        tableRepository.save(new RestaurantTable(17, 8, Zone.PRIVATE_ROOM,
                780, 470, 120, 80, false, true, false));
        tableRepository.save(new RestaurantTable(18, 4, Zone.PRIVATE_ROOM,
                620, 560, 80, 70, false, true, false));

        // === Near playground — bottom left (1 table) ===
        tableRepository.save(new RestaurantTable(19, 4, Zone.INDOOR_MAIN,
                150, 470, 80, 80, false, false, true));

        log.info("Seeded {} restaurant tables", tableRepository.count());
    }

    private void seedRandomReservations() {
        List<RestaurantTable> tables = tableRepository.findAll();
        Random random = new Random();
        LocalDate today = LocalDate.now();

        String[] names = {
                "Mari Tamm", "Jaan Kask", "Liisa Mets", "Peeter Sepp",
                "Kati Rebane", "Andres Ilves", "Piret Kuusk", "Toomas Paju",
                "Anna Lepp", "Martin Saar", "Kristiina Vaher", "Robert Nurme"
        };

        int reservationCount = 10 + random.nextInt(6); // 10-15 reservations

        for (int i = 0; i < reservationCount; i++) {
            RestaurantTable table = tables.get(random.nextInt(tables.size()));
            int daysAhead = random.nextInt(7); // 0-6 days from now
            int hour = 11 + random.nextInt(10); // 11:00 - 20:00
            int minute = random.nextBoolean() ? 0 : 30;
            int duration = List.of(60, 90, 120, 150).get(random.nextInt(4));
            int partySize = 1 + random.nextInt(table.getCapacity());

            LocalDateTime startTime = LocalDateTime.of(
                    today.plusDays(daysAhead),
                    LocalTime.of(hour, minute));

            // Skip if this table already has an overlapping reservation
            LocalDateTime endTime = startTime.plusMinutes(duration);
            List<Reservation> overlapping = reservationRepository.findOverlappingForTable(
                    table, startTime, endTime);
            if (!overlapping.isEmpty()) {
                continue;
            }

            String name = names[random.nextInt(names.length)];
            Reservation reservation = new Reservation(
                    table, name, partySize, startTime, duration, null);
            reservationRepository.save(reservation);
        }

        log.info("Seeded {} random reservations", reservationRepository.count());
    }
}
