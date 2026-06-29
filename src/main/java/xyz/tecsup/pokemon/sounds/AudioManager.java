package xyz.tecsup.pokemon.sounds;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

// Centraliza la reproducción de audio del juego: música de fondo (en loop)
// y efectos puntuales como cries de Pokémon (una sola vez).
public class AudioManager {

    // Solo puede sonar una música de fondo a la vez — se guarda la referencia
    // para poder detenerla cuando cambia de pantalla (mapa -> batalla, etc.)
    private static Clip currentMusic;

    // Reproduce un archivo en loop continuo. Detiene la música anterior si había una.
    public static void playMusic(String path) {
        stopMusic();

        try {
            URL url = AudioManager.class.getResource(path);
            if (url == null) {
                System.out.println("No se encontró el audio: " + path);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);
            currentMusic = AudioSystem.getClip();
            currentMusic.open(audioStream);
            currentMusic.loop(Clip.LOOP_CONTINUOUSLY);
            currentMusic.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Error reproduciendo música: " + e.getMessage());
        }
    }

    // Detiene la música de fondo actual, si hay alguna sonando
    public static void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.close();
            currentMusic = null;
        }
    }

    // Reproduce un efecto de sonido una sola vez (no en loop), independiente
    // de la música de fondo — usado para cries de Pokémon, sonido de pisar hierba, etc.
    public static void playSoundEffect(String path) {
        try {
            URL url = AudioManager.class.getResource(path);
            if (url == null) {
                System.out.println("No se encontró el audio: " + path);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();

            // Liberar recursos automáticamente cuando termine de sonar
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Error reproduciendo efecto: " + e.getMessage());
        }
    }

    // Reproduce el cry de un Pokémon específico según su id (1-151)
    public static void playPokemonCry(int pokemonId) {
        playSoundEffect("/PokemonCries/" + pokemonId + ".wav");
    }

    public static void playMusic(String path, float volumeDb) {
        playMusic(path);
        if (currentMusic != null && currentMusic.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) currentMusic.getControl(FloatControl.Type.MASTER_GAIN);
            float clamped = Math.clamp(volumeDb, gainControl.getMinimum(), gainControl.getMaximum());
            gainControl.setValue(clamped);
        }
    }
}