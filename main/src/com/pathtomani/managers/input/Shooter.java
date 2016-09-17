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

package com.pathtomani.managers.input;

import com.badlogic.gdx.math.Vector2;
import com.pathtomani.common.ManiMath;
import com.pathtomani.entities.gun.GunItem;
import com.pathtomani.entities.projectile.ProjectileConfig;
import com.pathtomani.entities.ship.ManiShip;
import com.pathtomani.entities.gun.GunMount;

public class Shooter {

  public static final float E_SPD_PERC = .6f; // 0 means that target speed is not considered, 1 means that it's fully considered
  public static final float MIN_SHOOT_AAD = 2f;
  private boolean myShoot;
  private boolean myShoot2;
  private boolean myRight;
  private boolean myLeft;

  public Shooter() {
  }

  public void update(ManiShip ship, Vector2 enemyPos, boolean dontRotate, boolean canShoot, Vector2 enemySpd,
                     float enemyApproxRad)
  {
    myLeft = false;
    myRight = false;
    myShoot = false;
    myShoot2 = false;
    Vector2 shipPos = ship.getPosition();
    if (enemyPos == null || !canShoot) return;
    float toEnemyDst = enemyPos.dst(shipPos);

    GunItem g1 = processGun(ship, false);
    GunItem g2 = processGun(ship, true);
    if (g1 == null && g2 == null) return;

    float projSpd = 0;
    GunItem g = null;
    if (g1 != null) {
      ProjectileConfig projConfig = g1.config.clipConf.projConfig;
      projSpd = projConfig.spdLen + projConfig.acc; // for simplicity
      g = g1;
    }
    if (g2 != null) {
      ProjectileConfig projConfig = g2.config.clipConf.projConfig;
      float g2PS = projConfig.spdLen + projConfig.acc; // for simplicity
      if (projSpd < g2PS) {
        projSpd = g2PS;
        g = g2;
      }
    }

    Vector2 gunRelPos = ship.getHull().getGunMount(g == g2).getRelPos();
    Vector2 gunPos = ManiMath.toWorld(gunRelPos, ship.getAngle(), shipPos);
    float shootAngle = calcShootAngle(gunPos, ship.getSpd(), enemyPos, enemySpd, projSpd, false);
    ManiMath.free(gunPos);
    if (shootAngle != shootAngle) return;
    {
      // ok this is a hack
      float toShip = ManiMath.angle(enemyPos, shipPos);
      float toGun = ManiMath.angle(enemyPos, gunPos);
      shootAngle += toGun - toShip;
    }
    float shipAngle = ship.getAngle();
    float maxAngleDiff = ManiMath.angularWidthOfSphere(enemyApproxRad, toEnemyDst) + 10f;
    ProjectileConfig projConfig = g.config.clipConf.projConfig;
    if (projSpd > 0 && projConfig.guideRotSpd > 0) maxAngleDiff += projConfig.guideRotSpd * toEnemyDst / projSpd;
    if (ManiMath.angleDiff(shootAngle, shipAngle) < maxAngleDiff) {
      myShoot = true;
      myShoot2 = true;
      return;
    }

    if (dontRotate) return;
    Boolean ntt = Mover.needsToTurn(shipAngle, shootAngle, ship.getRotSpd(), ship.getRotAcc(), MIN_SHOOT_AAD);
    if (ntt != null) {
      if (ntt) myRight = true; else myLeft = true;
    }
  }
  
  // returns gun if it's fixed & can shoot
  private GunItem processGun(ManiShip ship, boolean second) {
    GunMount mount = ship.getHull().getGunMount(second);
    if (mount == null) return null;
    GunItem g = mount.getGun();
    if (g == null || g.ammo <= 0) return null;

    if (g.config.clipConf.projConfig.zeroAbsSpd || g.config.clipConf.projConfig.guideRotSpd > 0) {
      if (second) myShoot2 = true; else myShoot = true;
      return null;
    }

    if (g.config.fixed) return g;

    if (mount.isDetected()) {
      if (second) myShoot2 = true; else myShoot = true;
    }
    return null;
  }

  public boolean isShoot() {
    return myShoot;
  }

  public boolean isShoot2() {
    return myShoot2;
  }

  public boolean isLeft() {
    return myLeft;
  }

  public boolean isRight() {
    return myRight;
  }

  public static float calcShootAngle(Vector2 gunPos, Vector2 gunSpd, Vector2 ePos, Vector2 eSpd, float projSpd,
    boolean sharp)
  {
    Vector2 eSpdShortened = ManiMath.getVec(eSpd);
    if (!sharp) eSpdShortened.scl(E_SPD_PERC);
    Vector2 relESpd = ManiMath.distVec(gunSpd, eSpdShortened);
    ManiMath.free(eSpdShortened);
    float rotAngle = ManiMath.angle(relESpd);
    float v = relESpd.len();
    float v2 = projSpd;
    ManiMath.free(relESpd);
    Vector2 toE = ManiMath.distVec(gunPos, ePos);
    ManiMath.rotate(toE, -rotAngle);
    float x = toE.x;
    float y = toE.y;
    float a = v * v - v2 * v2;
    float b = 2 * x * v;
    float c = x * x + y * y;
    float t = ManiMath.genQuad(a, b, c);
    float res;
    if (t != t) {
      res = Float.NaN;
    } else {
      toE.x += t * v;
      res = ManiMath.angle(toE) + rotAngle;
    }
    ManiMath.free(toE);
    return res;
  }
}
