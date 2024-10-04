package it.polimi.ingsw.gui.support;

import it.polimi.ingsw.gui.components.panes.card.CardRect;
import it.polimi.ingsw.gui.support.helper.Card2D;
import it.polimi.ingsw.gui.support.interfaces.DeckController;
import it.polimi.ingsw.gui.support.interfaces.HandController;
import it.polimi.ingsw.gui.support.interfaces.ManuscriptController;
import it.polimi.ingsw.gui.support.info.*;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.card.properties.CardFace;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static it.polimi.ingsw.gui.support.Util.spacerAnim;
import static java.lang.Math.max;

public class AnimationBuilder {
    private static final int prefMoveToAnimationDuration = 200;

    public static Point2D positionOnHover(Point2D pt, Pane hoverPane) {
        Point2D p = hoverPane.sceneToLocal(pt);
        return new Point2D(p.getX(), p.getY());

    }

    public static Animation deckDrawToHandAnimation(AnimDrawDeckToHandInfo drawInfo, Pane hoverPane) {

        HandController handController = drawInfo.handController();
        DeckController deckController = drawInfo.deckController();

        CardPaneInfo deckPaneInfo = deckController.getDeckPaneInfo(drawInfo.deckType());
        Point2D deckPos = positionOnHover(deckPaneInfo.scenePosition(), hoverPane);
        Card2D deckSize = deckPaneInfo.size();


        int handIndex = drawInfo.handIndex();
        CardPaneInfo handPaneInfo = handController.getHandPaneInfo(handIndex);


        TypedCard newDeckCard = drawInfo.drawnCard();
        Util.setCardFace(newDeckCard, CardFace.BACK);

        CardRect animCardPane = ComponentBuilder.newCardRect(newDeckCard, deckSize, deckPos);

        // start
        Timeline startAnim = new Timeline();
        startAnim.setOnFinished((e) -> {
            if (!hoverPane.getChildren().contains(animCardPane)) hoverPane.getChildren().add(animCardPane);
            deckController.update();

            //            deckPane.hideCardRect(deckCardPane);
        });

        // flip
        Animation flipAnim;
        if (!newDeckCard.isClean()) {
            flipAnim = animCardPane.flipAnimation();
        } else {
            flipAnim = new Timeline();
        }

        flipAnim.setOnFinished((e) -> {
            deckController.showDeckPane(drawInfo.deckType());
        });


        AnimMoveToInfo animMoveToInfo = new AnimMoveToInfo(
                deckPos,
                positionOnHover(handPaneInfo.scenePosition(), hoverPane),
                handPaneInfo.size(),
                animCardPane
        );

        // move
        Animation moveAnim = commonMoveToAnimation(animMoveToInfo, hoverPane);
        moveAnim.setOnFinished((e) -> {
            handController.showHandPane(handIndex);
            hoverPane.getChildren().remove(animCardPane);
        });


        return new SequentialTransition(startAnim, flipAnim, moveAnim);

    }

    public static Animation visibleDrawToHandAnimation(AnimDrawVisibleToHandInfo drawInfo, Pane hoverPane) {

        HandController handController = drawInfo.handController();
        DeckController deckController = drawInfo.deckController();


        CardPaneInfo deckPaneInfo = deckController.getDeckPaneInfo(drawInfo.selectedType());
        Point2D deckPos = positionOnHover(deckPaneInfo.scenePosition(), hoverPane);
        Card2D deckSize = deckPaneInfo.size();

        CardPaneInfo selectedPaneInfo = deckController.getVisiblePaneInfo(drawInfo.selectedType(), drawInfo.visibleIndex());
        Point2D selectedPos = positionOnHover(selectedPaneInfo.scenePosition(), hoverPane);
        Card2D selectedSize = selectedPaneInfo.size();

        CardPaneInfo handPaneInfo = handController.getHandPaneInfo(drawInfo.handIndex());
        Point2D handPos = positionOnHover(handPaneInfo.scenePosition(), hoverPane);
        Card2D handSize = handPaneInfo.size();


        Util.setCardFace(drawInfo.newDeckCard(), CardFace.BACK);


        // Set animPane to selectedPane
        CardRect animCardPane = ComponentBuilder.newCardRect(drawInfo.selectedCard(), deckSize, selectedPos);

        // Animations
        // Start actions
        Timeline startAnim = new Timeline();
        startAnim.setOnFinished((e) -> {
            Platform.runLater(() -> {
                if (!hoverPane.getChildren().contains(animCardPane)) hoverPane.getChildren().add(animCardPane);
                deckController.hideVisiblePane(drawInfo.selectedType(), drawInfo.visibleIndex());
            });
        });

        // Visible to Hand Move anim
        Animation moveHandAnim = commonMoveToAnimation(new AnimMoveToInfo(
                selectedPos, handPos, handSize, animCardPane
        ), hoverPane);


        if (!drawInfo.emptyDeck()) {
            moveHandAnim.setOnFinished((e) -> {
                handController.showHandPane(drawInfo.handIndex());

                //            animCardPane.setCardHeight(selectedSize.getHeight());
                animCardPane.setSize(selectedSize);
                animCardPane.setCard(drawInfo.newDeckCard());
                animCardPane.relocate(deckPos.getX(), deckPos.getY());

                deckController.update();
            });

            // Flip deck drawnDeckCard Anim
            Animation flipAnim = animCardPane.flipAnimation();
            flipAnim.setOnFinished((e) -> {
                deckController.showDeckPane(drawInfo.selectedType());

                deckController.update();

            });

            Timeline spaceAnim = new Timeline(
                    new KeyFrame(Duration.millis(100))
            );


            // Deck to visible Move Anim
            Animation moveVisibleAnim = commonMoveToAnimation(new AnimMoveToInfo(
                    deckPos, selectedPos, selectedSize, animCardPane
            ), hoverPane);

            moveVisibleAnim.setOnFinished((e) -> {
                deckController.showVisiblePane(drawInfo.selectedType(), drawInfo.visibleIndex());
                hoverPane.getChildren().remove(animCardPane);
            });

            return new SequentialTransition(startAnim,
                    spacerAnim(100),
                    moveHandAnim,
                    flipAnim,
                    spacerAnim(100),
                    moveVisibleAnim);
        }

        moveHandAnim.setOnFinished((e) -> {
            handController.showHandPane(drawInfo.handIndex());

            //            animCardPane.setCardHeight(selectedSize.getHeight());
            hoverPane.getChildren().remove(animCardPane);

            deckController.update();
        });


        return new SequentialTransition(startAnim, spacerAnim(100), moveHandAnim);
    }

