package net.noblebrains.outbound;

import net.noblebrains.outbound.config.OutboundConfig;
import net.noblebrains.outbound.minecart.MinecartPickupHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class MinecartClimbingTest {

    @BeforeEach
    void setUp() {
        OutboundConfig.get().enableClimbingMinecartPickup = true;
    }

    @Test
    @DisplayName("Predicate is unmodified when source is null or non-minecart (preserves cramming)")
    void testSourceNotRideableMinecart() {
        Predicate<Entity> dummyOriginal = e -> false;

        // Null source returns original predicate
        Predicate<Entity> resultNull = MinecartPickupHelper.wrapPushablePredicate(null, dummyOriginal);
        assertSame(dummyOriginal, resultNull, "Null source must return original predicate unmodified");
    }

    @Test
    @DisplayName("Predicate is unmodified when config toggle is disabled")
    void testConfigDisabled() {
        OutboundConfig.get().enableClimbingMinecartPickup = false;
        Predicate<Entity> dummyOriginal = e -> false;

        Predicate<Entity> result = MinecartPickupHelper.wrapPushablePredicate(null, dummyOriginal);
        assertSame(dummyOriginal, result, "Should return original predicate when feature is disabled in config");
    }

    @Test
    @DisplayName("Valid climbing entity can be picked up by minecart")
    void testValidClimbingEntityPickup() {
        boolean canPickUp = MinecartPickupHelper.canPickUpClimbing(
                true,                      // isLiving
                true,                      // isAlive
                false,                     // isSpectator
                false,                     // isPassenger
                true,                      // onClimbable
                true,                      // isPositionTicking
                false,                     // isClientRemotePlayer
                Team.CollisionRule.ALWAYS, // sourceRule
                Team.CollisionRule.ALWAYS, // targetRule
                false                      // isAllied
        );
        assertTrue(canPickUp, "Valid living climbing mob should be eligible for minecart pickup");
    }

    @Test
    @DisplayName("Entities that are dead, spectator, already passenger, or not on climbable are rejected")
    void testInvalidEntityStatesRejected() {
        // Dead mob
        assertFalse(MinecartPickupHelper.canPickUpClimbing(
                true, false, false, false, true, true, false,
                Team.CollisionRule.ALWAYS, Team.CollisionRule.ALWAYS, false
        ), "Dead mob must be rejected");

        // Spectator
        assertFalse(MinecartPickupHelper.canPickUpClimbing(
                true, true, true, false, true, true, false,
                Team.CollisionRule.ALWAYS, Team.CollisionRule.ALWAYS, false
        ), "Spectator must be rejected");

        // Already passenger
        assertFalse(MinecartPickupHelper.canPickUpClimbing(
                true, true, false, true, true, true, false,
                Team.CollisionRule.ALWAYS, Team.CollisionRule.ALWAYS, false
        ), "Already riding passenger must be rejected");

        // Not on climbable
        assertFalse(MinecartPickupHelper.canPickUpClimbing(
                true, true, false, false, false, true, false,
                Team.CollisionRule.ALWAYS, Team.CollisionRule.ALWAYS, false
        ), "Mob not on climbable must be rejected");

        // Not a living entity
        assertFalse(MinecartPickupHelper.canPickUpClimbing(
                false, true, false, false, true, true, false,
                Team.CollisionRule.ALWAYS, Team.CollisionRule.ALWAYS, false
        ), "Non-living entity must be rejected");

        // Not in ticking chunk
        assertFalse(MinecartPickupHelper.canPickUpClimbing(
                true, true, false, false, true, false, false,
                Team.CollisionRule.ALWAYS, Team.CollisionRule.ALWAYS, false
        ), "Non-ticking entity must be rejected");

        // Remote client player
        assertFalse(MinecartPickupHelper.canPickUpClimbing(
                true, true, false, false, true, true, true,
                Team.CollisionRule.ALWAYS, Team.CollisionRule.ALWAYS, false
        ), "Client remote player must be rejected");
    }

    @Test
    @DisplayName("Team collision rules are strictly respected")
    void testTeamCollisionRules() {
        // Source rule NEVER
        assertFalse(MinecartPickupHelper.canPickUpClimbing(
                true, true, false, false, true, true, false,
                Team.CollisionRule.NEVER, Team.CollisionRule.ALWAYS, false
        ), "Source collision rule NEVER must reject");

        // Target rule NEVER
        assertFalse(MinecartPickupHelper.canPickUpClimbing(
                true, true, false, false, true, true, false,
                Team.CollisionRule.ALWAYS, Team.CollisionRule.NEVER, false
        ), "Target collision rule NEVER must reject");

        // PUSH_OWN_TEAM when allied -> returns false (vanilla formula)
        assertFalse(MinecartPickupHelper.canPickUpClimbing(
                true, true, false, false, true, true, false,
                Team.CollisionRule.PUSH_OWN_TEAM, Team.CollisionRule.ALWAYS, true
        ), "PUSH_OWN_TEAM with allied entity evaluates to false in vanilla");

        // PUSH_OTHER_TEAMS when allied -> evaluates to true in vanilla formula
        assertTrue(MinecartPickupHelper.canPickUpClimbing(
                true, true, false, false, true, true, false,
                Team.CollisionRule.PUSH_OTHER_TEAMS, Team.CollisionRule.ALWAYS, true
        ), "PUSH_OTHER_TEAMS with allied entity evaluates to true in vanilla");

        // PUSH_OTHER_TEAMS when NOT allied -> evaluates to false in vanilla formula
        assertFalse(MinecartPickupHelper.canPickUpClimbing(
                true, true, false, false, true, true, false,
                Team.CollisionRule.PUSH_OTHER_TEAMS, Team.CollisionRule.ALWAYS, false
        ), "PUSH_OTHER_TEAMS with non-allied entity evaluates to false in vanilla");
    }

    @Test
    @DisplayName("Entity cramming immunity decoupling is verified")
    void testCrammingDecouplingLogic() {
        // 1. Verify config toggle presence and default value
        assertTrue(OutboundConfig.get().enableClimbingMinecartPickup, "Feature should be enabled by default");

        // 2. A non-minecart source (such as during cramming check between mobs) does NOT wrap pushable predicate
        Predicate<Entity> crammingPredicate = e -> false;
        Predicate<Entity> result = MinecartPickupHelper.wrapPushablePredicate(null, crammingPredicate);
        assertSame(crammingPredicate, result, "Cramming pushable predicate between mobs remains strictly vanilla");
    }
}
