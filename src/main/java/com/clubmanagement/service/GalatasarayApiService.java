package com.clubmanagement.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GalatasarayApiService {

    private static final String TEAM_ID = "133604"; // Galatasaray on TheSportsDB
    private static final String BASE_URL = "https://www.thesportsdb.com/api/v1/json/3";

    private final RestTemplate restTemplate;

    @Data
    @AllArgsConstructor
    public static class PlayerData {
        private String firstName;
        private String lastName;
        private Integer jerseyNumber;
        private String position; // "GOALKEEPER","DEFENDER","MIDFIELDER","FORWARD"
        private String nationality;
        private String dateOfBirth;
        private Double heightCm;
        private Double weightKg;
        private String photoUrl;
    }

    @Data
    @AllArgsConstructor
    public static class MatchData {
        private String opponent;
        private String matchDate;
        private String matchTime;
        private String venue;
        private String competition;
        private boolean home;
    }

    @SuppressWarnings("unchecked")
    public List<PlayerData> fetchSquad() {
        try {
            String url = BASE_URL + "/lookup_all_players.php?id=" + TEAM_ID;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || response.get("player") == null) {
                log.warn("TheSportsDB: empty squad response, using fallback");
                return getFallbackSquad();
            }

            List<Map<String, Object>> players = (List<Map<String, Object>>) response.get("player");
            List<PlayerData> result = new java.util.ArrayList<>();

            for (Map<String, Object> p : players) {
                String fullName = str(p, "strPlayer");
                if (fullName == null || fullName.isEmpty()) continue;

                String[] parts = splitName(fullName);
                Integer num = parseIntSafe(str(p, "strNumber"));
                String pos = mapPosition(str(p, "strPosition"));
                String photo = str(p, "strThumb");
                if (photo != null && photo.isBlank()) photo = null;

                result.add(new PlayerData(
                        parts[0], parts[1], num, pos,
                        str(p, "strNationality"),
                        str(p, "dateBorn"),
                        parseDoubleSafe(str(p, "strHeight")),
                        parseDoubleSafe(str(p, "strWeight")),
                        photo
                ));
            }

            if (result.isEmpty()) {
                log.warn("TheSportsDB: 0 players parsed, using fallback");
                return getFallbackSquad();
            }

            log.info("TheSportsDB: fetched {} players for Galatasaray", result.size());
            return result;

        } catch (Exception e) {
            log.warn("TheSportsDB squad fetch failed: {}, using fallback", e.getMessage());
            return getFallbackSquad();
        }
    }

    @SuppressWarnings("unchecked")
    public List<MatchData> fetchUpcomingMatches() {
        try {
            String url = BASE_URL + "/eventsnext.php?id=" + TEAM_ID;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || response.get("events") == null) {
                log.info("TheSportsDB: no upcoming matches");
                return Collections.emptyList();
            }

            List<Map<String, Object>> events = (List<Map<String, Object>>) response.get("events");
            List<MatchData> result = new java.util.ArrayList<>();

            for (Map<String, Object> e : events) {
                String home = str(e, "strHomeTeam");
                String away = str(e, "strAwayTeam");
                boolean isHome = "Galatasaray".equalsIgnoreCase(home);
                String opponent = isHome ? away : home;
                if (opponent == null || opponent.isBlank()) continue;

                result.add(new MatchData(
                        opponent,
                        str(e, "dateEvent"),
                        str(e, "strTime"),
                        str(e, "strVenue"),
                        str(e, "strLeague"),
                        isHome
                ));
            }

            log.info("TheSportsDB: fetched {} upcoming matches", result.size());
            return result;

        } catch (Exception e) {
            log.warn("TheSportsDB match fetch failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ---- helpers ----

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof String s ? s : null;
    }

    private Integer parseIntSafe(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }

    private Double parseDoubleSafe(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s.replaceAll("[^0-9.]", "")); } catch (Exception e) { return null; }
    }

    private String[] splitName(String fullName) {
        int idx = fullName.lastIndexOf(' ');
        if (idx < 0) return new String[]{fullName, ""};
        return new String[]{fullName.substring(0, idx).trim(), fullName.substring(idx + 1).trim()};
    }

    private String mapPosition(String pos) {
        if (pos == null) return "MIDFIELDER";
        String p = pos.toLowerCase();
        if (p.contains("goalkeeper") || p.contains("keeper") || p.contains("goalie")) return "GOALKEEPER";
        if (p.contains("defender") || p.contains("back") || p.contains("centre-b")) return "DEFENDER";
        if (p.contains("forward") || p.contains("striker") || p.contains("attacker")) return "FORWARD";
        return "MIDFIELDER";
    }

    // 2025-2026 Season Squad
    public List<PlayerData> getFallbackSquad() {
        return Arrays.asList(
            // ── Goalkeepers ──────────────────────────────────────────────
            new PlayerData("Uğurcan",     "Çakır",        1,  "GOALKEEPER", "Turkish",      "1996-04-11", 189.0, 83.0, null),
            new PlayerData("Günay",       "Güvenç",      19,  "GOALKEEPER", "Turkish",      "2001-04-04", 189.0, 81.0, null),
            // ── Defenders ────────────────────────────────────────────────
            new PlayerData("Muhammed",    "Baltacı",      3,  "DEFENDER",   "Turkish",      "2002-04-08", 186.0, 78.0, null),
            new PlayerData("Ismail",      "Jakobs",       4,  "DEFENDER",   "Senegalese",   "1999-08-17", 182.0, 75.0, null),
            new PlayerData("Davinson",    "Sánchez",      6,  "DEFENDER",   "Colombian",    "1996-06-12", 192.0, 84.0, null),
            new PlayerData("Eren",        "Elmalı",      17,  "DEFENDER",   "Turkish",      "2001-03-23", 179.0, 72.0, null),
            new PlayerData("Kaan",        "Ayhan",       23,  "DEFENDER",   "Turkish",      "1994-10-10", 188.0, 83.0, null),
            new PlayerData("Abdülkerim",  "Bardakcı",    42,  "DEFENDER",   "Turkish",      "1994-09-21", 186.0, 84.0, null),
            new PlayerData("Wilfried",    "Singo",       90,  "DEFENDER",   "Ivorian",      "2000-12-25", 185.0, 80.0, null),
            // ── Midfielders ───────────────────────────────────────────────
            new PlayerData("Gabriel",     "Sara",         8,  "MIDFIELDER", "Brazilian",    "1999-11-10", 174.0, 69.0, null),
            new PlayerData("İlkay",       "Gündoğan",    20,  "MIDFIELDER", "German",       "1990-10-24", 180.0, 80.0, null),
            new PlayerData("Yáser",       "Asprilla",    22,  "MIDFIELDER", "Colombian",    "2002-07-21", 175.0, 66.0, null),
            new PlayerData("Lucas",       "Torreira",    34,  "MIDFIELDER", "Uruguayan",    "1996-02-11", 166.0, 62.0, null),
            new PlayerData("Mario",       "Lemina",      99,  "MIDFIELDER", "Gabonese",     "1993-05-01", 184.0, 80.0, null),
            // ── Forwards ──────────────────────────────────────────────────
            new PlayerData("Roland",      "Sallai",       7,  "FORWARD",    "Hungarian",    "1997-05-22", 182.0, 76.0, null),
            new PlayerData("Mauro",       "Icardi",       9,  "FORWARD",    "Argentinian",  "1993-02-19", 181.0, 82.0, null),
            new PlayerData("Leroy",       "Sané",        10,  "FORWARD",    "German",       "1996-01-11", 183.0, 75.0, null),
            new PlayerData("Yunus",       "Akgün",       11,  "FORWARD",    "Turkish",      "2000-02-16", 180.0, 74.0, null),
            new PlayerData("Victor",      "Osimhen",     45,  "FORWARD",    "Nigerian",     "1998-12-29", 186.0, 78.0, null),
            new PlayerData("Noa",         "Lang",        77,  "FORWARD",    "Dutch",        "1999-06-07", 173.0, 68.0, null),
            new PlayerData("Barış Alper", "Yılmaz",      53,  "FORWARD",    "Turkish",      "2000-07-26", 180.0, 73.0, null),
            new PlayerData("Sacha",       "Boey",        93,  "DEFENDER",   "French",       "2000-11-05", 175.0, 69.0, null)
        );
    }
}
