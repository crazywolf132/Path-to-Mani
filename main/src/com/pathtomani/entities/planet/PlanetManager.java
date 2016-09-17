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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.pathtomani.common.ManiMath;
import com.pathtomani.managers.files.HullConfigManager;
import com.pathtomani.game.*;
import com.pathtomani.entities.maze.Maze;
import com.pathtomani.entities.maze.MazeConfigs;
import com.pathtomani.entities.ship.hulls.HullConfig;
import com.pathtomani.common.Const;
import com.pathtomani.gfx.GameColors;
import com.pathtomani.gfx.TextureManager;
import com.pathtomani.gfx.ManiColor;
import com.pathtomani.entities.item.ItemManager;
import com.pathtomani.entities.ship.ManiShip;
import com.pathtomani.entities.ship.hulls.Hull;

import java.util.ArrayList;
import java.util.List;

public class PlanetManager {

  private final ArrayList<ManiSystem> mySystems;
  private final ArrayList<Planet> myPlanets;
  private final ArrayList<SystemBelt> myBelts;
  private final FlatPlaceFinder myFlatPlaceFinder;
  private final PlanetConfigs myPlanetConfigs;
  private final MazeConfigs myMazeConfigs;
  private final ArrayList<Maze> myMazes;
  private final SunSingleton mySunSingleton;
  private final SysConfigs mySysConfigs;
  private final PlanetCoreSingleton myPlanetCore;
  private Planet myNearestPlanet;

  public PlanetManager(TextureManager textureManager, HullConfigManager hullConfigs, GameColors cols, ItemManager itemManager) {
    myPlanetConfigs = new PlanetConfigs(textureManager, hullConfigs, cols, itemManager);
    mySysConfigs = new SysConfigs(textureManager, hullConfigs, itemManager);
    myMazeConfigs = new MazeConfigs(textureManager, hullConfigs, itemManager);

    mySystems = new ArrayList<ManiSystem>();
    myMazes = new ArrayList<Maze>();
    myPlanets = new ArrayList<Planet>();
    myBelts = new ArrayList<SystemBelt>();
    myFlatPlaceFinder = new FlatPlaceFinder();
    mySunSingleton = new SunSingleton(textureManager);
    myPlanetCore = new PlanetCoreSingleton(textureManager);
  }

  public void fill(ManiNames names) {
    new SystemsBuilder().build(mySystems, myPlanets, myBelts, myPlanetConfigs, myMazeConfigs, myMazes, mySysConfigs, names);
  }

  public void update(ManiGame game) {
    Vector2 camPos = game.getCam().getPos();
    for (int i = 0, myPlanetsSize = myPlanets.size(); i < myPlanetsSize; i++) {
      Planet p = myPlanets.get(i);
      p.update(game);
    }
    for (int i = 0, myMazesSize = myMazes.size(); i < myMazesSize; i++) {
      Maze m = myMazes.get(i);
      m.update(game);
    }

    myNearestPlanet = getNearestPlanet(camPos);

    ManiSystem nearestSys = getNearestSystem(camPos);
    applyGrav(game, nearestSys);
  }

  public Planet getNearestPlanet(Vector2 pos) {
    float minDst = Float.MAX_VALUE;
    Planet res = null;
    for (int i = 0, myPlanetsSize = myPlanets.size(); i < myPlanetsSize; i++) {
      Planet p = myPlanets.get(i);
      float dst = pos.dst(p.getPos());
      if (dst < minDst) {
        minDst = dst;
        res = p;
      }
    }
    return res;
  }

  private void applyGrav(ManiGame game, ManiSystem nearestSys) {
    float npGh = myNearestPlanet.getGroundHeight();
    float npFh = myNearestPlanet.getFullHeight();
    float npMinH = myNearestPlanet.getMinGroundHeight();
    Vector2 npPos = myNearestPlanet.getPos();
    Vector2 sysPos = nearestSys.getPos();
    float npGravConst = myNearestPlanet.getGravConst();

    List<ManiObject> objs = game.getObjMan().getObjs();
    for (int i = 0, objsSize = objs.size(); i < objsSize; i++) {
      ManiObject obj = objs.get(i);
      if (!obj.receivesGravity()) continue;

      Vector2 objPos = obj.getPosition();
      float minDist;
      Vector2 srcPos;
      float gravConst;
      boolean onPlanet;
      float toNp = npPos.dst(objPos);
      float toSys = sysPos.dst(objPos);
      if (toNp < npFh) {
        if (recoverObj(obj, toNp, npMinH)) continue;
        minDist = npGh;
        srcPos = npPos;
        gravConst = npGravConst;
        onPlanet = true;
      } else if (toSys < Const.SUN_RADIUS) {
        minDist = SunSingleton.SUN_HOT_RAD;
        srcPos = sysPos;
        gravConst = SunSingleton.GRAV_CONST;
        onPlanet = false;
      } else {
        continue;
      }

      Vector2 grav = ManiMath.getVec(srcPos);
      grav.sub(objPos);
      float len = grav.len();
      grav.nor();
      if (len < minDist) {
        len = minDist;
      }
      float g = gravConst / len / len;
      grav.scl(g);
      obj.receiveForce(grav, game, true);
      ManiMath.free(grav);
      if (!onPlanet) {
        mySunSingleton.doDmg(game, obj, toSys);
      }
    }

  }

