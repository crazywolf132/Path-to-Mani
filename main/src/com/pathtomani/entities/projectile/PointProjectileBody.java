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
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.pathtomani.common.ManiMath;
import com.pathtomani.game.ManiGame;
import com.pathtomani.game.ManiObject;
import com.pathtomani.entities.ship.ManiShip;
import com.pathtomani.common.Const;

public class PointProjectileBody implements ProjectileBody {
  private final Vector2 myPos;
  private final Vector2 mySpd;
  private final MyRayBack myRayBack;
  private final float myAcc;

  public PointProjectileBody(float angle, Vector2 muzzlePos, Vector2 gunSpd, float spdLen,
                             Projectile projectile, ManiGame game, float acc)
  {
    myPos = new Vector2(muzzlePos);
    mySpd = new Vector2();
    ManiMath.fromAl(mySpd, angle, spdLen);
    mySpd.add(gunSpd);
    myRayBack = new MyRayBack(projectile, game);
    myAcc = acc;
  }

  @Override
  public void update(ManiGame game) {
    if (myAcc > 0 && ManiMath.canAccelerate(myAcc, mySpd)) {
      float spdLen = mySpd.len();
      if (spdLen < Const.MAX_MOVE_SPD) {
        mySpd.scl((spdLen + myAcc) / spdLen);
      }
    }
    Vector2 prevPos = ManiMath.getVec(myPos);
    Vector2 diff = ManiMath.getVec(mySpd);
    diff.scl(game.getTimeStep());
    myPos.add(diff);
    ManiMath.free(diff);
    game.getObjMan().getWorld().rayCast(myRayBack, prevPos, myPos);
    ManiMath.free(prevPos);
  }

  @Override
  public Vector2 getPos() {
    return myPos;
  }

  @Override
  public void receiveForce(Vector2 force, ManiGame game, boolean acc) {
    force.scl(game.getTimeStep());
    if (!acc) force.scl(10f);
    mySpd.add(force);
  }

  @Override
  public Vector2 getSpd() {
    return mySpd;
  }

  @Override
  public void onRemove(ManiGame game) {
  }

  @Override
  public float getAngle() {
    return ManiMath.angle(mySpd);
  }

  @Override
  public void changeAngle(float diff) {
    ManiMath.rotate(mySpd, diff);
  }

  @Override
  public float getDesiredAngle(ManiShip ne) {
    return ManiMath.angle(myPos, ne.getPosition());
  }


  private class MyRayBack implements RayCastCallback {

    private final Projectile myProjectile;
    private final ManiGame myGame;

    private MyRayBack(Projectile projectile, ManiGame game) {
      myProjectile = projectile;
      myGame = game;
    }

    @Override
    public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
      ManiObject o = (ManiObject) fixture.getBody().getUserData();
      boolean oIsMassless = o instanceof Projectile && ((Projectile) o).isMassless();
      if (!oIsMassless && myProjectile.shouldCollide(o, fixture, myGame.getFactionMan())) {
        myPos.set(point);
        myProjectile.setObstacle(o, myGame);
        return 0;
      }
      return -1;
    }
  }
}