    public static Animation placeCardAnimation(AnimCardPlaceInfo drawInfo, Pane hoverPane) {
        HandController handController = drawInfo.handController();
        ManuscriptController manuscriptController = drawInfo.manuscriptController();

        CardPaneInfo placeholderPaneInfo = manuscriptController.getCardInfo(drawInfo.position());
        Point2D placeholderPos = positionOnHover(placeholderPaneInfo.scenePosition(), hoverPane);
        Card2D placeholderSize = placeholderPaneInfo.size();

        CardPaneInfo handPaneInfo = handController.getHandPaneInfo(drawInfo.handIndex());
        Point2D handPos = positionOnHover(handPaneInfo.scenePosition(), hoverPane);
        Card2D handSize = handPaneInfo.size();

        //        Util.setCardFace(drawInfo.getDrawnCard(), CardFace.BACK);

        // Set animPane to selectedPane
        //        CardRect animCardPane = new CardRect(handSize.getHeight(), drawInfo.cardInHand());
        CardRect animCardPane = ComponentBuilder.newCardRect(drawInfo.cardInHand(), handSize, handPos);

        // Animations
        // Start actions
        Timeline startAnim = new Timeline();
        startAnim.setOnFinished((e) -> {

            if (!hoverPane.getChildren().contains(animCardPane)) hoverPane.getChildren().add(animCardPane);
            handController.hideHandPane(drawInfo.handIndex());
        });

        // Visible to Hand Move anim
        Animation moveHandAnim = commonMoveToAnimation(new AnimMoveToInfo(
                handPos, placeholderPos, placeholderSize, animCardPane, placeholderPaneInfo.posProp()
        ), hoverPane);
        moveHandAnim.setOnFinished((e) -> {
            manuscriptController.showCardAtPosition(drawInfo.position());

            hoverPane.getChildren().remove(animCardPane);
        });


        return new SequentialTransition(startAnim, moveHandAnim);
    }

    public static Animation commonMoveToAnimation(AnimMoveToInfo animInfo, Pane hoverPane) {
        Point2D startPos = animInfo.startPos();

        Point2D endPos = animInfo.endPos();
        Card2D endSize = animInfo.endSize();

        CardRect animCardPane = animInfo.animatedPane();


        List<KeyValue> animValues = new ArrayList<>(List.of(
                new KeyValue(animCardPane.sizeProperty(), endSize, Interpolator.EASE_BOTH)
        ));

        if (animInfo.endPosProp() != null) {
            System.out.println("--- pos system");
            animValues.add(animCardPane.toPositionKeyValue(animInfo.endPosProp(), hoverPane));
        } else {
            animValues.addAll(List.of(
                    new KeyValue(animCardPane.layoutXProperty(), endPos.getX(), Interpolator.EASE_BOTH),
                    new KeyValue(animCardPane.layoutYProperty(), endPos.getY(), Interpolator.EASE_BOTH)
            ));
        }

        // Animation

        Timeline moveAnim = new Timeline();
        double distance = startPos.distance(endPos);
        int duration = (int) max(distance * 1, prefMoveToAnimationDuration);
        System.out.println(duration);
        //                System.out.println("dist: " + distance +" "+ duration);

        moveAnim.getKeyFrames().setAll(
                new KeyFrame(Duration.millis(duration),
                        animValues.toArray(KeyValue[]::new)
                )
        );

        return moveAnim;
    }

    public static void moveScrollDown(ScrollPane pane) {
        (new Timeline(new KeyFrame(Duration.millis(100), new KeyValue(pane.vvalueProperty(), 1)))).play();
    }
}
