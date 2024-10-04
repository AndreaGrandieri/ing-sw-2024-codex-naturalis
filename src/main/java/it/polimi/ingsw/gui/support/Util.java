package it.polimi.ingsw.gui.support;

import it.polimi.ingsw.MainBot;
import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.controller.GameState;
import it.polimi.ingsw.controller.event.game.GameEvents;
import it.polimi.ingsw.controller.example.GameControllerExample;
import it.polimi.ingsw.gui.GuiApplication;
import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.card.factory.GoalCardFactory;
import it.polimi.ingsw.model.card.factory.GoldCardFactory;
import it.polimi.ingsw.model.card.factory.ResourceCardFactory;
import it.polimi.ingsw.model.card.factory.StarterCardFactory;
import it.polimi.ingsw.model.card.properties.CardFace;
import it.polimi.ingsw.model.card.properties.CardKingdom;
import it.polimi.ingsw.model.game.CardType;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerColor;
import it.polimi.ingsw.model.player.PlayerManuscript;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Util {
    private static final String IMAGES_DIR = "images/";
    private static final String FONTS_DIR = "fonts/";
    private static final ExecutorService ex = Executors.newFixedThreadPool(4);

    public static String getImage(String image) {
        return getAsset(IMAGES_DIR + image);
    }

    public static String getFont(String image) {
        return getAsset(FONTS_DIR + image);
    }

    private static String getAsset(String asset) {
        return GuiApplication.class.getResource(asset).toExternalForm();
    }


    public static PlayerManuscript getManuscript1() {
        PlayerManuscript otherPlayerManuscript = new PlayerManuscript((new StarterCardFactory()).generateCard(81));

        otherPlayerManuscript.insertCard(new ManuscriptPosition(1, 0), (new GoldCardFactory()).generateCard(79));
        otherPlayerManuscript.insertCard(new ManuscriptPosition(1, 1), (new GoldCardFactory()).generateCard(67));
        otherPlayerManuscript.insertCard(new ManuscriptPosition(-1, 0), (new GoldCardFactory()).generateCard(51));
        otherPlayerManuscript.insertCard(new ManuscriptPosition(-2, 0), (new GoldCardFactory()).generateCard(41));
        otherPlayerManuscript.insertCard(new ManuscriptPosition(-2, 1), (new ResourceCardFactory()).generateCard(33));
        return otherPlayerManuscript;
    }

    // Return submap with specified keys
    public static <K, V> Map<K, V> filterMapByKeys(Map<K, V> map, List<K> currKeys) {
        return map.entrySet().stream()
                .filter(e -> !currKeys.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    public static PlayerManuscript getManuscript2(int maxX) {

        int r1 = 52, r2 = 66;

        PlayerManuscript manuscript = new PlayerManuscript();
        manuscript.insertStarterCard((new StarterCardFactory()).generateCard(81));

        for (int y = -5; y <= 5; y++) {
            for (int x = -5; x <= maxX; x++) {
                if ((y == 0 || x == 0) && !(x == 0 && y == 0)) {
                    manuscript.insertCard(new ManuscriptPosition(x, y), generateTypedCard(y % 2 == 0 ? r1 : r2));
                }
            }
        }
        return manuscript;
    }

    public static <T> T createInit(T o, Consumer<T> init) {
        init.accept(o);
        return o;
    }

    public static TypedCard generateTypedCard(int id) {
        TypedCard card = (new ResourceCardFactory()).generateCard(20);
        if (id > 0 && id <= 40) {
            card = (new ResourceCardFactory()).generateCard(id);
        }
        if (id > 40 && id <= 80) {
            card = (new GoldCardFactory()).generateCard(id);
        }
        return card;

    }

    public static Card generateCard(int id) {
        Card card = (new ResourceCardFactory()).generateCard(20);
        if (id > 0 && id <= 40) {
            card = (new ResourceCardFactory()).generateCard(id);
        }
        if (id > 40 && id <= 80) {
            card = (new GoldCardFactory()).generateCard(id);
        }
        if (id > 80 && id <= 86) {
            card = (new StarterCardFactory()).generateCard(id);
        }
        if (id > 86 && id <= 102) {
            card = (new GoalCardFactory()).generateCard(id);
        }
        return card;

    }

    public static void setCardFace(Card c, CardFace f) {
        if (c.getFace() != f) {
            c.flip();
        }
    }

    public static Timeline newStartAnimation(EventHandler<ActionEvent> e) {
        Timeline t = new Timeline();

        t.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO)
        );
        t.setOnFinished(e);
        return t;
    }

    public static void main(String[] args) {
        GameController gc = GameControllerExample.gc(List.of("a", "b", "c", "d"));
        GameControllerExample.simulateFullGame(gc);
        System.out.println(gc.getGameFlow().getState());
        System.out.println(gc.getGameBoard().allDecksEmpty());
        System.out.println(gc.getWinner());
        System.out.println(gc.getGameFlow().getAllUsernames().stream().collect(
                Collectors.toMap(Function.identity(), n -> gc.getPlayerData(n).getPoints())
        ));
    }

    public static GameController newInitializedController(List<String> players) {
        GameController gc = new GameController(new ArrayList<>(players));


        // initializing players' manuscripts
        for (String player : gc.getGameFlow().getAllUsernames()) {
            gc.setStarterCard(player, CardFace.FRONT);
        }

        // give players random colors
        for (String player : gc.getGameFlow().getAllUsernames()) {
            List<PlayerColor> availableColors = gc.getAvailableColors();
            gc.setPlayerColor(player, availableColors.get(random((availableColors.size() - 1))));
        }

        // every player chooses first goal proposed
        for (String player : players) {
            gc.choosePrivateGoal(player, gc.getProposedPrivateGoals(player).get(0));
        }

        for (int i = 0; i < 5; i++) {
            for (String player : players) {
                List<ManuscriptPosition> positions = new ArrayList<>(gc.getManuscript(player).getAllAvailablePositions());
                int pos = random((positions.size() - 1));
                ManuscriptPosition position = positions.get(pos);

                TypedCard card;
                do {
                    int handIndex = random(2);
                    card = gc.getPlayerData(player).getHand().get(handIndex);
                } while (card == null);

                if (!gc.getManuscript(player).isPlaceable(position, card)) {
                    card.flip();
                }


                gc.placeCard(player, card, position);

                while (gc.getGameFlow().getState() == GameState.PLAYING && gc.drawVisibleCard(player,
                        (random(2) % 2 == 0) ? CardType.GOLD : CardType.RESOURCE,
                        random(2)) == null) ;

            }
        }

        return gc;

    }

    public static GameController endStateGameController(List<String> players) {
        GameController gc = new GameController(new ArrayList<>(players));


        // initializing players' manuscripts
        for (String player : gc.getGameFlow().getAllUsernames()) {
            gc.setStarterCard(player, CardFace.FRONT);
        }

        // give players random colors
        for (String player : gc.getGameFlow().getAllUsernames()) {
            List<PlayerColor> availableColors = gc.getAvailableColors();
            gc.setPlayerColor(player, availableColors.get(random((availableColors.size() - 1))));
        }

        // every player chooses first goal proposed
        for (String player : players) {
            gc.choosePrivateGoal(player, gc.getProposedPrivateGoals(player).get(0));
        }

        System.out.println(gc.getGameFlow().getState());
        while (gc.getGameFlow().getState() != GameState.POST_GAME) {
            for (String player : players) {
                List<ManuscriptPosition> positions = new ArrayList<>(gc.getManuscript(player).getAllAvailablePositions());
                int pos = random(positions.size() - 1);
                ManuscriptPosition position = positions.get(pos);

                TypedCard card;
                do {
                    int handIndex = random(2);
                    card = gc.getPlayerData(player).getHand().get(handIndex);
                } while (card == null);

                if (!gc.getManuscript(player).isPlaceable(position, card) || gc.getManuscript(player).isCostSatisfied(card)) {
                    card.flip();
                }

                if (!gc.placeCard(player, card, position)) {
//                    System.out.println(player + " " + card + gc.getPlayerData(player).getPoints());
                    System.out.println("Error");
                }


                while (gc.getGameFlow().getState() == GameState.PLAYING && gc.drawVisibleCard(player,
                        (random(2) % 2 == 0) ? CardType.GOLD : CardType.RESOURCE,
                        random(1)) == null) ;

            }
        }

        return gc;

    }

    private static int random(int x) {
        return (int) (Math.random() * (x + 1));
    }

    public static GameController newInitializedControllerWithBots(List<String> players, int turns) {
        GameController gc = new GameController(new ArrayList<>(players));

        String mainUsername = players.get(0);

        List<String> otherPlayers = new ArrayList<>(players);
        otherPlayers.remove(mainUsername);

        Object lock = new Object();

        final int[] i = {0};

        gc.addEventHandler(null, GameEvents.TURN_CHANGE, turnChangeEvent -> {
            if (turnChangeEvent.currentUsername().equals(mainUsername)) {


                String player = mainUsername;

                if (i[0] < turns) {
                    List<ManuscriptPosition> positions = new ArrayList<>(gc.getManuscript(player).getAllAvailablePositions());
                    int pos = random((positions.size() - 1));
                    ManuscriptPosition position = positions.get(pos);

                    TypedCard card;
                    do {
                        int handIndex = random(2);
                        card = gc.getPlayerData(player).getHand().get(handIndex);
                    } while (card == null);

                    if (!gc.getManuscript(player).isPlaceable(position, card)) {
                        card.flip();
                    }


                    gc.placeCard(player, card, position);

                    while (gc.getGameFlow().getState() == GameState.PLAYING && gc.drawVisibleCard(player,
                            (random(2) % 2 == 0) ? CardType.GOLD : CardType.RESOURCE,
                            random(2)) == null) ;

                } else {
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
                i[0]++;

            }
        });

        List<MainBot> bots = new ArrayList<>();
        for (String player : otherPlayers) {
            MainBot bot = new MainBot(player, gc);
            bots.add(bot);
            bot.setWaitTime(0);
            bot.setupGame();
        }

        // initializing players' manuscripts
        gc.setStarterCard(mainUsername, CardFace.FRONT);


        // give players random colors
        List<PlayerColor> availableColors = gc.getAvailableColors();
        gc.setPlayerColor(mainUsername, availableColors.get(random((availableColors.size() - 1))));


        // every player chooses first goal proposed
        gc.choosePrivateGoal(mainUsername, gc.getProposedPrivateGoals(mainUsername).get(0));

        System.out.println("Init done");

        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        bots.forEach(mainBot -> mainBot.setWaitTime(2000));

        return gc;

    }

    public static Animation spacerAnim(int duration) {
        return new Timeline(
                new KeyFrame(Duration.millis(duration))
        );
    }


    public static TypedCard cardFromKingdom(CardType t, CardKingdom ck) {
        if (ck == null) return null;
        // Default fungi, resource
        int id = 1;
        if (CardKingdom.PLANT == ck) id = 11;
        if (CardKingdom.ANIMAL == ck) id = 21;
        if (CardKingdom.INSECT == ck) id = 31;

        TypedCard card = null;
        if (CardType.RESOURCE == t) {
            card = new ResourceCard(id, null, null, null);
        }

        if (CardType.GOLD == t) {
            id = id + 40;
            card = new GoldCard(id, null, null, null, null);
        }
        card.setFace(CardFace.BACK);

        return card;
    }

    public static void taskWithCallback(Runnable work, Runnable onFinish) {

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                work.run();
                return null;
            }
        };
        task.setOnSucceeded(workerStateEvent -> {
            onFinish.run();
        });
        new Thread(task).start();

    }

    public static void runThread(Runnable work) {
        //ex.submit(work);
        new Thread(work).start();
    }

    public static void runWaitThread(Runnable work) {
        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            work.run();
            latch.countDown();
        }).start();

        try {
            latch.await();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void runWaitUI(Runnable work) {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            work.run();
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void taskExWithCallback2(Runnable work, Runnable onFinish) {

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                work.run();
                return null;
            }
        };
        task.setOnSucceeded(workerStateEvent -> {
            onFinish.run();
        });

        ex.submit(task);
    }

    public static void sleep(long wait) {
        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void runWaitAnimation(Timeline moveAnim) {
        Object lock = new Object();
        moveAnim.setOnFinished(actionEvent -> {
            synchronized (lock) {
                lock.notifyAll();
            }
        });
        moveAnim.play();
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static <T extends Card> T cloneAndSetFace(T card, CardFace face) {
        T card1 = (T) card.cloneCard();
        card1.setFace(face);
        return card1;
    }
}
