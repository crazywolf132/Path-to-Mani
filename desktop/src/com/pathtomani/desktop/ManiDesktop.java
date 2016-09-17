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
package com.pathtomani.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.pathtomani.common.GameOptions;
import com.pathtomani.ManiApplication;
import com.pathtomani.common.ManiFileReader;
import com.pathtomani.game.DebugOptions;
import com.pathtomani.soundtest.SoundTestListener;
import org.terasology.crashreporter.CrashReporter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ManiDesktop {
    public static void main(String[] argv) {
        if (false) {
            new LwjglApplication(new SoundTestListener(), "sound test", 800, 600);
            return;
        }

        LwjglApplicationConfiguration c = new LwjglApplicationConfiguration();
        boolean devBuild = java.nio.file.Files.exists(Paths.get("devBuild"));
        if (devBuild) {
            DebugOptions.DEV_ROOT_PATH = "main/"; // Lets the game run from source without a tweaked working directory
            c.vSyncEnabled = false; //Setting to false disables vertical sync
            c.foregroundFPS = 0; //disables foreground fps throttling
            c.backgroundFPS = 0; //disables background fps throttling
        }
        MyReader reader = new MyReader();
        DebugOptions.read(reader);


        if (DebugOptions.EMULATE_MOBILE) {
            c.width = 640;
            c.height = 480;
            c.fullscreen = false;
        } else {
            GameOptions d = new GameOptions(false, reader);
            c.width = d.x;
            c.height = d.y;
            c.fullscreen = d.fullscreen;
        }

        c.title = "Path to Mani";
        if (DebugOptions.DEV_ROOT_PATH == null) {
            c.addIcon("res/icon.png", Files.FileType.Internal);
        } else {
            c.addIcon(DebugOptions.DEV_ROOT_PATH + "res/icon.png", Files.FileType.Absolute);
        }

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, final Throwable ex) {
                // Get the exception stack trace string
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                ex.printStackTrace(printWriter);
                String exceptionString = stringWriter.getBuffer().toString();

                // Write to system.err
                System.err.println(exceptionString);

                // Create a crash dump file
                String fileName = "crash-" + new SimpleDateFormat("yyyy-dd-MM_HH-mm-ss").format(new Date()) + ".log";
                List<String> lines = Arrays.asList(exceptionString);
                Path logPath = new MyReader().create(fileName, lines).toAbsolutePath().getParent();

                // Run asynchronously so that the error message view is not blocked
                new Thread(() -> {
                    CrashReporter.report(ex, logPath);
                }).start();
            }
        });

        new LwjglApplication(new ManiApplication(), c);
    }

    private static class MyReader implements ManiFileReader {
        @Override
        public Path create(String fileName, List<String> lines) {
            if (DebugOptions.DEV_ROOT_PATH != null) {
                fileName = DebugOptions.DEV_ROOT_PATH + fileName;
            }
            Path file = Paths.get(fileName);
            try {
                java.nio.file.Files.write(file, lines, Charset.forName("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return file;
        }

        @Override
        public List<String> read(String fileName) {
            if (DebugOptions.DEV_ROOT_PATH != null) {
                fileName = DebugOptions.DEV_ROOT_PATH + fileName;
            }
            ArrayList<String> lines = new ArrayList<String>();
            try {
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                String line = "";
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
                br.close();
            } catch (IOException ignore) {
            }
            return lines;
        }
    }
}
