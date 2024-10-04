package it.polimi.ingsw.gui.support.context;

import it.polimi.ingsw.controller.GameFlow;
import it.polimi.ingsw.gui.support.info.PlayerInfo;
import it.polimi.ingsw.model.game.CommonBoard;
import it.polimi.ingsw.model.player.PlayerData;
import it.polimi.ingsw.model.player.PlayerManuscript;
import javafx.beans.property.ObjectProperty;

public class PlayerDataSource {

    private final String currUsername;
    private final GameContext ctx;

    public PlayerDataSource(GameContext ctx, String currUsername) {
        this.currUsername = currUsername;
        this.ctx = ctx;
    }

    public String username() {
        return currUsername;
    }

    public ObjectProperty<PlayerData> playerDataProperty() {
        return ctx.playerDataProperty(currUsername);
    }


    public PlayerData getPlayerData() {
        return ctx.getPlayerData(currUsername);
    }


    public void setPlayerData(PlayerData playerData) {
        ctx.setPlayerData(currUsername, playerData);
    }


    public void setPlayerInfo(PlayerInfo playerinfo) {
        ctx.setPlayerInfo(currUsername, playerinfo);
    }

    public ObjectProperty<PlayerInfo> playerInfoProperty() {
        return ctx.playerInfoProperty(currUsername);
    }

    public PlayerInfo getPlayerInfo() {
        return ctx.getPlayerInfo(currUsername);
    }

    public ObjectProperty<PlayerManuscript> manuscriptProperty() {
        return ctx.manuscriptProperty(currUsername);
    }


    public PlayerManuscript getManuscript() {
        return ctx.getManuscript(currUsername);
    }


    public GameFlow getGameFlow() {
        return ctx.getGameFlow();
    }

    public ObjectProperty<GameFlow> gameFlowProperty() {
        return ctx.gameFlowProperty();
    }

    public ObjectProperty<CommonBoard> boardProperty() {
        return ctx.boardProperty();
    }


    public CommonBoard getBoard() {
        return ctx.getBoard();
    }
}
