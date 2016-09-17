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

package com.pathtomani.entities.gun;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.pathtomani.gfx.TextureManager;
import com.pathtomani.common.ManiMath;
import com.pathtomani.managers.files.FileManager;
import com.pathtomani.game.DmgType;
import com.pathtomani.entities.item.*;
import com.pathtomani.entities.projectile.ProjectileConfig;
import com.pathtomani.managers.sound.ManiSound;
import com.pathtomani.managers.sound.SoundManager;
import com.pathtomani.game.HardnessCalc;

public class GunConfig {
  public final float minAngleVar;
  public final float maxAngleVar;
  public final float angleVarDamp;
  public final float angleVarPerShot;
  public final float timeBetweenShots;
  public final float reloadTime;
  public final float gunLength;
  public final String displayName;
  public final TextureAtlas.AtlasRegion tex;
  public final boolean lightOnShot;
  public final int price;
  public final String desc;
  public final float dps;
  public final GunItem example;
  public final ClipConfig clipConf;
  public final ManiSound shootSound;
  public final ManiSound reloadSound;
  public final TextureAtlas.AtlasRegion icon;
  public final boolean fixed;
  public final float meanDps;
  public final ManiItemType itemType;
  public final float texLenPerc;
  public final String code;

  public GunConfig(float minAngleVar, float maxAngleVar, float angleVarDamp, float angleVarPerShot,
                   float timeBetweenShots,
                   float reloadTime, float gunLength, String displayName,
                   boolean lightOnShot, int price,
                   ClipConfig clipConf, ManiSound shootSound, ManiSound reloadSound, TextureAtlas.AtlasRegion tex,
                   TextureAtlas.AtlasRegion icon, boolean fixed, ManiItemType itemType, float texLenPerc, String code)
  {
    this.shootSound = shootSound;
    this.reloadSound = reloadSound;

    this.tex = tex;

    this.maxAngleVar = maxAngleVar;
    this.minAngleVar = minAngleVar;
    this.angleVarDamp = angleVarDamp;
    this.angleVarPerShot = angleVarPerShot;
    this.timeBetweenShots = timeBetweenShots;
    this.reloadTime = reloadTime;
    this.gunLength = gunLength;
    this.displayName = displayName;
    this.lightOnShot = lightOnShot;
    this.price = price;
    this.clipConf = clipConf;
    this.icon = icon;
    this.fixed = fixed;
    this.itemType = itemType;
    this.texLenPerc = texLenPerc;
    this.code = code;

    dps = HardnessCalc.getShotDps(this, clipConf.projConfig.dmg);
    meanDps = HardnessCalc.getGunMeanDps(this);
    this.desc = makeDesc();
    example = new GunItem(this, 0, 0);
  }

  private String makeDesc() {
    StringBuilder sb = new StringBuilder();
    ProjectileConfig pc = clipConf.projConfig;
    sb.append(fixed ? "Heavy gun (no rotation)\n" : "Light gun (auto rotation)\n");
    if (pc.dmg > 0) {
      sb.append("Dmg: ").append(ManiMath.nice(dps)).append("/s\n");
      DmgType dmgType = pc.dmgType;
      if (dmgType == DmgType.ENERGY) sb.append("Weak against armor\n");
      else if (dmgType == DmgType.BULLET) sb.append("Weak against shields\n");
    } else if (pc.emTime > 0) {
      sb.append("Disables enemy ships for ").append(ManiMath.nice(pc.emTime)).append(" s\n");
    }
    if (pc.density > 0) {
      sb.append("Knocks enemies back\n");
    }
    sb.append("Reload: ").append(ManiMath.nice(reloadTime)).append(" s\n");
    if (clipConf.infinite) {
      sb.append("Infinite ammo\n");
    } else {
      sb.append("Uses ").append(clipConf.plural).append("\n");
    }
    return sb.toString();
  }

  public static void load(TextureManager textureManager, ItemManager itemManager, SoundManager soundManager, ManiItemTypes types) {
    JsonReader r = new JsonReader();
    FileHandle configFile = FileManager.getInstance().getItemsDirectory().child("guns.json");
    JsonValue parsed = r.parse(configFile);
    for (JsonValue sh : parsed) {
      float minAngleVar = sh.getFloat("minAngleVar", 0);
      float maxAngleVar = sh.getFloat("maxAngleVar");
      float angleVarDamp = sh.getFloat("angleVarDamp");
      float angleVarPerShot = sh.getFloat("angleVarPerShot");
      float timeBetweenShots = sh.getFloat("timeBetweenShots");
      float reloadTime = sh.getFloat("reloadTime");
      float gunLength = sh.getFloat("gunLength");
      float texLenPerc = sh.getFloat("texLenPerc", 1);
      String texName = sh.getString("texName");
      String displayName = sh.getString("displayName");
      boolean lightOnShot = sh.getBoolean("lightOnShot", false);
      int price = sh.getInt("price");
      String clipName = sh.getString("clipName");
      ClipConfig clipConf = clipName.isEmpty() ? null : ((ClipItem) itemManager.getExample(clipName)).getConfig();
      String reloadSoundPath = sh.getString("reloadSound");
      ManiSound reloadSound = soundManager.getSound(reloadSoundPath, configFile);
      String shootSoundPath = sh.getString("shootSound");
      float shootPitch = sh.getFloat("shootSoundPitch", 1);
      ManiSound shootSound = soundManager.getPitchedSound(shootSoundPath, configFile, shootPitch);
      TextureAtlas.AtlasRegion tex = textureManager.getTex("smallGameObjs/guns/" + texName, configFile);
      TextureAtlas.AtlasRegion icon = textureManager.getTex(TextureManager.ICONS_DIR + texName, configFile);
      boolean fixed = sh.getBoolean("fixed", false);
      String code = sh.name;
      ManiItemType itemType = fixed ? types.fixedGun : types.gun;
      GunConfig c = new GunConfig(minAngleVar, maxAngleVar, angleVarDamp, angleVarPerShot, timeBetweenShots, reloadTime,
        gunLength, displayName, lightOnShot, price, clipConf, shootSound, reloadSound, tex, icon, fixed, itemType, texLenPerc, code);
      itemManager.registerItem(c.example);
    }
  }
}
