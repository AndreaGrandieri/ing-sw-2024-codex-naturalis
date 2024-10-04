package it.polimi.ingsw.gui.tests.manTest;

import it.polimi.ingsw.controller.GameFlow;
import it.polimi.ingsw.gui.components.panes.manuscript.ManuscriptBox;
import it.polimi.ingsw.gui.support.helper.PC;
import it.polimi.ingsw.gui.support.context.PlayerDataSource;
import it.polimi.ingsw.gui.support.info.PlayerInfo;
import it.polimi.ingsw.model.card.GoalCard;
import it.polimi.ingsw.model.card.ResourceCard;
import it.polimi.ingsw.model.card.StarterCard;
import it.polimi.ingsw.model.card.deck.CardDeck;
import it.polimi.ingsw.model.card.factory.GoalCardFactory;
import it.polimi.ingsw.model.card.factory.ResourceCardFactory;
import it.polimi.ingsw.model.card.factory.StarterCardFactory;
import it.polimi.ingsw.model.game.CommonBoard;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerColor;
import it.polimi.ingsw.model.player.PlayerData;
import it.polimi.ingsw.model.player.PlayerManuscript;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.IntStream.range;
import static javafx.beans.binding.Bindings.createDoubleBinding;

public class TestController2 implements Initializable {
    private final CardDeck<StarterCard> starterDeck = new CardDeck<>(new StarterCardFactory());
    private final PlayerManuscript man = new PlayerManuscript(starterDeck.drawNextCard());
    public ManuscriptBox manPane;
    public VBox mainBox;
    private CardDeck<ResourceCard> resDeck = new CardDeck<>(new ResourceCardFactory());
    @FXML
    private Label welcomeText;

    @FXML
    protected void onButtonClick() {
        welcomeText.setText("Button click!");
        randomPlaceMan(man);
        manPane.setManuscript(new PC<>(man, ManuscriptBox.MANUSCRIPT_UPDATE));
        manPane.requestFocus();
    }

    private void randomPlaceMan(PlayerManuscript mp) {
        ManuscriptPosition pos = new ArrayList<>(mp.getAllAvailablePositions()).get(0);
        mp.insertCard(pos, drawResource());
    }

    private ResourceCard drawResource() {
        try {
            return resDeck.drawNextCard();
        } catch (Exception e) {
            System.out.println(e);
            resDeck = new CardDeck<>(new ResourceCardFactory());
            return resDeck.drawNextCard();
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        CardDeck<GoalCard> goalsDeck = new CardDeck<>(new GoalCardFactory());

        CommonBoard board = new CommonBoard(goalsDeck.drawNextNCards(2));

        PlayerData pd = new PlayerData("pippo");
        pd.setColor(PlayerColor.RED);
        pd.setHand(resDeck.drawNextNCards(3));
        PlayerDataSource dControl = new PlayerDataSource(null, null) {

            @Override
            public String username() {
                return null;
            }

            @Override
            public PlayerData getPlayerData() {
                return pd;
            }

            @Override
            public ObjectProperty<PlayerData> playerDataProperty() {
                return null;
            }

            @Override
            public PlayerInfo getPlayerInfo() {
                return null;
            }

            @Override
            public void setPlayerInfo(PlayerInfo playerinfo) {

            }

            @Override
            public void setPlayerData(PlayerData playerData) {

            }

            @Override
            public ObjectProperty<PlayerInfo> playerInfoProperty() {
                return null;
            }

            @Override
            public PlayerManuscript getManuscript() {
                return man;
            }

            @Override
            public ObjectProperty<PlayerManuscript> manuscriptProperty() {
                return null;
            }

            @Override
            public GameFlow getGameFlow() {
                return null;
            }

            @Override
            public ObjectProperty<GameFlow> gameFlowProperty() {
                return null;
            }

            @Override
            public CommonBoard getBoard() {
                return null;
            }

            @Override
            public ObjectProperty<CommonBoard> boardProperty() {
                return null;
            }

        };

        ReadOnlyObjectProperty<Bounds> mainBoundsProperty = mainBox.layoutBoundsProperty();
        manPane.ratioProperty().bind(
                createDoubleBinding(() -> {
                    Bounds b = mainBoundsProperty.get();
                    return max(min(
                            b.getWidth() / 1920, b.getHeight() / 1080
                    ) * 1.3, 0.2);
                }, mainBoundsProperty)
        );
        range(0, 30).forEach(i -> randomPlaceMan(man));
        manPane.setManuscript(new PC<>(man, ManuscriptBox.MANUSCRIPT_REDRAW));
        manPane.clickedPlaceholderProperty().addListener((observableValue, intPoint, t1) -> {
            man.insertCard(t1.toManuscriptPosition(), drawResource());
            manPane.setManuscript(new PC<>(man, ManuscriptBox.MANUSCRIPT_UPDATE));
        });
        mainBox.focusedProperty().addListener(observable -> System.out.println("ss"));
    }
}
