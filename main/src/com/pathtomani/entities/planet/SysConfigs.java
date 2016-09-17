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

package com.pathtomani.entities.planet;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.pathtomani.entities.item.TradeConfig;
import com.pathtomani.gfx.TextureManager;
import com.pathtomani.common.ManiMath;
import com.pathtomani.managers.files.FileManager;
import com.pathtomani.managers.files.HullConfigManager;
import com.pathtomani.managers.ShipConfig;
import com.pathtomani.managers.chunk.SpaceEnvConfig;
import com.pathtomani.entities.item.ItemManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SysConfigs {
  private final Map<String, SysConfig> myConfigs;
  private final Map<String, SysConfig> myHardConfigs;
  private final Map<String, SysConfig> myBeltConfigs;
  private final Map<String, SysConfig> myHardBeltConfigs;

  public SysConfigs(TextureManager textureManager, HullConfigManager hullConfigs, ItemManager itemManager) {
    myConfigs = new HashMap<String, SysConfig>();
    myHardConfigs = new HashMap<String, SysConfig>();
    myBeltConfigs = new HashMap<String, SysConfig>();
    myHardBeltConfigs = new HashMap<String, SysConfig>();

    load(textureManager, hullConfigs, false, "systems.json", itemManager);
    load(textureManager, hullConfigs, true, "asteroidBelts.json", itemManager);
  }

  private void load(TextureManager textureManager, HullConfigManager hullConfigs, boolean belts, String configName,
    ItemManager itemManager)
  {
    JsonReader r = new JsonReader();
    FileHandle configFile = FileManager.getInstance().getConfigDirectory().child(configName);
    JsonValue parsed = r.parse(configFile);
    for (JsonValue sh : parsed) {
      ArrayList<ShipConfig> tempEnemies = ShipConfig.loadList(sh.get("temporaryEnemies"), hullConfigs, itemManager);
      ArrayList<ShipConfig> innerTempEnemies = ShipConfig.loadList(sh.get("innerTemporaryEnemies"), hullConfigs, itemManager);
      SpaceEnvConfig envConfig = new SpaceEnvConfig(sh.get("environment"), textureManager, configFile);

      ArrayList<ShipConfig> constEnemies = null;
      ArrayList<ShipConfig> constAllies = null;
      if (!belts) {
        constEnemies = ShipConfig.loadList(sh.get("constantEnemies"), hullConfigs, itemManager);
        constAllies = ShipConfig.loadList(sh.get("constantAllies"), hullConfigs, itemManager);
      }
      TradeConfig tradeConfig = TradeConfig.load(itemManager, sh.get("trading"), hullConfigs);
      boolean hard = sh.getBoolean("hard", false);
      SysConfig c = new SysConfig(sh.name, tempEnemies, envConfig, constEnemies, constAllies, tradeConfig, innerTempEnemies, hard);
      Map<String, SysConfig> configs = belts ? hard ? myHardBeltConfigs : myBeltConfigs : hard ? myHardConfigs : myConfigs;
      configs.put(sh.name, c);
    }
  }

  public SysConfig getRandomBelt(boolean hard) {
    Map<String, SysConfig> config = hard ? myHardBeltConfigs : myBeltConfigs;
    return ManiMath.elemRnd(new ArrayList<SysConfig>(config.values()));
  }

  public SysConfig getConfig(String name) {
    SysConfig res = myConfigs.get(name);
    if (res != null) return res;
    return myHardConfigs.get(name);
  }

  public SysConfig getRandomCfg(boolean hard) {
    Map<String, SysConfig> config = hard ? myHardConfigs : myConfigs;
    return ManiMath.elemRnd(new ArrayList<SysConfig>(config.values()));
  }

  public void addAllConfigs(ArrayList<ShipConfig> l) {
    for (SysConfig sc : myConfigs.values()) {
      l.addAll(sc.constAllies);
      l.addAll(sc.constEnemies);
      l.addAll(sc.tempEnemies);
      l.addAll(sc.innerTempEnemies);
    }
    for (SysConfig sc : myHardConfigs.values()) {
      l.addAll(sc.constAllies);
      l.addAll(sc.constEnemies);
      l.addAll(sc.tempEnemies);
      l.addAll(sc.innerTempEnemies);
    }
    for (SysConfig sc : myBeltConfigs.values()) {
      l.addAll(sc.tempEnemies);
    }
    for (SysConfig sc : myHardBeltConfigs.values()) {
      l.addAll(sc.tempEnemies);
    }
  }
}
