package it.polimi.ingsw.gui.support.info;

import it.polimi.ingsw.model.card.properties.CardFace;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record PlayerInfo(
        int handIndex,
        boolean isMainPlayer,
        Point2D manuscriptPosition,
        List<CardFace> handFaces
) {


    public PlayerInfo() {
        this(0, false, new Point2D(0, 0), Arrays.asList(null, null, null));
    }

    public PlayerInfo(PlayerInfo other) {
        this(other.handIndex, other.isMainPlayer, other.manuscriptPosition, new ArrayList<>(other.handFaces));
    }


    public PlayerInfo setHandFace(int index, CardFace face) {
        List<CardFace> faces = new ArrayList<>(handFaces);
        faces.set(index, face);
        return new PlayerInfo(handIndex, isMainPlayer, manuscriptPosition, faces);
    }

    public CardFace getHandFace(int index) {
        return handFaces.get(index);
    }


    public PlayerInfo setHandIndex(int selectedCardIndex) {
        return new PlayerInfo(selectedCardIndex, isMainPlayer, manuscriptPosition, handFaces);
    }


    public PlayerInfo setMainPlayer(boolean mainPlayer) {
        return new PlayerInfo(handIndex, isMainPlayer, manuscriptPosition, handFaces);
    }


    public PlayerInfo setManuscriptPosition(Point2D manuscriptPosition) {
        return new PlayerInfo(handIndex, isMainPlayer, manuscriptPosition, handFaces);
    }
}


