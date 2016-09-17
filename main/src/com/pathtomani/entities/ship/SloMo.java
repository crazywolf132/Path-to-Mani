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

package com.pathtomani.entities.ship;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.pathtomani.common.ManiMath;
import com.pathtomani.game.AbilityCommonConfig;
import com.pathtomani.game.ManiGame;
import com.pathtomani.managers.dra.DraLevel;
import com.pathtomani.entities.item.ItemManager;
import com.pathtomani.entities.item.ManiItem;
import com.pathtomani.gfx.particle.ParticleSrc;

public class SloMo implements ShipAbility {
  private static final float SLO_MO_CHG_SPD = .03f;
  private final Config myConfig;

  private float myFactor;

  public SloMo(Config config) {
    myConfig = config;
    myFactor = 1;
  }

  @Override
  public AbilityConfig getConfig() {
    return myConfig;
  }

  @Override
  public AbilityCommonConfig getCommonConfig() {
    return myConfig.cc;
  }

  @Override
  public float getRadius() {
    return Float.MAX_VALUE;
  }

  @Override
  public boolean update(ManiGame game, ManiShip owner, boolean tryToUse) {
    if (tryToUse) {
      myFactor = myConfig.factor;
      Vector2 pos = owner.getPosition();
      ParticleSrc src = new ParticleSrc(myConfig.cc.effect, -1, DraLevel.PART_BG_0, new Vector2(), true, game, pos, owner.getSpd(), 0);
      game.getPartMan().finish(game, src, pos);
      return true;
    }
    float ts = game.getTimeStep();
    myFactor = ManiMath.approach(myFactor, 1, SLO_MO_CHG_SPD * ts);
    return false;
  }

  public float getFactor() {
    return myFactor;
  }


  public static class Config implements AbilityConfig {
    public final float factor;
    public final float rechargeTime;
    private final ManiItem chargeExample;
    private final AbilityCommonConfig cc;

    public Config(float factor, float rechargeTime, ManiItem chargeExample, AbilityCommonConfig cc)
    {
      this.factor = factor;
      this.rechargeTime = rechargeTime;
      this.chargeExample = chargeExample;
      this.cc = cc;
    }

    @Override
    public ShipAbility build() {
      return new SloMo(this);
    }

    @Override
    public ManiItem getChargeExample() {
      return chargeExample;
    }

    @Override
    public float getRechargeTime() {
      return rechargeTime;
    }

    @Override
    public void appendDesc(StringBuilder sb) {
      sb.append("Time slow down to ").append((int) (factor * 100)).append("%\n");
    }

    public static AbilityConfig load(JsonValue abNode, ItemManager itemManager, AbilityCommonConfig cc) {
      float factor = abNode.getFloat("factor");
      float rechargeTime = abNode.getFloat("rechargeTime");
      ManiItem chargeExample = itemManager.getExample("sloMoCharge");
      return new Config(factor, rechargeTime, chargeExample, cc);
    }
  }
}
