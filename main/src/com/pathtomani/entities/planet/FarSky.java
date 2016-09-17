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

import com.badlogic.gdx.math.Vector2;
import com.pathtomani.common.Const;
import com.pathtomani.game.FarObj;
import com.pathtomani.game.ManiGame;
import com.pathtomani.game.ManiObject;

public class FarSky implements FarObj {
  private final Planet myPlanet;

  public FarSky(Planet planet) {
    myPlanet = planet;
  }

  @Override
  public boolean shouldBeRemoved(ManiGame game) {
    return false;
  }

  @Override
  public ManiObject toObj(ManiGame game) {
    return new Sky(game, myPlanet);
  }

  @Override
  public void update(ManiGame game) {
  }

  @Override
  public float getRadius() {
    return myPlanet.getGroundHeight() + Const.MAX_SKY_HEIGHT_FROM_GROUND;
  }

  @Override
  public Vector2 getPos() {
    return myPlanet.getPos();
  }

  @Override
  public String toDebugString() {
    return null;
  }

  @Override
  public boolean hasBody() {
    return false;
  }
}
