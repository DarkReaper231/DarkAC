/*
 * AntiCheatReloaded for Bukkit and Spigot.
 * Copyright (c) 2012-2015 AntiCheat Team | http://gravitydevelopment.net
 * Copyright (c) 2016-2018 Rammelkast | https://rammelkast.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.rammelkast.anticheatreloaded.check.movement;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.util.Distance;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

public class GlideCheck {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);

	public static Map<UUID, Double> lastDiff = new HashMap<UUID, Double>();
	public static Map<UUID, Float> lastFallDistance = new HashMap<UUID, Float>();
	public static Map<UUID, Integer> violations = new HashMap<UUID, Integer>();

	public static CheckResult runCheck(Player player, Distance distance) {
		if (VersionUtil.isFlying(player)) {
			return PASS;
		}
		if (player.getLocation().getBlock().getType() == Material.LADDER || player.getLocation().getBlock().getRelative(0, 1, 0).getType() == Material.LADDER) {
			return PASS;
		}
		if (!lastDiff.containsKey(player.getUniqueId())) {
			lastDiff.put(player.getUniqueId(), distance.getYDifference());
			return PASS;
		}
		if (!lastFallDistance.containsKey(player.getUniqueId())) {
			lastFallDistance.put(player.getUniqueId(), player.getFallDistance());
			return PASS;
		}
		double yDiff = distance.getYDifference();
		float fallDistance = player.getFallDistance();
		if (yDiff == lastDiff.get(player.getUniqueId()) && fallDistance > lastFallDistance.get(player.getUniqueId())) {
			if (!violations.containsKey(player.getUniqueId())) {
				violations.put(player.getUniqueId(), 1);
			} else {
				if (violations.get(player.getUniqueId()) + 1 >= AntiCheatReloaded.getManager().getBackend().getMagic()
						.GLIDE_LIMIT()) {
					violations.remove(player.getUniqueId());
					Location to = player.getLocation();
					to.setY(to.getY() - distanceToFall(to));
					player.teleport(to);
					// Report glide violation to statistics, just for the lulz jk
					AntiCheatReloaded.getPlugin().onGlideViolation();
					return new CheckResult(CheckResult.Result.FAILED,
							player.getName() + " was set back for gliding (yDiff="
									+ new BigDecimal(yDiff).setScale(2, BigDecimal.ROUND_UP) + ")");
				} else {
					violations.put(player.getUniqueId(), violations.get(player.getUniqueId()) + 1);
				}
			}
		} else {
			// TODO more sophisticated check
		}
		lastDiff.put(player.getUniqueId(), distance.getYDifference());
		lastFallDistance.put(player.getUniqueId(), player.getFallDistance());
		return PASS;
	}

	private static double distanceToFall(Location location) {
		location = location.clone();
		double firstY = location.getY();
		while (location.clone().add(0, -0.1, 0).getBlock().getType() == Material.AIR)
			location.add(0, -0.1, 0);
		return firstY - location.getY();
	}

}
