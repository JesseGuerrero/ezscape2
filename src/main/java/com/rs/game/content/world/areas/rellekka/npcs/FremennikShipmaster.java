// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//  Copyright (C) 2021 Trenton Kress
//  This file is part of project: Darkan
//
package com.rs.game.content.world.areas.rellekka.npcs;

import com.rs.engine.dialogue.Conversation;
import com.rs.engine.dialogue.HeadE;
import com.rs.game.content.skills.dungeoneering.DamonheimController;
import com.rs.game.model.entity.player.Player;
import com.rs.lib.game.Tile;

public class FremennikShipmaster extends Conversation {

	static final int SELECTION_NONE=0;
	static final int SELECTION_DAEMONHEIM=1;
	static final int SELECTION_GRUMPY=2;

	public FremennikShipmaster(Player player, int npcId, boolean backing) {
		this(player, npcId, backing, SELECTION_NONE);
	}

	private FremennikShipmaster(Player player, int npcId, boolean backing, int previousSelection) {
		super(player);

		addNPC(npcId, HeadE.CONFUSED, backing ? "Do you want a lift back to the south?" : "You want passage to Daemonheim?");
		addOptions(ops -> {
			ops.add("Yes, please.", () -> sail(player, backing));
			ops.add("Not right now, thanks.");

			if (npcId == 9708) {
				// Al-Kharid to Daemonheim Shipmaster
				if (previousSelection != SELECTION_DAEMONHEIM) {
					ops.add("Daemonheim?")
							.addPlayer(HeadE.CONFUSED, "Daemonheim?")
							.addNPC(npcId, HeadE.FRUSTRATED, "Yes, the icy peninsula far to the north of here.")
							.addNPC(npcId, HeadE.CALM_TALK, "Ice, snow, harsh winds...")
							.addNPC(npcId, HeadE.SAD_MILD, "...and no sand or swamp sludge, clogging up every orifice.")
							.addNPC(npcId, HeadE.SAD_MILD, "Are you done with questions? Can we go now?", () ->
									player.startConversation(new FremennikShipmaster(player, npcId, backing, SELECTION_DAEMONHEIM)));
				}

				if (previousSelection != SELECTION_GRUMPY)
				{
					ops.add("Why are you so grumpy?")
							.addPlayer(HeadE.CONFUSED, "Why are you so grumpy?")
							.addNPC(npcId, HeadE.ANGRY, "Grumpy? I should kill you where you stand!")
							.addNPC(npcId, HeadE.SAD_MILD, "But that wouldn't help with this damned humidity.")
							.addNPC(npcId, HeadE.SAD_MILD, "I need the snow in my boots, the sea wind stinging my face...")
							.addNPC(npcId, HeadE.SAD_MILD, "That's why I want to leave. Are you ready to go to Daemonheim?", () ->
									player.startConversation(new FremennikShipmaster(player, npcId, backing, SELECTION_GRUMPY)));
				}
			}
		});
		create();
	}

	public static void sail(Player player, boolean backing) {
		player.useStairs(-1, backing ? Tile.of(3254, 3171, 0) : Tile.of(3511, 3692, 0), 2, 3);
		if (backing)
			player.getControllerManager().forceStop();
		else
			player.getControllerManager().startController(new DamonheimController());
	}
}
