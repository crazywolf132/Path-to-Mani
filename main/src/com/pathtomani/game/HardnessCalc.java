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

import com.pathtomani.common.ManiMath;
import com.pathtomani.entities.gun.GunConfig;
import com.pathtomani.entities.gun.GunItem;
import com.pathtomani.entities.item.*;
import com.pathtomani.entities.maze.MazeConfig;
import com.pathtomani.entities.planet.PlanetConfig;
import com.pathtomani.entities.planet.SysConfig;
import com.pathtomani.entities.projectile.ProjectileConfig;
import com.pathtomani.entities.ship.FarShip;
import com.pathtomani.entities.ship.ManiShip;
import com.pathtomani.entities.ship.hulls.GunSlot;
import com.pathtomani.entities.ship.hulls.Hull;
import com.pathtomani.entities.ship.hulls.HullConfig;

import java.util.Iterator;
import java.util.List;

public class HardnessCalc {

  public static final float SHIELD_MUL = 1.2f;

  public static float getGunMeanDps(GunConfig gc) {
    ClipConfig cc = gc.clipConf;
    ProjectileConfig pc = cc.projConfig;

    float projDmg = pc.dmg;
    if (pc.emTime > 0) projDmg = 150;
    else if (pc.density > 0) projDmg += 10;

    float projHitChance;
    if (pc.guideRotSpd > 0) {
      projHitChance = .9f;
    } else if (pc.zeroAbsSpd) {
      projHitChance = 0.1f;
    } else {
      projHitChance = (pc.spdLen + pc.acc) / 6;
      if (pc.physSize > 0) projHitChance += pc.physSize;
      projHitChance = ManiMath.clamp(projHitChance, .1f, 1);
      if (gc.fixed) {
        projHitChance *= .3f;
      }
    }

    float shotDmg = projDmg * projHitChance;

    return getShotDps(gc, shotDmg);
  }

  public static float getShotDps(GunConfig gc, float shotDmg) {
    ClipConfig cc = gc.clipConf;
    int projectilesPerShot = cc.projectilesPerShot;
    if (gc.timeBetweenShots == 0) projectilesPerShot = cc.size;
    if (projectilesPerShot > 1) shotDmg *= .6f * projectilesPerShot;


    float timeBetweenShots = gc.timeBetweenShots == 0 ? gc.reloadTime : gc.timeBetweenShots;
    return shotDmg / timeBetweenShots;
  }

  private static float getItemCfgDps(ItemConfig ic, boolean fixed) {
    float dps = 0;
    for (ManiItem e : ic.examples) {
      if (!(e instanceof GunItem)) throw new AssertionError("all item options must be of the same type");
      GunItem g = (GunItem) e;
      if (g.config.fixed != fixed) {
        String items = "";
        for (ManiItem ex : ic.examples) {
          items += ex.getDisplayName() + " ";
        }
        throw new AssertionError("all gun options must have equal fixed param: " + items);
      }
      dps += g.config.meanDps;
    }

    return dps / ic.examples.size() * ic.chance;
  }

  public static float getShipConfDps(ShipConfig sc, ItemManager itemManager) {
    final List<ItemConfig> parsedItems = itemManager.parseItems(sc.items);
    final List<GunSlot> unusedGunSlots = sc.hull.getGunSlotList();

    float dps = 0;
    Iterator<ItemConfig> itemConfigIterator =  parsedItems.iterator();

    while(itemConfigIterator.hasNext() && !unusedGunSlots.isEmpty()) {
        ItemConfig itemConfig = itemConfigIterator.next();
        final ManiItem item = itemConfig.examples.get(0);

        if (item instanceof GunItem) {
            final GunItem gunItem = (GunItem) item;
            final Iterator<GunSlot> gunSlotIterator = unusedGunSlots.listIterator();

            boolean matchingSlotFound = false;
            while (gunSlotIterator.hasNext() && !matchingSlotFound) {
                final GunSlot gunSlot = gunSlotIterator.next();

                if (gunItem.config.fixed != gunSlot.allowsRotation()) {
                    dps += getItemCfgDps(itemConfig, gunItem.config.fixed);
                    gunSlotIterator.remove();
                    matchingSlotFound = true;
                }
            }
        }
    }

    return dps;
  }