  private boolean recoverObj(ManiObject obj, float toNp, float npMinH) {
    if (npMinH < toNp) return false;
    if (!(obj instanceof ManiShip)) return false;
    ManiShip ship = (ManiShip) obj;
    Hull hull = ship.getHull();
    if (hull.config.getType() == HullConfig.Type.STATION) return false;
    float fh = myNearestPlanet.getFullHeight();
    Vector2 npPos = myNearestPlanet.getPos();
    Vector2 toShip = ManiMath.distVec(npPos, ship.getPosition());
    float len = toShip.len();
    if (len == 0) {
      toShip.set(0, fh);
    } else {
      toShip.scl(fh / len);
    }
    toShip.add(npPos);
    Body body = hull.getBody();
    body.setTransform(toShip, 0);
    body.setLinearVelocity(Vector2.Zero);
    ManiMath.free(toShip);
    return true;
  }

  public Planet getNearestPlanet() {
    return myNearestPlanet;
  }

  public void drawDebug(GameDrawer drawer, ManiGame game) {
    if (DebugOptions.DRAW_PLANET_BORDERS) {
      ManiCam cam = game.getCam();
      float lineWidth = cam.getRealLineWidth();
      float vh = cam.getViewHeight();
      for (Planet p : myPlanets) {
        Vector2 pos = p.getPos();
        float angle = p.getAngle();
        float fh = p.getFullHeight();
        Color col = p == myNearestPlanet ? ManiColor.W : ManiColor.G;
        drawer.drawCircle(drawer.debugWhiteTex, pos, p.getGroundHeight(), col, lineWidth, vh);
        drawer.drawCircle(drawer.debugWhiteTex, pos, fh, col, lineWidth, vh);
        drawer.drawLine(drawer.debugWhiteTex, pos.x, pos.y, angle, fh, col, lineWidth);
      }

    }
  }

  public ArrayList<Planet> getPlanets() {
    return myPlanets;
  }

  public ArrayList<SystemBelt> getBelts() {
    return myBelts;
  }

  public ArrayList<ManiSystem> getSystems() {
    return mySystems;
  }

  public Vector2 findFlatPlace(ManiGame game, Planet p, ConsumedAngles takenAngles,
                               float objHalfWidth) {
    return myFlatPlaceFinder.find(game, p, takenAngles, objHalfWidth);
  }

  public ArrayList<Maze> getMazes() {
    return myMazes;
  }

  public ManiSystem getNearestSystem(Vector2 pos) {
    float minDst = Float.MAX_VALUE;
    ManiSystem res = null;
    for (int i = 0, mySystemsSize = mySystems.size(); i < mySystemsSize; i++) {
      ManiSystem s = mySystems.get(i);
      float dst = pos.dst(s.getPos());
      if (dst < minDst) {
        minDst = dst;
        res = s;
      }
    }
    return res;
  }

  public Maze getNearestMaze(Vector2 pos) {
    float minDst = Float.MAX_VALUE;
    Maze res = null;
    for (int i = 0, myMazesSize = myMazes.size(); i < myMazesSize; i++) {
      Maze m = myMazes.get(i);
      float dst = pos.dst(m.getPos());
      if (dst < minDst) {
        minDst = dst;
        res = m;
      }
    }
    return res;
  }

  public void drawSunHack(ManiGame game, GameDrawer drawer) {
    mySunSingleton.draw(game, drawer);
  }

  public void drawPlanetCoreHack(ManiGame game, GameDrawer drawer) {
    myPlanetCore.draw(game, drawer);
  }
}
