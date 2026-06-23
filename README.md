# Pokejava

Juego Pokémon estilo GBA en Java Swing (mapa, colisiones, encuentros en hierba
alta y batallas por turnos), con persistencia en PostgreSQL.

Refactor de [`ProyectoPokemonOld`](../ProyectoPokemonOld): la lógica del juego
es la misma, pero la base de datos migró de MySQL a PostgreSQL con
inicialización 100% automática vía Docker, y el acceso a datos usa un
connection pool (HikariCP) en vez de una conexión única hardcodeada.

## Requisitos previos

- Docker y Docker Compose
- JDK 21
- Maven

## Paso 1: Clonar y preparar

```bash
git clone <url-del-repo>
cd Pokejava
cp .env.example .env
```

## Paso 2: Levantar la base de datos

```bash
docker compose up -d
```

Qué pasa automáticamente:

- Descarga la imagen de Postgres 16 y crea el contenedor
- Ejecuta en orden los scripts de `db/init/`:
  - `01_schema.sql` → crea las tablas vacías
  - `02_load.sql` → carga todos los CSV de `db/data/` con `COPY` (en segundos)
  - `03_constraints.sql` → agrega llaves primarias, foráneas e índices
  - `04_seed.sql` → crea el jugador de prueba (Ash) con su equipo inicial
- El healthcheck confirma que la base está lista antes de que la app se conecte

## Paso 3: Ejecutar la aplicación

```bash
mvn compile exec:java
```

o generar el jar ejecutable:

```bash
mvn clean package
java -jar target/Pokejava.jar
```

Controles: flechas o WASD para moverse, ESPACIO para interactuar.

## Reinicializar la base de datos

Los scripts de `db/init/` solo corren la primera vez que se crea el volumen.

```bash
docker compose down -v   # borra el volumen, fuerza que se vuelvan a ejecutar
docker compose up -d
```

- `docker compose down` (sin `-v`) seguido de `up`: los datos persisten.
- `docker compose down -v`: borra todo y vuelve a cargar desde los CSV/scripts.

## Estructura

```
Pokejava/
├── docker-compose.yml
├── .env.example
├── pom.xml
├── db/
│   ├── init/              (scripts SQL, se ejecutan en orden alfabético)
│   └── data/               (CSVs fuente: pokemon, moves, stats, evoluciones...)
└── src/main/java/xyz/tecsup/Pokemon/
    ├── config/              (DatabaseConfig: pool HikariCP)
    ├── repository/          (acceso a datos, una clase por entidad)
    ├── entidades/           (modelos del juego)
    ├── batalla/             (lógica y UI de combate)
    ├── mapa/                (lectura/render de mapas Tiled + colisiones)
    ├── Control/             (input de teclado)
    └── main/                (Main, GamePanel, BarraMenu)
```
