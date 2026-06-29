package xyz.tecsup.pokemon.entity;

// Almacena qué jugador está activo durante la sesión actual de juego.
// Asignado dinámicamente por StartScreen, ya sea al crear una partida
// nueva o al cargar una existente del desplegable.
public class GameSession {
    public static int playerId;
}