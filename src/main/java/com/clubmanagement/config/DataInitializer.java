package com.clubmanagement.config;

import com.clubmanagement.entity.*;
import com.clubmanagement.enums.*;
import com.clubmanagement.repository.*;
import com.clubmanagement.service.GalatasarayApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;
    private final GalatasarayApiService gsApi;
    private final Random rng = new Random(42);

    @Bean
    CommandLineRunner seedData(UserRepository userRepo,
                               PlayerRepository playerRepo,
                               ClubMatchRepository matchRepo,
                               TrainingSessionRepository trainingRepo,
                               DailyMealRepository mealRepo,
                               AnnouncementRepository announcementRepo,
                               InjuryReportRepository injuryRepo) {
        return args -> {
            if (userRepo.count() > 0) return;

            // ── Admin ──────────────────────────────────────────────────
            User admin = save(userRepo, "admin", "admin@gsapp.com", "admin123", RoleName.ADMIN);

            // ── Fatih Terim – Technical Director ───────────────────────
            User terim = save(userRepo, "terim.fatih", "terim.fatih@gsapp.com", "terim123", RoleName.TECHNICAL_DIRECTOR);

            // ── Assistant Coach ─────────────────────────────────────────
            User assistant = save(userRepo, "ercan.abdullah", "ercan.abdullah@gsapp.com", "assist123", RoleName.ASSISTANT_COACH);

            // ── Galatasaray Squad (from TheSportsDB or fallback) ────────
            // Use verified fallback squad (TheSportsDB free tier returns wrong team data for GS)
            List<GalatasarayApiService.PlayerData> squad = gsApi.getFallbackSquad();
            List<Player> players = new ArrayList<>();
            // Conditions for 22 players (2025-26 squad order — Icardi=2 injured, others 4-5)
            int[] conditions = {5, 4, 4, 5, 5, 4, 4, 5, 5, 5, 5, 4, 4, 4, 4, 2, 5, 4, 5, 4, 5, 4};

            for (int i = 0; i < squad.size(); i++) {
                GalatasarayApiService.PlayerData pd = squad.get(i);
                String username = toUsername(pd.getFirstName(), pd.getLastName());
                User u = save(userRepo, username, username + "@gsapp.com", "player123", RoleName.PLAYER);

                LocalDate dob = null;
                if (pd.getDateOfBirth() != null && !pd.getDateOfBirth().isBlank()) {
                    try { dob = LocalDate.parse(pd.getDateOfBirth()); } catch (Exception ignored) {}
                }

                Position pos;
                try { pos = Position.valueOf(pd.getPosition()); }
                catch (Exception e) { pos = Position.MIDFIELDER; }

                int cond = i < conditions.length ? conditions[i] : 4;
                Player p = Player.builder()
                        .user(u)
                        .firstName(pd.getFirstName())
                        .lastName(pd.getLastName())
                        .jerseyNumber(pd.getJerseyNumber())
                        .position(pos)
                        .nationality(pd.getNationality())
                        .dateOfBirth(dob)
                        .heightCm(pd.getHeightCm())
                        .weightKg(pd.getWeightKg())
                        .photoUrl(pd.getPhotoUrl())
                        .contractEndDate(LocalDate.of(2026, 6, 30))
                        .conditionRating(cond)
                        .build();
                players.add(playerRepo.save(p));
            }

            // ── Upcoming Matches (TheSportsDB) ──────────────────────────
            List<GalatasarayApiService.MatchData> apiMatches = gsApi.fetchUpcomingMatches();
            for (GalatasarayApiService.MatchData md : apiMatches) {
                LocalDateTime dt = parseMatchDateTime(md.getMatchDate(), md.getMatchTime());
                if (dt == null) continue;
                matchRepo.save(ClubMatch.builder()
                        .opponent(md.getOpponent())
                        .matchDate(dt)
                        .venue(md.getVenue() != null && !md.getVenue().isBlank() ? md.getVenue() : "RAMS Park")
                        .venueType(md.isHome() ? VenueType.HOME : VenueType.AWAY)
                        .competition(md.getCompetition() != null ? md.getCompetition() : "Süper Lig")
                        .status(MatchStatus.SCHEDULED)
                        .build());
            }

            // Played match (for history display)
            matchRepo.save(ClubMatch.builder()
                    .opponent("Fenerbahçe").matchDate(LocalDateTime.now().minusDays(10))
                    .venue("RAMS Park").venueType(VenueType.HOME)
                    .competition("Süper Lig").status(MatchStatus.PLAYED)
                    .homeScore(3).awayScore(1)
                    .notes("Harika bir derbiye imza attık! 3 puan Cimbom'un.").build());

            matchRepo.save(ClubMatch.builder()
                    .opponent("Beşiktaş").matchDate(LocalDateTime.now().minusDays(24))
                    .venue("Vodafone Park").venueType(VenueType.AWAY)
                    .competition("Süper Lig").status(MatchStatus.PLAYED)
                    .homeScore(1).awayScore(2).build());

            // ── Training Sessions ───────────────────────────────────────
            trainingRepo.save(TrainingSession.builder()
                    .sessionDate(LocalDate.now())
                    .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(12, 0))
                    .location("RAMS Park Antrenman Sahası").focus(TrainingFocus.TACTICAL)
                    .intensity(Intensity.MEDIUM).notes("Maç öncesi taktik blok çalışması")
                    .createdBy(terim).build());

            trainingRepo.save(TrainingSession.builder()
                    .sessionDate(LocalDate.now().plusDays(1))
                    .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(11, 30))
                    .location("RAMS Park Antrenman Sahası").focus(TrainingFocus.PHYSICAL)
                    .intensity(Intensity.HIGH).notes("Dayanıklılık ve sprint antrenmanı")
                    .createdBy(assistant).build());

            trainingRepo.save(TrainingSession.builder()
                    .sessionDate(LocalDate.now().plusDays(3))
                    .startTime(LocalTime.of(10, 30)).endTime(LocalTime.of(12, 30))
                    .location("RAMS Park Antrenman Sahası").focus(TrainingFocus.TECHNICAL)
                    .intensity(Intensity.MEDIUM).notes("Top kontrolü ve pas egzersizleri")
                    .createdBy(terim).build());

            trainingRepo.save(TrainingSession.builder()
                    .sessionDate(LocalDate.now().plusDays(5))
                    .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0))
                    .location("RAMS Park Antrenman Sahası").focus(TrainingFocus.RECOVERY)
                    .intensity(Intensity.LOW).notes("Maç sonrası toparlanma seansı")
                    .createdBy(assistant).build());

            // ── 3-Month Meal Plan ───────────────────────────────────────
            generateMealPlan(mealRepo);

            // ── Injury Reports (gerçekçi) ────────────────────────────────
            // Mauro Icardi – ACL injury (left out of squad Dec 2024)
            Player icardi = players.stream()
                    .filter(p -> "Icardi".equalsIgnoreCase(p.getLastName()))
                    .findFirst().orElse(null);
            if (icardi != null) {
                injuryRepo.save(InjuryReport.builder()
                        .player(icardi)
                        .reportedBy(terim)
                        .injuryType("ACL Rüptürü")
                        .bodyPart("Sol Diz")
                        .description("Antalyaspor maçında sol dizde ön çapraz bağ kopması. Cerrahi müdahale yapıldı.")
                        .injuryDate(LocalDate.of(2024, 12, 15))
                        .expectedReturnDate(LocalDate.of(2025, 9, 1))
                        .status(InjuryStatus.UNDER_TREATMENT)
                        .build());
            }

            // Torreira – muscle strain (minor)
            Player torreira = players.stream()
                    .filter(p -> "Torreira".equalsIgnoreCase(p.getLastName()))
                    .findFirst().orElse(null);
            if (torreira != null) {
                injuryRepo.save(InjuryReport.builder()
                        .player(torreira)
                        .reportedBy(assistant)
                        .injuryType("Kas Gerilmesi")
                        .bodyPart("Sağ Uyluk")
                        .description("Antrenmanda hafif uyluk gerilmesi. Kısa süreli dinlenme önerildi.")
                        .injuryDate(LocalDate.now().minusDays(3))
                        .expectedReturnDate(LocalDate.now().plusDays(5))
                        .status(InjuryStatus.UNDER_TREATMENT)
                        .build());
            }

            // ── Announcements ───────────────────────────────────────────
            announcementRepo.save(Announcement.builder()
                    .title("🏆 Galatasaray Kulüp Yönetim Sistemine Hoş Geldiniz")
                    .content("Tüm oyuncu ve teknik heyet üyeleri kişiselleştirilmiş panolarına erişebilir. " +
                             "Günlük kondisyon değerlendirmelerinizi eksiksiz giriniz. Cimbom'a başarılar!")
                    .createdBy(admin).pinned(true).build());

            announcementRepo.save(Announcement.builder()
                    .title("Maç Günü Protokolü")
                    .content("Maç günlerinde RAMS Park'a 3 saat önce gelinmesi zorunludur. " +
                             "Isınma programı Teknik Direktörümüz Fatih Terim tarafından açıklanacaktır.")
                    .targetRole(RoleName.PLAYER).createdBy(terim).pinned(false).build());

            announcementRepo.save(Announcement.builder()
                    .title("Yemek Planı Güncellemesi")
                    .content("3 aylık beslenme programı sisteme yüklendi. " +
                             "Sporcularımız öğün zamanlarına titizlikle uymalarını rica ederiz.")
                    .createdBy(admin).pinned(false).build());

            announcementRepo.save(Announcement.builder()
                    .title("Sezon Sonu Değerlendirme Toplantısı")
                    .content("Tüm teknik heyet sezon sonu değerlendirme toplantısına katılım zorunludur.")
                    .targetRole(RoleName.ASSISTANT_COACH).createdBy(terim).pinned(false).build());

            log.info("=== Galatasaray sistemi başarıyla yüklendi ===");
            log.info("Admin: admin/admin123 | Teknik Direktör: terim.fatih/terim123 | Asistan: ercan.abdullah/assist123");
            log.info("Oyuncular: <soyad.ad>@gsapp.com / player123");
        };
    }

    // ── Helpers ────────────────────────────────────────────────────────

    private User save(UserRepository repo, String username, String email, String pass, RoleName role) {
        return repo.save(User.builder()
                .username(username).email(email)
                .password(passwordEncoder.encode(pass))
                .role(role).enabled(true).build());
    }

    private String toUsername(String firstName, String lastName) {
        return normalize(lastName) + "." + normalize(firstName.replace(" ", ""));
    }

    private String normalize(String s) {
        return s.toLowerCase()
                .replace("ş", "s").replace("ç", "c").replace("ğ", "g")
                .replace("ı", "i").replace("ü", "u").replace("ö", "o")
                .replace("â", "a").replace("î", "i").replace("û", "u")
                .replace("é", "e").replace("á", "a").replace("ó", "o")
                .replace("ú", "u").replace("ñ", "n").replace("ã", "a")
                .replace(" ", "").replaceAll("[^a-z.]", "");
    }

    private LocalDateTime parseMatchDateTime(String date, String time) {
        if (date == null || date.isBlank()) return null;
        try {
            LocalDate d = LocalDate.parse(date);
            LocalTime t = LocalTime.of(20, 0);
            if (time != null && !time.isBlank()) {
                String clean = time.replace("+00:00", "").replace("Z", "").trim();
                if (clean.length() >= 5) {
                    t = LocalTime.of(
                            Integer.parseInt(clean.substring(0, 2)),
                            Integer.parseInt(clean.substring(3, 5))
                    ).plusHours(3); // UTC → Turkey time
                }
            }
            return LocalDateTime.of(d, t);
        } catch (Exception e) {
            return null;
        }
    }

    private void generateMealPlan(DailyMealRepository mealRepo) {
        // 7-day rotating menus (healthy sports nutrition, Turkish)
        String[][] breakfasts = {
            {"Yulaf ezmesi, bal, muz, ceviz", "520", "Gluten, Kuruyemiş"},
            {"Menemen (3 yumurta), tam buğday ekmek, yeşil zeytin", "480", "Yumurta, Gluten"},
            {"Granola, Yunan yoğurdu, çilek, yaban mersini", "440", "Süt, Gluten"},
            {"Haşlanmış yumurta, avokadolu tam buğday ekmek, domates", "420", "Yumurta, Gluten"},
            {"Protein smoothie: muz, süt, yulaf, protein tozu", "460", "Süt, Gluten"},
            {"Süzme peynir, bal, ceviz, tam tahıllı gevrek", "390", "Süt, Kuruyemiş, Gluten"},
            {"Kepekli waffle, meyveli yoğurt, taze meyve", "410", "Gluten, Süt"}
        };

        String[][] lunches = {
            {"Izgara tavuk göğsü, kinoa, ızgara sebze karışımı", "780", null},
            {"Somon fileto, haşlanmış brokoli, kahverengi pirinç", "820", "Balık"},
            {"Mercimek çorbası, tam buğday ekmeği, cacık", "650", "Gluten, Süt"},
            {"Tavuk şiş, bulgur pilavı, mevsim salatası", "750", "Gluten"},
            {"Ton balıklı makarna, zeytinyağı, çeri domates", "700", "Balık, Gluten"},
            {"Fırın somon, ızgara sebze, taze salata", "760", "Balık"},
            {"Izgara köfte, pilav, yanında cacık", "720", "Süt"}
        };

        String[][] dinners = {
            {"Izgara dana bonfile, haşlanmış brokoli, pilav", "850", null},
            {"Fırın tavuk but, fırın sebze güveci", "780", null},
            {"Izgara levrek, ıspanak salatası, zeytinyağlı", "690", "Balık"},
            {"Spagetti bolonez, parmezan, yeşil salata", "820", "Gluten, Süt"},
            {"Kuzu pirzola, fırın patates, ızgara mantar", "900", null},
            {"Tavuk sote, kepekli makarna, zeytinyağlı", "760", "Gluten"},
            {"Karidesli risotto, parmezan, limon", "800", "Kabuklu Deniz Ürünleri, Süt"}
        };

        String[][] snacks = {
            {"Protein bar, muz", "280", null},
            {"Badem, ceviz, fındık karışımı (30g)", "200", "Kuruyemiş"},
            {"Yunan yoğurdu, granola, bal", "230", "Süt, Gluten"},
            {"Elma dilimleri, fıstık ezmesi", "220", "Kuruyemiş"},
            {"Süt bazlı protein shake", "300", "Süt"},
            {"Humus, havuç ve salatalık dilimleri", "180", null},
            {"Karışık kuru meyve, tam tahıllı kraker", "240", "Gluten"}
        };

        // Generate 14 days ago → 105 days from now (total 120 days)
        LocalDate start = LocalDate.now().minusDays(14);
        for (int i = 0; i < 120; i++) {
            LocalDate date = start.plusDays(i);
            int idx = i % 7;

            mealRepo.save(build(date, MealType.BREAKFAST, breakfasts[idx]));
            mealRepo.save(build(date, MealType.LUNCH,     lunches[idx]));
            mealRepo.save(build(date, MealType.DINNER,    dinners[idx]));
            mealRepo.save(build(date, MealType.SNACK,     snacks[idx]));
        }
        log.info("Yemek planı oluşturuldu: 120 gün × 4 öğün = {} kayıt", 120 * 4);
    }

    private DailyMeal build(LocalDate date, MealType type, String[] data) {
        return DailyMeal.builder()
                .mealDate(date)
                .mealType(type)
                .menuDescription(data[0])
                .caloricValue(Integer.parseInt(data[1]))
                .allergenInfo(data[2])
                .build();
    }
}
