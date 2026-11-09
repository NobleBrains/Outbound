package net.noblebrains.outbound.leash;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

import java.util.Objects;

public final class LeashProxyEntity extends Turtle implements Leashable {
    public static final String TEAM_NAME = "leashplayersimpl";

    private final LivingEntity target;

    public LeashProxyEntity(LivingEntity target) {
        super(EntityTypes.TURTLE, target.level());

        this.target = target;

        setHealth(1.0F);
        setPermanentlyInvulnerable(true);
        setNoAi(true);
        setBaby(true);
        setInvisible(true);
        setSilent(true);
        noPhysics = true;

        MinecraftServer server = level().getServer();
        if (server != null) {
            Scoreboard scoreboard = server.getScoreboard();

            PlayerTeam team = scoreboard.getPlayerTeam(TEAM_NAME);
            if (team == null) {
                team = scoreboard.addPlayerTeam(TEAM_NAME);
            }
            if (team.getCollisionRule() != Team.CollisionRule.NEVER) {
                team.setCollisionRule(Team.CollisionRule.NEVER);
            }

            scoreboard.addPlayerToTeam(getScoreboardName(), team);
        }
    }

    private boolean proxyUpdate() {
        if (proxyIsRemoved()) return false;

        if (target == null) return true;
        if (target.level() != level() || !target.isAlive()) return true;

        Vec3 posActual = position();
        Vec3 posTarget = PlayerLeashHelper.calculateNeckPosition(
                target.position(),
                target.getXRot(),
                target.isShiftKeyDown()
        );

        if (!Objects.equals(posActual, posTarget)) {
            setRot(0.0F, 0.0F);
            setPos(posTarget.x, posTarget.y, posTarget.z);
            setBoundingBox(getDimensions(Pose.DYING).makeBoundingBox(posTarget));
        }

        return false;
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) return;
        if (proxyUpdate() && !proxyIsRemoved()) {
            proxyRemove();
        }
    }

    public boolean proxyIsRemoved() {
        return this.isRemoved();
    }

    public void proxyRemove() {
        MinecraftServer server = level().getServer();
        if (server == null) {
            return;
        }
        Scoreboard scoreboard = server.getScoreboard();

        PlayerTeam team = scoreboard.getPlayerTeam(TEAM_NAME);
        if (team != null) {
            scoreboard.removePlayerFromTeam(getScoreboardName(), team);
        }

        setPos(0.0D, -100.0D, 0.0D);
        super.discard();
    }

    @Override
    public float getHealth() {
        return 1.0F;
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public void push(Entity entity) {
    }
}
