/*
 *  Copyright (c) 2023, jones (https://jonesdev.xyz) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jones.antipopup;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerData;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiPopUpPlugin extends JavaPlugin {

    @Override
    public void onLoad() {

        // build the packet api based on the spigot packet events builder
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));

        // initialize the packet api
        PacketEvents.getAPI().getSettings()

                // do not check for updates
                // this is just to prevent console spam
                .checkForUpdates(false)

                // disable b-stats
                .bStats(false)

                // disable debug mode
                .debug(false);

        // load the packet events base api
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {

        // initialize packet events
        PacketEvents.getAPI().init();

        // check server version to avoid issues
        if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_19_1)) {
            Bukkit.getLogger().severe("This plugin can only run on 1.19.1+ servers.");
            onDisable();
            return;
        }

        // register listener for right clicking
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListenerAbstract() {

            @Override
            public void onPacketSend(final PacketSendEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.SERVER_DATA) {
                    event.setCancelled(true);

                    final WrapperPlayServerServerData wrapper = new WrapperPlayServerServerData(event);

                    final WrapperPlayServerServerData rewritten = new WrapperPlayServerServerData(wrapper.getMOTD(),
                            wrapper.getIcon().orElse(null),
                            wrapper.isPreviewsChat(),
                            wrapper.isEnforceSecureChat()
                    );

                    // yes, it actually is that simple
                    event.getUser().sendPacket(rewritten);
                }
            }
        });
    }

    @Override
    public void onDisable() {

        // terminate packet events
        if (PacketEvents.getAPI().isInitialized()) {
            PacketEvents.getAPI().terminate();
        }
    }
}
