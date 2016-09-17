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

import com.pathtomani.common.IniReader;
import com.pathtomani.managers.files.FileManager;
import com.pathtomani.managers.files.HullConfigManager;
import com.pathtomani.entities.gun.GunItem;
import com.pathtomani.entities.item.ItemManager;
import com.pathtomani.entities.item.ManiItem;
import com.pathtomani.entities.ship.hulls.HullConfig;

import java.util.ArrayList;

public class SaveManager {

  public static final String FILE_NAME = "prevShip.ini";

  public static void writeShip(HullConfig hull, float money, ArrayList<ManiItem> items, ManiGame game) {
    String hullName = game.getHullConfigs().getName(hull);
    StringBuilder sb = new StringBuilder();
    for (ManiItem i : items) {
      sb.append(i.getCode());
      if (i.isEquipped() > 0) {
        sb.append("-" + i.isEquipped());
      }
      sb.append(" ");
      // Save gun's loaded ammo
      if (i instanceof GunItem) {
        GunItem g = (GunItem) i;
        if (g.ammo > 0 && !g.config.clipConf.infinite) {
          sb.append(g.config.clipConf.code + " ");
        }
      }
    }
    IniReader.write(FILE_NAME, "hull", hullName, "money", (int) money, "items", sb.toString());
  }

  public static boolean hasPrevShip() {
    return FileManager.getInstance().getDynamicFile(FILE_NAME).exists();
  }

  public static ShipConfig readShip(HullConfigManager hullConfigs, ItemManager itemManager) {
    IniReader ir = new IniReader(FILE_NAME, null, false);
    String hullName = ir.getString("hull", null);
    if (hullName == null) return null;
    HullConfig hull = hullConfigs.getConfig(hullName);
    if (hull == null) return null;
    int money = ir.getInt("money", 0);
    String itemsStr = ir.getString("items", "");
    return new ShipConfig(hull, itemsStr, money, 1, null, itemManager);
  }
}
