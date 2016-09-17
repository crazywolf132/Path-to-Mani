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

package com.pathtomani.game.screens;

import com.pathtomani.ManiApplication;
import com.pathtomani.ui.ManiLayouts;

public class GameScreens {
  public final MainScreen mainScreen;
  public final MapScreen mapScreen;
  public final MenuScreen menuScreen;
  public final InventoryScreen inventoryScreen;
  public final TalkScreen talkScreen;

  public GameScreens(float r, ManiApplication cmp) {
    ManiLayouts layouts = cmp.getLayouts();
    RightPaneLayout rightPaneLayout = layouts.rightPaneLayout;
    mainScreen = new MainScreen(r, rightPaneLayout, cmp);
    mapScreen = new MapScreen(rightPaneLayout, cmp.isMobile(), r, cmp.getOptions());
    menuScreen = new MenuScreen(layouts.menuLayout, cmp.getOptions());
    inventoryScreen = new InventoryScreen(r, cmp.getOptions());
    talkScreen = new TalkScreen(layouts.menuLayout, cmp.getOptions());
  }

}
