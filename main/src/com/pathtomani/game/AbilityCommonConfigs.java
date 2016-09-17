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

package com.pathtomani.game;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.pathtomani.managers.sound.SoundManager;
import com.pathtomani.gfx.GameColors;
import com.pathtomani.gfx.TextureManager;
import com.pathtomani.managers.files.FileManager;
import com.pathtomani.gfx.particle.EffectTypes;

public class AbilityCommonConfigs {
    public final AbilityCommonConfig teleport;
    public final AbilityCommonConfig emWave;
    public final AbilityCommonConfig unShield;
    public final AbilityCommonConfig knockBack;
    public final AbilityCommonConfig sloMo;

    public AbilityCommonConfigs(EffectTypes effectTypes, TextureManager textureManager, GameColors cols, SoundManager soundManager) {
        JsonReader r = new JsonReader();

        FileHandle configFile = FileManager.getInstance().getConfigDirectory().child("abilities.json");
        JsonValue node = r.parse(configFile);
        teleport = AbilityCommonConfig.load(node.get("teleport"), effectTypes, textureManager, cols, configFile, soundManager);
        emWave = AbilityCommonConfig.load(node.get("emWave"), effectTypes, textureManager, cols, configFile, soundManager);
        unShield = AbilityCommonConfig.load(node.get("unShield"), effectTypes, textureManager, cols, configFile, soundManager);
        knockBack = AbilityCommonConfig.load(node.get("knockBack"), effectTypes, textureManager, cols, configFile, soundManager);
        sloMo = AbilityCommonConfig.load(node.get("sloMo"), effectTypes, textureManager, cols, configFile, soundManager);
    }
}
