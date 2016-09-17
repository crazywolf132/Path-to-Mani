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

package com.pathtomani.entities.projectile;

import com.badlogic.gdx.math.Vector2;
import com.pathtomani.game.ManiGame;
import com.pathtomani.entities.ship.ManiShip;

public interface ProjectileBody {
  void update(ManiGame game);
  Vector2 getPos();
  Vector2 getSpd();
  void receiveForce(Vector2 force, ManiGame game, boolean acc);
  void onRemove(ManiGame game);
  float getAngle();
  void changeAngle(float diff);
  float getDesiredAngle(ManiShip ne);
}
