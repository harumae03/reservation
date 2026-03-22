# Verdant Bistro — Nutikas Restorani Reserveerimissüsteem

CGI suvepraktika proovitöö. Veebirakendus restorani laudade broneerimiseks, mis soovitab kliendile parimat lauda seltskonna suuruse ja eelistuste põhjal.

![Tech Stack](https://img.shields.io/badge/Spring%20Boot-4.0.4-green) ![Java](https://img.shields.io/badge/Java-25-orange) ![React](https://img.shields.io/badge/React-19-blue) ![Vite](https://img.shields.io/badge/Vite-8-purple)

---

## Rakenduse käivitamine

### Variant 1: Docker (soovituslik)

Eelduseks on [Docker Desktop](https://www.docker.com/products/docker-desktop/) olemasolu.

```bash
git clone <repo-url>
cd reservation
docker compose up --build
```

Oodake kuni mõlemad konteinerid on käivitunud (backend logis näete `Started ReservationApplication`). Rakendus avaneb aadressil:

**http://localhost:3000**

Peatamiseks: `Ctrl+C` ja `docker compose down`.

### Variant 2: Lokaalne arenduskeskkond

Eeldused:
- **Java 25+** ([Eclipse Temurin](https://adoptium.net/) või Oracle JDK)
- **Node.js 22+** ([nodejs.org](https://nodejs.org/))

```bash
git clone <repo-url>
cd reservation

# 1. Käivita backend (port 8080)
./mvnw spring-boot:run

# 2. Ava uus terminal ja käivita frontend (port 5173)
cd frontend
npm install
npm run dev
```

Rakendus avaneb aadressil **http://localhost:5173**. Frontend suunab `/api` päringud automaatselt backendile (Vite proxy).

> **Märkus:** Andmebaas on H2 in-memory — iga taaskäivitusega andmed kaovad ja genereeritakse uued juhuslikud broneeringud.

### Testide käivitamine

```bash
./mvnw test
```

Käivitab 10 testi (skooralgoritmi ühiktestid + kontrolleri integratsioonitestid).

---

## Funktsionaalsus

### 1. Broneeringu otsing ja filtreerimine

Kasutaja näeb restorani SVG saaliplaani ja saab filtreerida vabu laudu:

- **Kuupäev** — tänasest edasi
- **Kellaaeg** — 30-minutiliste sammudega (11:00–21:30)
- **Seltskonna suurus** — 1–12 inimest (üle 8 aktiveerib laudade liitmise)
- **Tsoon** — Terrass, Sisesaal, Aknakoht, Privaatruum
- **Eelistused** — Akna ääres, Vaikne nurk, Mängunurga lähedal
- **Külastuse kestus** — 1h, 1.5h, 2h, 2.5h, 3h

Restoran on avatud **11:00–22:00**. Rakendus arvutab automaatselt järgmise mõistliku algusaja.

### 2. Laua soovitamine (skooralgoritm)

Rakendus hindab vabu laudu kolme kriteeriumiga (max 100 punkti):

| Kriteerium | Max punktid | Loogika |
|-----------|------------|---------|
| **Mahutavus** | 40 | Täpne match = 40p. Iga üleliigne koht −10p. |
| **Tsoon** | 35 | Soovitud tsoon = 35p. Filter puudub = 10p. |
| **Eelistused** | 25 | Normaliseeritud: `(kattuvad / soovitud) × 25`. Mängunurga läheduse puhul kasutatakse kauguspõhist skoori (max raadiusega 450 ühikut). |

Soovitused sorteeritakse: eelistustele vastavad lauad ees, seejärel tsooni match, seejärel koondskoor.

### 3. Visuaalne saali plaan

SVG-põhine interaktiivne saali plaan (viewBox `0 0 1040 770`) koos 19 lauaga 4 tsoonis:

- **Roheline** — saadaval
- **Hall** (viirutatud) — hõivatud
- **Oranž/roheline helendus** — soovitatud (top 3 roheline, ülejäänud oranž)
- **Sinine** — valitud

Laua peal on näha number ja kohtade arv. Parim soovitus saab "PARIM" märgise. Hõivatud lauad genereeritakse juhuslikult igal rakenduse käivitusel, nagu ülesanne nõuab.

### 4. Broneeringu tegemine

Klikk saadaval/soovitatud laual avab broneerimisvormi. Kasutaja sisestab nime ning kinnitab kuupäeva, aja ja seltskonna suuruse. Eduka broneeringu järel kuvatakse toast-teade ja saali plaan uueneb.

Valideerimine: nimi kohustuslik, seltskonna suurus 1–20, kellaaeg restorani lahtioleku ajal, topeltbroneeringu kontroll (HTTP 409).

---

## Boonusfunktsioonid

### Dünaamiline laudade liitmine

Kui suurt seltskonda (nt 10 inimest) ei saa paigutada ühte lauda, soovitab süsteem kahte kõrvuti asuvat lauda sama tsooni sees, mida saab kokku lükata. Liidetud laudade puhul:
- Soovitusalgoritm otsib kõik sama tsooni saadaval laudade paarid, mille keskpunktide vaheline kaugus on alla 220 SVG ühiku
- Liidetud laudade koondmahutavus peab katma seltskonna suuruse
- Saali plaanil kuvatakse liidetud laudade vahel katkendlik ühendusliin
- Broneeringu tegemisel luuakse mõlemale lauale eraldi broneering sama kliendi nimega

### Admin vaade

Lihtne liides restorani omanikule laudade haldamiseks:
- Ülemine navigatsioon sisaldab "Admin" nuppu vaate vahetamiseks
- Admin vaates on filtripaneel ja soovitused peidetud
- Lauad on lohistatavad — koordinaadid teisendatakse SVG ruumis `getScreenCTM().inverse()` abil
- Positsiooni muutus salvestatakse kohe `PUT /api/tables/{id}/position` kaudu
- Hõivatud laudade all kuvatakse broneeringu info: kliendi nimi, kellaaeg ja seltskonna suurus

### Külastuse kestuse arvestamine

Kasutaja valib külastuse kestuse (1–3h). Saadavuse arvutus arvestab olemasolevate broneeringute kestvusega — kui 2h pärast vabaneb laud, on see uuesti broneeritav.

### TheMealDB päevapraad

Sidebar kuvab 3 juhuslikku päevapraadi [TheMealDB](https://www.themealdb.com/) API-st. Tulemused on cache'itud päeva kaupa.

### Docker

Multi-stage build mõlemale:
- **Backend**: `eclipse-temurin:25-jdk` (build) → `eclipse-temurin:25-jre` (runtime)
- **Frontend**: `node:22-alpine` (build) → `nginx:alpine` (serveerib staatilist buildi + proxy `/api` → backend)

### Automaattestid

- **`TableRecommendationServiceTest`** — skooralgoritmi ühiktestid (täpne match, üleliigsed kohad, eelistuste normaliseerimine, tsooni skoor)
- **`ReservationControllerTest`** — `@WebMvcTest` integratsioonitestid (201 Created, 409 Conflict, 204 No Content)

---

## Projekti struktuur

```
reservation/
├── src/main/java/com/cgi/reservation/
│   ├── model/          — JPA olemid (RestaurantTable, Reservation, Zone, ReservationStatus)
│   ├── repository/     — Spring Data JPA repod (overlap-päringud native SQL-iga)
│   ├── service/        — Äriloogika (saadavus, soovitused, broneeringud, TheMealDB)
│   ├── controller/     — REST API (TableController, ReservationController, MealController)
│   ├── dto/            — Andmeedastuse objektid
│   └── config/         — DataSeeder, CORS, GlobalExceptionHandler
├── frontend/src/
│   ├── components/
│   │   ├── FilterPanel/         — Filtrite paneel
│   │   ├── FloorPlan/           — SVG saali plaan + TableShape
│   │   ├── RecommendationList/  — Soovituste nimekiri
│   │   ├── ReservationForm/     — Broneerimise modaalaken
│   │   └── DailySpecials/       — TheMealDB päevapraad
│   ├── services/api.js          — API kõnede kiht
│   └── types/index.js           — JSDoc tüübid ja konstandid
├── Dockerfile                   — Backend multi-stage build
├── frontend/Dockerfile          — Frontend multi-stage build
└── docker-compose.yml           — Orkestratsioon (backend:8080 + frontend:3000)
```

## API lõpp-punktid

| Meetod | URL | Kirjeldus |
|--------|-----|-----------|
| `GET` | `/api/tables` | Kõik lauad |
| `GET` | `/api/tables/status?dateTime=` | Laudade staatused konkreetsel ajal |
| `GET` | `/api/tables/recommend?dateTime&partySize&duration&zone&preferences` | Soovitused |
| `POST` | `/api/reservations` | Loo broneering |
| `DELETE` | `/api/reservations/{id}` | Tühista broneering |
| `GET` | `/api/reservations?date=` | Broneeringud kuupäeva järgi |
| `PUT` | `/api/tables/{id}/position` | Uuenda laua positsiooni (admin) |
| `GET` | `/api/meals/daily-specials` | TheMealDB päevapraad |


---

## Tööprotsess ja ajakulu

Töö toimus kolmel päeval. Commitide ajalugu peegeldab arengut samm-sammult.

### Päev 1 (20. märts) — Alus (~4h)
- Projekti seadistamine (Spring Boot 4.0.4 + React 19 + Vite 8)
- JPA olemid, enumid, repod ja H2 seadistus
- DataSeeder juhuslike broneeringute genereerimiseks
- Frontend baastemplate

### Päev 2 (21. märts) — Põhifunktsionaalsus + boonused (~9h)
- DTO-d, teenused, kontrollerid, REST API
- Ühiktestid ja HQL → native SQL migratsioon (Hibernate 7 tõttu)
- Frontend disaini prototüüpimine Google Stitch'i AI canvas'el
- React frontend: SVG saali plaan, filtrid, soovitused, broneerimise voog
- Bean Validation ja ärireeglite valideerimine
- TheMealDB integratsioon eestikeelsete tõlgetega
- 30-minutilised ajaslotid (asendas vaba tekstivälja)
- Kauguspõhine mängunurga skoor
- Mobiilne responsiivsus ja edge case'id
- Docker multi-stage builds + docker-compose

### Päev 3 (22. märts) — Boonused + lõplik polish (~4h)
- Dünaamiline laudade liitmine (backend loogika + frontend)
- Admin vaade drag-and-drop laudade paigutamiseks
- Broneeringu info kuvamine admin vaates
- README dokumentatsioon

**Kokku: ~17h**

### Keerulised kohad

- **Spring Boot 4 + Hibernate 7** — HQL `FUNCTION()` ei tööta Hibernate 7-ga, pidin ümber kirjutama native SQL päringuteks koos `DATEADD` funktsiooniga. Lahendasin Spring Boot dokumentatsiooni ja Hibernate 7 migratsiooni juhendi abil.
- **SVG saali plaan** — toolide (seats) paigutamine ümber erineva suurusega laudade oli geomeetriliselt keeruline. Lõpuks kasutasin dünaamilist arvutust laua suuruse ja kohtade arvu põhjal.
- **Soovitusalgoritmi tasakaalustamine** — pidin katsetama erinevaid kaalusid, et algoritm ei eelistaks liiga suuri laudu ega ignoreeriks eelistusi. Lõplik jaotus (40/35/25) annab mõistliku tulemuse.
- **Jackson 3.x importimised** — Spring Boot 4.0.4 kasutab Jackson 3.x-i (`tools.jackson.databind` nimeruumi), mis erineb varasemast `com.fasterxml.jackson`-ist.
- **Admin vaate SVG drag-and-drop** — hiire koordinaatide teisendamine SVG viewBox ruumi nõudis `getScreenCTM().inverse()` kasutamist, et arvestada SVG skaleerimise ja `preserveAspectRatio`-ga.

---

## Kasutatud tööriistad ja viited

- **[Claude Code](https://claude.com/claude-code)** (Anthropic) — AI-abiline koodi kirjutamisel ja arhitektuuri planeerimisel. Kasutatud läbivalt backend ja frontend arenduses.
- **[Google Stitch](https://stitch.withgoogle.com/)** — Frontend disaini kavandamine. Kasutasin Stitch'i AI-põhist lõuendit (canvas) esialgse UI layout'i ja komponentide visuaalse prototüübi loomiseks. Kirjeldasin soovitud kasutajakogemust ja disainistiili (sage green teema, restorani atmosfäär), mille põhjal Stitch genereeris high-fidelity ekraanivaated. Sealt eksportisin DESIGN.md disainisüsteemi (värvid, fondid, spacing), mida kasutasin lõpliku CSS-i alusena.
- **[TheMealDB](https://www.themealdb.com/)** — Avalik retsepti-API päevapraadi soovituste jaoks.
- **[Material Symbols Outlined](https://fonts.google.com/icons)** — Ikoonifont.
- **[Spring Boot Documentation](https://docs.spring.io/spring-boot/)** — Spring Boot 4.x konfiguratsiooni ja migratsiooni juhised.

---

## Ülesande nõuete katvus

| Nõue | Staatus | Märkused |
|------|---------|----------|
| Broneeringu otsing ja filtreerimine | ✅ | Kuupäev, kellaaeg, seltskonna suurus, tsoon, eelistused |
| Laua soovitamine ja paigutuse loogika | ✅ | Skooralgoritm (mahutavus + eelistused + tsoon) |
| Visuaalne saali plaan | ✅ | SVG interaktiivne plaan, hõivatud vs saadaval vs soovitatud |
| Juhuslikud broneeringud | ✅ | DataSeeder genereerib 10–15 broneeringut igal käivitusel |
| Spring Boot + viimane Java LTS | ✅ | Spring Boot 4.0.4, Java 25 |
| Versioonikontroll (Git) | ✅ | 15 committi 3 päeva jooksul |
| Keskmine külastuse aeg | ✅ | Kasutaja valib kestuse, saadavus arvestab sellega |
| Dünaamiline laudade liitmine | ✅ | Kõrvuti asuvad lauad liidus suurele seltskonnale |
| Admin vaade | ✅ | Drag-and-drop paigutus + broneeringu info kuvamine |
| Väline API (TheMealDB) | ✅ | 3 juhuslikku päevapraadi, cache'itud päeva kaupa |
| Testid | ✅ | Ühik- ja integratsioonitestid (10 testi) |
| Docker | ✅ | Multi-stage builds + docker-compose |
