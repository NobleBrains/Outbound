package net.noblebrains.outbound.afk;

public interface AfkPlayer {
    boolean outbound$isAfk();

    void outbound$recordActiveInput();
}
