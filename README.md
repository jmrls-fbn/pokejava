# Pokejava

Juego Pokémon estilo GBA en Java Swing (mapa, colisiones, encuentros en hierba
alta y batallas por turnos), con persistencia en **SQLite** (archivo local, sin
servidor).

Refactor de [`ProyectoPokemonOld`](../ProyectoPokemonOld): la lógica del juego
es la misma, pero la base de datos pasó de MySQL/PostgreSQL a SQLite embebido.
Ya no hace falta Docker ni un gestor de base de datos aparte: el juego se
distribuye como un único JAR y crea su propia base la primera vez que arranca.

## Requisitos previos

- JDK 21
- Maven

## Ejecutar la aplicación

```bash
mvn compile exec:java
```

o generar el jar ejecutable:

```bash
mvn clean package
java -jar target/Pokejava.jar
```

La primera vez que se ejecuta, el juego crea el archivo `pokemon.db` en el
directorio actual y lo llena con los datos de Pokémon, movimientos, stats, etc.
(empaquetados como CSV dentro del JAR) más el jugador de prueba "Ash". En los
siguientes arranques simplemente abre ese archivo, así que el progreso persiste.

Controles: flechas o WASD para moverse, ESPACIO para interactuar.

## Reinicializar la base de datos

Para empezar de cero (borra todas las partidas guardadas), basta con eliminar
el archivo de base de datos; se volverá a crear en el próximo arranque:

```bash
rm pokemon.db
```

## Estructura

```
Pokejava/
├── pom.xml
└── src/main/
    ├── java/xyz/tecsup/pokemon/
    │   ├── config/      (DatabaseConfig + DatabaseInitializer: conexión SQLite y carga inicial)
    │   ├── repository/  (acceso a datos, una clase por entidad)
    │   ├── entity/      (modelos del juego)
    │   ├── battle/      (lógica y UI de combate)
    │   ├── map/         (lectura/render de mapas Tiled + colisiones)
    │   ├── control/     (input de teclado)
    │   ├── sounds/      (AudioManager)
    │   └── main/        (Main, GamePanel, StartScreen, MenuBar)
    └── resources/
        ├── db/
        │   ├── schema.sql   (esquema SQLite, se ejecuta en la primera carga)
        │   └── data/        (CSVs fuente: pokemon, moves, stats, evoluciones...)
        └── ...              (sprites, mapas, audio)
```
