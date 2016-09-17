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

package com.pathtomani.managers.sound;

import com.badlogic.gdx.math.Vector2;
import com.pathtomani.common.Nullable;
import com.pathtomani.game.GameDrawer;
import com.pathtomani.game.ManiGame;
import com.pathtomani.game.ManiObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DebugHintDrawer {
  private final Map<ManiObject, DebugHint> myTracedNotes;
  private final Map<Vector2, DebugHint> myFreeNotes;

  public DebugHintDrawer() {
    myTracedNotes = new HashMap<ManiObject, DebugHint>();
    myFreeNotes = new HashMap<Vector2, DebugHint>();
  }

  public void add(@Nullable ManiObject owner, Vector2 pos, String value) {
    DebugHint dh;
    if (owner == null) {
      dh = myFreeNotes.get(pos);
      if (dh == null) {
        dh = new DebugHint(null, pos);
        myFreeNotes.put(pos, dh);
      }
    } else {
      dh = myTracedNotes.get(owner);
      if (dh == null) {
        dh = new DebugHint(owner, owner.getPosition());
        myTracedNotes.put(owner, dh);
      }
    }
    dh.add(value);
  }

  public void update(ManiGame game) {
    updateEach(game, myTracedNotes.values().iterator());
    updateEach(game, myFreeNotes.values().iterator());
  }

  private void updateEach(ManiGame game, Iterator<DebugHint> it) {
    while (it.hasNext()) {
      DebugHint n = it.next();
      n.update(game);
      if (n.shouldRemove()) it.remove();
    }
  }

  public void draw(GameDrawer drawer, ManiGame game) {
    for (DebugHint n : myTracedNotes.values()) n.draw(drawer, game);
    for (DebugHint n : myFreeNotes.values()) n.draw(drawer, game);
  }

}
