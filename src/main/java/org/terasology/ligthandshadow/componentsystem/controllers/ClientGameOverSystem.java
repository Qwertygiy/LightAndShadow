/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.components.PlayerStatisticsComponent;
import org.terasology.ligthandshadow.componentsystem.events.GameOverEvent;
import org.terasology.logic.characters.AliveCharacterComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.PlayerUtil;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.ingame.DeathScreen;
import org.terasology.rendering.nui.layouts.miglayout.MigLayout;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIText;

/**
 * Displays game over screen for all clients.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class ClientGameOverSystem extends BaseComponentSystem {

    @In
    private NUIManager nuiManager;
    @In
    private LocalPlayer localPlayer;
    @In
    private EntityManager entityManager;

    @ReceiveEvent
    public void onGameOver(GameOverEvent event, EntityRef entity) {
        nuiManager.removeOverlay(LASUtils.ONLINE_PLAYERS_OVERLAY);
        DeathScreen deathScreen = nuiManager.pushScreen(LASUtils.DEATH_SCREEN, DeathScreen.class);

        MigLayout migLayout = deathScreen.find("playerStatistics", MigLayout.class);
        if (migLayout != null) {
            Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
            for (EntityRef client : clients) {

                ClientComponent clientComponent = client.getComponent(ClientComponent.class);
                migLayout.addWidget(new UILabel(PlayerUtil.getColoredPlayerName(clientComponent.clientInfo)), new MigLayout.CCHint());

                EntityRef character = client.getComponent(ClientComponent.class).character;
                if (!character.equals(EntityRef.NULL)) {
                    PlayerStatisticsComponent playerStatisticsComponent = character.getComponent(PlayerStatisticsComponent.class);
                    migLayout.addWidget(new UILabel(String.valueOf(playerStatisticsComponent.kills)), new MigLayout.CCHint());
                    migLayout.addWidget(new UILabel(String.valueOf(playerStatisticsComponent.deaths)), new MigLayout.CCHint("wrap"));
                }
            }
        }

        UILabel gameOverDetails = deathScreen.find("gameOverDetails", UILabel.class);
        if (gameOverDetails != null) {
            if (event.winningTeam.equals(localPlayer.getCharacterEntity().getComponent(LASTeamComponent.class).team)) {
                gameOverDetails.setText("You Win!");
            } else {
                gameOverDetails.setText("You Lose!");
            }
        }
    }
}
