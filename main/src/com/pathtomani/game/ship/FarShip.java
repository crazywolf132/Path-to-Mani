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

package com.pathtomani.game.ship;

import com.badlogic.gdx.math.Vector2;
import com.pathtomani.game.FarObj;
import com.pathtomani.game.ManiGame;
import com.pathtomani.game.RemoveController;
import com.pathtomani.game.gun.GunItem;
import com.pathtomani.game.item.*;
import com.pathtomani.game.ship.hulls.HullConfig;
import com.pathtomani.game.input.Pilot;

public class FarShip implements FarObj {
  private final Vector2 myPos;
  private final Vector2 mySpd;
  private final Shield myShield;
  private final Armor myArmor;
  private float myAngle;
  private final float myRotSpd;
  private final Pilot myPilot;
  private final ItemContainer myContainer;
  private final HullConfig myHullConfig;
  private float myLife;
  private final GunItem myGun1;
  private final GunItem myGun2;
  private final RemoveController myRemoveController;
  private final EngineItem myEngine;
  private ShipRepairer myRepairer;
  private float myMoney;
  private final TradeContainer myTradeContainer;

  public FarShip(Vector2 pos, Vector2 spd, float angle, float rotSpd, Pilot pilot, ItemContainer container,
    HullConfig hullConfig, float life,
    GunItem gun1, GunItem gun2, RemoveController removeController, EngineItem engine,
    ShipRepairer repairer, float money, TradeContainer tradeContainer, Shield shield, Armor armor)
  {
    myPos = pos;
    mySpd = spd;
    myAngle = angle;
    myRotSpd = rotSpd;
    myPilot = pilot;
    myContainer = container;
    myHullConfig = hullConfig;
    myLife = life;
    myGun1 = gun1;
    myGun2 = gun2;
    myRemoveController = removeController;
    myEngine = engine;
    myRepairer = repairer;
    myMoney = money;
    myTradeContainer = tradeContainer;
    myShield = shield;
    myArmor = armor;

    if (myPilot.isPlayer()) {
      if (myShield != null) {
        myShield.setEquipped(1);
      }
      if (myArmor != null) {
        myArmor.setEquipped(1);
      }
      if (myGun1 != null) {
        myGun1.setEquipped(1);
      }
      if (myGun2 != null) {
        myGun2.setEquipped(2);
      }
    }
  }

  @Override
  public boolean shouldBeRemoved(ManiGame game) {
    return myRemoveController != null && myRemoveController.shouldRemove(myPos);
  }

  @Override
  public ManiShip toObj(ManiGame game) {
    return game.getShipBuilder().build(game, myPos, mySpd, myAngle, myRotSpd, myPilot, myContainer, myHullConfig, myLife, myGun1,
      myGun2, myRemoveController, myEngine, myRepairer, myMoney, myTradeContainer, myShield, myArmor);
  }

  @Override
  public void update(ManiGame game) {
    myPilot.updateFar(game, this);
    if (myTradeContainer != null) myTradeContainer.update(game);
    if (myRepairer != null) myLife += myRepairer.tryRepair(game, myContainer, myLife, myHullConfig);
  }

  @Override
  public float getRadius() {
    return myHullConfig.getApproxRadius();
  }

  @Override
  public Vector2 getPos() {
    return myPos;
  }

  @Override
  public String toDebugString() {
    return null;
  }

  @Override
  public boolean hasBody() {
    return true;
  }

  public void setPos(Vector2 pos) {
    myPos.set(pos);
  }

  public void setSpd(Vector2 spd) {
    mySpd.set(spd);
  }

  public Pilot getPilot() {
    return myPilot;
  }

  public HullConfig getHullConfig() {
    return myHullConfig;
  }

  public float getAngle() {
    return myAngle;
  }

  public Vector2 getSpd() {
    return mySpd;
  }

  public EngineItem getEngine() {
    return myEngine;
  }

  public void setAngle(float angle) {
    myAngle = angle;
  }

  public GunItem getGun(boolean secondary) {
    return secondary ? myGun2 : myGun1;
  }

  public Shield getShield() {
    return myShield;
  }

  public Armor getArmor() {
    return myArmor;
  }

  public float getLife() {
    return myLife;
  }

  public boolean mountCanFix(boolean sec) {
    final int slotNr = (sec) ? 1 : 0;

    return !myHullConfig.getGunSlot(slotNr).allowsRotation();
  }

  public float getMoney() {
    return myMoney;
  }

  public ItemContainer getIc() {
    return myContainer;
  }
}