/*
 * Copyright 2016 BurntGameProductions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pathtomani.managers.sound;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

import com.pathtomani.managers.GameOptions;
import com.pathtomani.managers.files.FileManager;

import java.util.ArrayList;

/**
 * Singleton class that is responsible for playing all music throughout the game.
 */
public final class MusicManager {
    private static MusicManager instance = null;
    private static final String DIR = "res/sounds/";
    private final FileHandle menuMusic;
    private ArrayList<FileHandle> gameMusic = new ArrayList<FileHandle>();;
    private Music currentlyPlaying = null;
    private FileHandle currentMusicFile = null;
    /**
     * Returns the singleton instance of this class.
     * @return The instance.
     */
    public static MusicManager getInstance() {
        if(instance == null) {
            instance = new MusicManager();
        }

        return instance;
    }

    /**
     * Initalise the MusicManager class.
     */
    private MusicManager() {
        menuMusic = FileManager.getInstance().getStaticFile("res/sounds/music/dreadnaught.ogg");
        gameMusic.add(FileManager.getInstance().getStaticFile("res/sounds/music/cimmerian dawn.ogg"));
        gameMusic.add(FileManager.getInstance().getStaticFile("res/sounds/music/into the dark.ogg"));
        gameMusic.add(FileManager.getInstance().getStaticFile("res/sounds/music/space theatre.ogg"));
    }

    /**
     * Start playing the music menu from the beginning of the track. The menu music loops continuously.
     */
    public void PlayMenuMusic(GameOptions options) {
    	if(currentMusicFile!=menuMusic)
    	{
    		StopMusic();
    		playMusic(menuMusic, options);
    		currentlyPlaying.setLooping(true);
    	}
    }

    public void PlayGameMusic(final GameOptions options) {
        if(gameMusic.contains(currentMusicFile))
        {
            int index = (gameMusic.indexOf(currentMusicFile) +1)% gameMusic.size();
            StopMusic();
            playMusic(gameMusic.get(index), options);
            currentlyPlaying.setOnCompletionListener(new Music.OnCompletionListener() {
                @Override
                public void onCompletion(Music music) {
                    PlayGameMusic(options);
                }
            });

        }else
        {
           StopMusic();
           playMusic(gameMusic.get(0), options);
        }
    }

    public void playMusic(FileHandle music, GameOptions options)
    {
        currentMusicFile = music;
    	currentlyPlaying = Gdx.audio.newMusic(music);
        currentlyPlaying.setVolume(options.musicMul);
        currentlyPlaying.play();
    }
    /**
     * Stop playing all music.
     */
    public void StopMusic() {
        if(currentlyPlaying != null)
        {
            currentlyPlaying.stop();
            currentlyPlaying.dispose();
            currentMusicFile = null;
        }
    }

    public void resetVolume(GameOptions options)
    {
        currentlyPlaying.setVolume(options.musicMul);
    }
}