  public static float getShipCfgDmgCap(ShipConfig sc, ItemManager itemManager) {
    List<ItemConfig> parsed = itemManager.parseItems(sc.items);
    float meanShieldLife = 0;
    float meanArmorPerc = 0;
    for (ItemConfig ic : parsed) {
      ManiItem item = ic.examples.get(0);
      if (meanShieldLife == 0 && item instanceof Shield) {
        for (ManiItem ex : ic.examples) {
          meanShieldLife += ((Shield) ex).getLife();
        }
        meanShieldLife /= ic.examples.size();
        meanShieldLife *= ic.chance;
      }
      if (meanArmorPerc == 0 && item instanceof Armor) {
        for (ManiItem ex : ic.examples) {
          meanArmorPerc += ((Armor) ex).getPerc();
        }
        meanArmorPerc /= ic.examples.size();
        meanArmorPerc *= ic.chance;
      }
    }
    return sc.hull.getMaxLife() / (1 - meanArmorPerc) + meanShieldLife * SHIELD_MUL;
  }

  private static float getShipConfListDps(List<ShipConfig> ships) {
    float maxDps = 0;
    for (ShipConfig e : ships) {
      if (maxDps < e.dps) maxDps = e.dps;
    }
    return maxDps;
  }

  public static float getGroundDps(PlanetConfig pc, float grav) {
    float groundDps = getShipConfListDps(pc.groundEnemies);
    float bomberDps = getShipConfListDps(pc.lowOrbitEnemies);
    float res = bomberDps < groundDps ? groundDps : bomberDps;
    float gravFactor = 1 + grav * .5f;
    return res * gravFactor;
  }

  public static float getAtmDps(PlanetConfig pc) {
    return getShipConfListDps(pc.highOrbitEnemies);
  }

  public static float getMazeDps(MazeConfig c) {
    float outer = getShipConfListDps(c.outerEnemies);
    float inner = getShipConfListDps(c.innerEnemies);
    float res = inner < outer ? outer : inner;
    return res * 1.25f;
  }

  public static float getBeltDps(SysConfig c) {
    return 1.2f * getShipConfListDps(c.tempEnemies);
  }

  public static float getSysDps(SysConfig c, boolean inner) {
    return getShipConfListDps(inner ? c.innerTempEnemies : c.tempEnemies);
  }

  private static float getGunDps(GunItem g) {
    if (g == null) return 0;
    return g.config.meanDps;
  }

  public static float getShipDps(ManiShip s) {
    Hull h = s.getHull();
    return getGunDps(h.getGun(false)) + getGunDps(h.getGun(true));
  }

  public static float getFarShipDps(FarShip s) {
    return getGunDps(s.getGun(false)) + getGunDps(s.getGun(true));
  }

  public static float getShipDmgCap(ManiShip s) {
    return getDmgCap(s.getHull().config, s.getArmor(), s.getShield());
  }

  public static float getFarShipDmgCap(FarShip s) {
    return getDmgCap(s.getHullConfig(), s.getArmor(), s.getShield());
  }

  private static float getDmgCap(HullConfig hull, Armor armor, Shield shield) {
    float r = hull.getMaxLife();
    if (armor != null) r *= 1 / (1 - armor.getPerc());
    if (shield != null) r += shield.getMaxLife() * SHIELD_MUL;
    return r;
  }

  public static boolean isDangerous(float destDmgCap, float dps) {
    float killTime = destDmgCap / dps;
    return killTime < 5;
  }

  public static boolean isDangerous(float destDmgCap, Object srcObj) {
    float dps = getShipObjDps(srcObj);
    return isDangerous(destDmgCap, dps);
  }

  public static float getShipObjDps(Object srcObj) {
    return srcObj instanceof ManiShip ? getShipDps((ManiShip) srcObj) : getFarShipDps((FarShip) srcObj);
  }
}
