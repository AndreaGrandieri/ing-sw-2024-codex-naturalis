package it.polimi.ingsw.cli;

import it.polimi.ingsw.client.ConnectionManager;
import it.polimi.ingsw.client.NetworkClientChatController;
import it.polimi.ingsw.client.NetworkClientGameController;
import it.polimi.ingsw.client.NetworkClientMatchController;
import it.polimi.ingsw.client.network.ProfileOfClient;
import it.polimi.ingsw.client.network.ProfileOfClientException;
import it.polimi.ingsw.client.network.liveloop.UserOfClient;
import it.polimi.ingsw.client.network.tcpip.ClientCriticalError;
import it.polimi.ingsw.controller.ChatMessage;
import it.polimi.ingsw.controller.GameFlow;
import it.polimi.ingsw.controller.interfaces.ClientChatController;
import it.polimi.ingsw.model.card.GoalCard;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.card.properties.CardFace;
import it.polimi.ingsw.model.game.CardType;
import it.polimi.ingsw.model.game.CommonBoard;
import it.polimi.ingsw.model.game.gamelobby.LobbyInfo;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerColor;

import java.util.List;

import static it.polimi.ingsw.controller.event.lobby.LobbyEvents.LOBBY_START;

/**
 * Codex Naturalis CLI application. Run an interactive session to interact with a running server.
 */
public class CliApplication {
    private final String address;

    private final int port;

    private final boolean rmi;

    private String myUsername;

    private ConnectionManager cm;

    private NetworkClientMatchController cmc;

    private NetworkClientGameController cgc;

    private NetworkClientChatController ccc;

    private HandlerManager hm;

    private final IOManager io;


    /**
     * Main constructor.
     * @param address Server address
     * @param port Server port
     * @param rmi if true, use RMI
     */
    public CliApplication(String address, int port, boolean rmi) {
        this.address = address;
        this.port = port;
        this.rmi = rmi;

        io = new IOManager();

        hm = new HandlerManager();
    }

    /**
     * Start the CLI to actually start playing.
     */
    public void runGame() {
        initGame();

        // Before game start, gc is still null
        while (true) {
            String command = io.readPrintPrompt(hm.getState());
            runCommand(command);

            if (hm.getState() == CliState.PRE_LOBBY) {
                cgc = null;
                ccc = null;
            }
        }
    }

    private void initGame() {
        IOManager.splashScreen();

        cm = connect(address, port, rmi);

        if (!rmi) {
            cm.getUser().getListenLoop().setUncaughtExceptionHandler((t, e) -> {
                IOManager.println("\n\n-------");
                IOManager.error("Critical error. Connection lost. Program will close.");
                System.exit(1);
            });
        }

        assignUsername();

        cmc = cm.clientMatchController();

        cmc.addEventHandler(LOBBY_START, (e) -> lobbyStartInternalHandler());

        IOManager.println("Type help for a list of commands");
        IOManager.println("");
    }

    private void lobbyStartInternalHandler() {
        this.cgc = cm.clientGameController();
        hm.setCgc(cgc);
        hm.setUser(myUsername);

        // Request GameFlow to the Server
        GameFlow gameFlow = this.cgc.getGameFlow();

        switch (gameFlow.getState()) {
            case SETTING -> hm.setState(CliState.INIT);

            case PLAYING, LAST_ROUND, IDLE -> {
                if (!gameFlow.isCurrentPlayer(this.myUsername)) {
                    hm.setState(CliState.WAIT);
                } else {
                    hm.setState(CliState.PLAY);
                }
            }

            case POST_GAME, END -> hm.setState(CliState.END);

            default -> hm.setState(CliState.WAIT);
        }

        // When receiving notification of game start, we change our state into playing state
        UserOfClient user = null;
        try {
            user = ProfileOfClient.getInstance().getUserOfClient();
        } catch (ProfileOfClientException e) {
            // Should never happen and safe recover may not be possible.
            throw new ClientCriticalError("Critical error in Client. Please start it again.");
        }
        // // user.setState(new InGameStateOfClient(user));

        IOManager.println("");
        IOManager.printPrompt(hm.getState());
    }

    private void assignUsername() {
        IOManager.print("Insert username: ");
        String username = io.read();
        boolean isUsernameValid = cm.registerUsername(username);
        while (!isUsernameValid) {
            IOManager.error("Username not valid");
            IOManager.print("Insert username: ");
            username = io.read();
            isUsernameValid = cm.registerUsername(username);
        }

        IOManager.println("Username accepted: " + username);
        this.myUsername = username;
    }

    private void runCommand(String command) {
        String[] words = command.trim().replaceAll(" +", " ").split(" ", 5);

        String keyword = words[0];
        try {
            boolean runUniversal = true;
            boolean runSpecific = true;

            IOManager.cleanTerminal();
            IOManager.printPrompt(hm.getState());
            for (String word : words) {
                IOManager.print(word + " ");
            }
            IOManager.println("");

            if (keyword.equals("help")) {
                helpCommand();
            } else if (hm.getState() != CliState.PRE_LOBBY && hm.getState() != CliState.LOBBY) {
                switch (keyword) {
                    case "hand" -> handCommand();
                    case "drawable" -> drawableCommand();
                    case "goal" -> goalCommand();
                    case "players" -> playersCommand();
                    case "points" -> pointsCommand();
                    case "manuscript" -> manuscriptCommand(words);
                    case "items" -> itemsCommand(words);
                    case "positions" -> positionsCommand();
                    case "send" -> sendCommand(words);
                    case "messages" -> messagesCommand(words);
                    case "quit" -> quitCommand();
                    default -> runUniversal = false;
                }
            } else {
                runUniversal = false;
            }

            if (hm.getState() == CliState.PRE_LOBBY) {
                switch (keyword) {
                    case "create" -> createCommand(words);
                    case "lobbies" -> lobbiesCommand();
                    case "join" -> joinCommand(words);
                    case "exit" -> killCommand();
                    default -> runSpecific = false;
                }
            } else if (hm.getState() == CliState.LOBBY) {
                switch (keyword) {
                    case "info" -> infoCommand();
                    case "start" -> startCommand();
                    case "send" -> sendCommand(words);
                    case "messages" -> messagesCommand(words);
                    case "quit" -> quitCommand();
                    default -> runSpecific = false;
                }
            } else if (hm.getState() == CliState.INIT) {
                switch (keyword) {
                    case "starter" -> starterCommand(words);
                    case "private-goals" -> privateGoalsCommand(words);
                    case "color" -> colorCommand(words);
                    default -> runSpecific = false;
                }
            } else if (hm.getState() == CliState.PLAY) {
                if (keyword.equals("play")) {
                    playCommand(words);
                } else if (keyword.equals("default")) {
                    defaultPlayCommand();
                } else {
                    runSpecific = false;
                }
            } else if (hm.getState() == CliState.WAIT) {
                runSpecific = false;
            } else if (hm.getState() == CliState.DRAW) {
                if (keyword.equals("draw")) {
                    drawCommand(words);
                } else {
                    runSpecific = false;
                }
            } else if (hm.getState() == CliState.END) {
                if (keyword.equals("winner")) {
                    winnerCommand();
                } else if (keyword.equals("quit")) {
                    quitCommand();
                } else {
                    runSpecific = false;
                }
            }

            if (!runSpecific && !runUniversal) {
                IOManager.error("Invalid command: " + words[0]);
            }
        } catch (CliInputException e) {
            IOManager.error(e.getMessage());
        }
    }

    private ConnectionManager connect(String address, int port, Boolean RMI) {
        try {
            return new ConnectionManager(address, port, RMI, (Void v) -> {
                IOManager.println("\n\n-------");
                IOManager.error("Critical error. Connection lost. Program will close.");
                System.exit(1);
            });
        } catch (Exception e){
            IOManager.error("Server unavailable");
            System.exit(1);
        }
        return null;
    }

    private void createCommand(String[] words) throws CliInputException {
        validateLength(words, 3, 3);
        int maxPlayers = validateInteger(words[2], 2, 4);
        boolean result = cmc.createLobby(words[1], maxPlayers);
        if (!result) {
            throw new CliInputException("Impossible to create lobby with name: " + words[1]);
        }
        hm.setState(CliState.LOBBY);

        this.ccc = cm.clientChatController();
    }

    private void lobbiesCommand() {
        List<LobbyInfo> lobbies = cmc.getLobbies();
        IOManager.println(lobbies.size() + " lobbies are available");
        for (LobbyInfo lobby : lobbies) {
            IOManager.println("ID: " + lobby.getUuid() + ", name: " + lobby.getName() + ", max players: " + lobby.getMaxPlayers() + ", players:");
            for (String name : lobby.getPlayerUsernames()) {
                IOManager.println("    " + name);
            }
        }
    }

    private void joinCommand(String[] words)  throws CliInputException {
        validateLength(words, 2, 2);

        boolean result = cmc.joinLobby(words[1]);
        if (!result) {
            throw new CliInputException("Impossible to join lobby with id: " + words[1]);
        }

        hm.setState(CliState.LOBBY);

        this.ccc = cm.clientChatController();
    }

    private void infoCommand() throws CliInputException {
        LobbyInfo info = cmc.getLobbyInfo();
        IOManager.println("Lobby name: " + info.getName() + ", ID: " + info.getUuid());
        IOManager.println("Players:");
        for (String player : info.getPlayerUsernames()) {
            IOManager.println("    " + player);
        }
    }

    private void startCommand() throws  CliInputException {
        boolean result = cmc.startLobby();
        if (!result) {
            throw new CliInputException("Lobby cannot be started or you are not authorized to do it.");
        } else {
            hm.setState(CliState.INIT);
            cgc = cm.clientGameController();
            hm.setCgc(cgc);
            hm.setUser(myUsername);
        }
    }

    private void killCommand() {
        System.exit(0);
    }

    private void quitCommand() throws CliInputException {
        boolean answer = false;

        if (hm.getState() == CliState.LOBBY) {
            answer = cmc.exitLobby();
        } else {
            answer = cmc.exitMatch();
        }

        if (answer) {
            // Full invalidation of controllers
            this.cgc = null;
            this.ccc = null;

            this.cm.destroyAndRebuildClientChatController(this.myUsername);
            this.cm.destroyAndRebuildClientGameController(this.myUsername);
            this.hm = new HandlerManager();

            hm.setState(CliState.PRE_LOBBY);
        } else {
            throw new CliInputException("Error on quitting");
        }
    }

    private void winnerCommand() {
        IOManager.println(cgc.getWinner());
    }

    private void drawCommand(String[] words) throws CliInputException {
        validateLength(words, 2, 2);
        int index = validateInteger(words[1], 0, 5);
        TypedCard drawn = switch (index) {
            case 0 -> cgc.drawVisibleCard(CardType.RESOURCE, 0);
            case 1 -> cgc.drawVisibleCard(CardType.RESOURCE, 1);
            case 2 -> cgc.drawVisibleCard(CardType.GOLD, 0);
            case 3 -> cgc.drawVisibleCard(CardType.GOLD, 1);
            case 4 -> cgc.drawCoveredCard(CardType.RESOURCE);
            case 5 -> cgc.drawCoveredCard(CardType.GOLD);
            default -> throw new CliInputException("Chosen index not available");
        };

        if (drawn != null) {
            hm.setState(CliState.WAIT);
        } else {
            throw new CliInputException("Card at index " + index + " is not available (empty) or action is not allowed right now");
        }
    }

    private void defaultPlayCommand() throws CliInputException {
        // Card placement
        ManuscriptPosition pos = cgc.getManuscript(myUsername).getAllAvailablePositions().stream().toList().get(0);
        TypedCard card = cgc.getPlayerData(myUsername).getHand().get(0);
        card.flip();
        cgc.placeCard(card, pos);

        // Card drawing
        if (!hm.isLastTurn()) {
            card = cgc.drawVisibleCard(CardType.RESOURCE, 0);
            if (card == null) {
                card = cgc.drawVisibleCard(CardType.RESOURCE, 1);
                if (card == null) {
                    card = cgc.drawVisibleCard(CardType.GOLD, 0);
                    if (card == null) {
                        card = cgc.drawVisibleCard(CardType.GOLD, 1);
                        if (card == null) {
                            card = cgc.drawCoveredCard(CardType.RESOURCE);
                            if (card == null) {
                                card = cgc.drawCoveredCard(CardType.GOLD);
                                if (card == null) {
                                    throw new CliInputException("Cannot draw");
                                }
                            }
                        }
                    }
                }
            }
        }

        hm.setState(CliState.WAIT);
    }

    private void playCommand(String[] words) throws CliInputException {
        validateLength(words, 4, 5);
        int index = validateInteger(words[1], 0, 2);
        int x = validateInteger(words[2]);
        int y = validateInteger(words[3]);
        boolean flip = false;
        if (words.length == 5) {
            flip = validateInteger(words[4], 0, 1) == 1;
        }

        TypedCard card = cgc.getPlayerData(myUsername).getHand().get(index);
        if (flip) {
            card.flip();
        }

        boolean result = cgc.placeCard(
                card,
                new ManuscriptPosition(x, y)
        );

        if (!result) {
            IOManager.error("Placement not accepted");
        } else {
            if (hm.isLastTurn()) {
                hm.setState(CliState.WAIT);     // We skip drawing on last turn
            } else {
                hm.setState(CliState.DRAW);
            }
        }
    }

    private void colorCommand(String[] words) throws CliInputException {
        List<PlayerColor> list = cgc.getAvailableColors();

        int length = validateLength(words, 1, 2);
        if (length == 1) {
            io.colors(list);
        } else {
            int index = validateInteger(words[1], 0, list.size() -1);
            cgc.setPlayerColor(list.get(index));
        }
    }

    private void privateGoalsCommand(String[] words) throws CliInputException {
        int length = validateLength(words, 1, 2);
        if (length == 1) {
            IOManager.println("ID 0:");
            io.goal(cgc.getProposedPrivateGoals().get(0));
            IOManager.println("ID 1:");
            io.goal(cgc.getProposedPrivateGoals().get(1));
        } else {
            int index = validateInteger(words[1], 0, 1);
            cgc.choosePrivateGoal(cgc.getProposedPrivateGoals().get(index));
        }
    }

    private void starterCommand(String[] words) throws CliInputException {
        int length = validateLength(words, 1, 2);

        if (length == 1) {
            io.starter(cgc.getStarterCard(myUsername));
        } else {
            int n = validateInteger(words[1], 0, 1);
            if (n == 0) {
                cgc.setStarterCard(CardFace.FRONT);
            } else {
                cgc.setStarterCard(CardFace.BACK);
            }
        }
    }

    private void messagesCommand(String[] words) throws CliInputException {
        validateLength(words, 2, 2);
        if (!words[1].equals(ClientChatController.BROADCAST_SENDER)) {
            validateUsernameChatControllerWay(words[1]);
        }

        List<ChatMessage> chat = cm.clientChatController().getMessagesFrom(words[1]);
        if (chat == null) {
            throw new CliInputException("Empty chat with " + words[1]);
        }

        IOManager.println(chat.size() + " unread messages");
        for (ChatMessage message : chat) {
            IOManager.println(message.sender() + ": " + message.message());
        }
    }

    private void sendCommand(String[] words) throws CliInputException {
        validateLength(words, 2);
        if (!words[1].equals(ClientChatController.BROADCAST_SENDER)) {
            validateUsernameChatControllerWay(words[1]);
        }

        String sender = words[1];
        StringBuilder message = new StringBuilder(" ");
        for (int i = 2; i < words.length; i++) {
            message.append(words[i]);
        }

        boolean answer = false;
        if (sender.equals(ClientChatController.BROADCAST_SENDER)) {
            answer = cm.clientChatController().sendBroadcastMessage(message.toString());
        } else {
            answer = cm.clientChatController().sendPrivateMessage(message.toString(), sender);
        }

        if (!answer) {
            throw new CliInputException("Message not sent");
        }
    }

    private void positionsCommand() throws CliInputException {
        io.positions(cgc.getManuscript(myUsername).getAllAvailablePositions());
    }

    private void itemsCommand(String[] words) throws CliInputException {
        int length = validateLength(words, 1, 2);
        if (length == 1) {
            io.items(cgc.getManuscript(myUsername).getItemsNumber());
        } else {
            validateUsername(words[1]);
            io.items(cgc.getManuscript(words[1]).getItemsNumber());
        }
    }

    private void manuscriptCommand(String[] words) throws CliInputException {
        int length = validateLength(words, 1, 2);
        if (length == 1) {
            IOManager.println(io.manuscript(cgc.getManuscript(myUsername)));
        } else {
            validateUsername(words[1]);
            IOManager.println(io.manuscript(cgc.getManuscript(words[1])));
        }
    }

    private void pointsCommand() {
        for (String user : cgc.getGameFlow().getAllUsernames()) {
            if (user.equals(myUsername)) {
                IOManager.print("(You) ");
            }
            IOManager.println(user + ": " + cgc.getPlayerData(user).getPoints());
        }
    }

    private void playersCommand() {
        for (String user : cgc.getGameFlow().getAllUsernames()) {
            if (user.equals(myUsername)) {
                IOManager.print("(You) ");
            }
            PlayerColor color = cgc.getPlayerData(user).getColor();
            if (color != null) {
                IOManager.print("(" + color + ") ");
            }
            IOManager.println(user);
        }
    }

    private void goalCommand() {
        CommonBoard board = cgc.getGameBoard();
        io.goal(board.getCommonGoals().get(0));
        io.goal(board.getCommonGoals().get(1));
        GoalCard privateGoal = cgc.getPlayerData(myUsername).getPrivateGoal();
        if (privateGoal != null) {
            io.goal(privateGoal);
        }
    }

    private void drawableCommand() {
        CommonBoard board = cgc.getGameBoard();
        IOManager.println("ID 0 (resource card):");
        io.typed(board.getVisibleCards(CardType.RESOURCE).get(0));
        IOManager.println("ID 1 (resource card):");
        io.typed(board.getVisibleCards(CardType.RESOURCE).get(1));
        IOManager.println("ID 2 (gold card):");
        io.typed(board.getVisibleCards(CardType.GOLD).get(0));
        IOManager.println("ID 3 (gold card):");
        io.typed(board.getVisibleCards(CardType.GOLD).get(1));
        IOManager.println("ID 4 (covered resource card):");
        io.coveredCard(board.peekTopCard(CardType.RESOURCE));
        IOManager.println("ID 5 (covered gold card):");
        io.coveredCard(board.peekTopCard(CardType.GOLD));
    }

    private void handCommand() {
        io.hand(cgc.getPlayerData(myUsername).getHand());
    }

    private void helpCommand() {
        IOManager.println(
                """
                        Codex Naturalis CLI
                        Usage: cli [username] [server IP address] [server port]

                        The CLI can be in one of these states: PRE_LOBBY, LOBBY, INIT, WAIT, PLAY, DRAW, END.
                        In each state you can enter specific commands, as shown below.
                        You can always enter the 'help' command, which shows this message.
                        In your state you can use:
                        """
        );

        // Universal commands
        if (hm.getState() != CliState.PRE_LOBBY && hm.getState() != CliState.LOBBY) {
            IOManager.println("""
                        Universal commands (can be used in any game mode, so all except PRE_LOBBY and LOBBY):
                        - points: shows the points of each player
                        - hand: shows your hand
                        - drawable: shows the cards available for drawing
                        - goal: shows the public goals and your private goal
                        - players: shows players usernames
                        - points: shows players points
                        - manuscript [USERNAME]: shows [USERNAME]'s manuscript. Without arguments, shows your manuscript.
                        - items [USERNAME]: shows [USERNAME]'s list of items. Without arguments, shows your list of items.
                        - positions: shows available positions in manuscript
                        - send ID MESSAGE: send MESSAGE to ID. If ID is *, send in broadcast.
                        - messages ID: show unread messages from ID. If *, show broadcast messages.
                        - quit: quit the game"""
            );
        }

        if (hm.getState() == CliState.PRE_LOBBY) {
            IOManager.println("""
                        PRE_LOBBY commands:
                        - create NAME MAX: Try to create lobby with name NAME and max number of players MAX
                        - lobbies: Show list of available lobbies.
                        - join ID: Try to join lobby with id ID (IDs can be obtained from 'lobbies' command)"""
            );
        } else if (hm.getState() == CliState.LOBBY) {
            IOManager.println("""
                        LOBBY commands:
                        - info: Show info on current lobby
                        - start: If you created the lobby, you can start it.
                        - send ID MESSAGE: send MESSAGE to ID. If ID is *, send in broadcast.
                        - messages ID: show unread messages from ID. If *, show broadcast messages.
                        - quit: quit the game"""
            );
        } else if (hm.getState() == CliState.INIT) {
            IOManager.println("""
                        INIT commands:
                        - starter 0|1: choose starter card side (0 -> front, 1 -> back). Without arguments, show starter card.
                        - private-goals 0|1 : choose goal card. Without arguments, shows goal cards.
                        - color ID: choose color with id ID. Without arguments, show available colors."""
            );
        } else if (hm.getState() == CliState.PLAY) {
            IOManager.println("""
                        PLAY commands:
                        - play ID X Y [FACE]: plays card with id ID (IDs can be obtained from 'hand' command) at [X, Y].
                                              If FACE is 1, card is placed on back, else on front (default on front)."""
            );
        } else if (hm.getState() == CliState.DRAW) {
            IOManager.println("""
                        DRAW commands:
                        - draw ID: draws card with id ID (IDs can be obtained from 'drawable' command)"""
            );
        } else if (hm.getState() == CliState.END) {
            IOManager.println("""
                        END commands:
                        - winner: shows winner username
                        - quit: quit the game"""
            );
        }

        if (hm.getState() != CliState.PRE_LOBBY && hm.getState() != CliState.LOBBY) {
            IOManager.println("""
                                            
                    Items table:
                    I: insect
                    F: fungi
                    P: plant
                    A: animal
                    W: inkwell
                    Q: quill
                    M: manuscript"""
            );
        }
    }

    private int validateLength(String[] words, int min) throws CliInputException {
        int length = words.length;
        if (length < min) {
            throw new CliInputException("Command must have at least " + min + " words");
        }

        return length;
    }

    private int validateLength(String[] words, int min, int max) throws CliInputException {
        int length = words.length;
        if (length < min || length > max) {
            if (min != max) {
                throw new CliInputException("Command must have between " + min + " and " + max + "words");
            } else {
                throw new CliInputException("Command must have " + min + " words");
            }
        }

        return length;
    }

    private void validateUsername(String username) throws CliInputException {
        if (!cgc.getGameFlow().getAllUsernames().contains(username)) {
            throw new CliInputException("Username " + username + " not found");
        }
    }

    private void validateUsernameChatControllerWay(String username) throws CliInputException {
        if (!ccc.getRecipients().contains(username)) {
            throw new CliInputException("Username " + username + " not found");
        }
    }

    private int validateInteger(String word, int min, int max) throws CliInputException {
        int n = validateInteger(word);
        if (n < min || n > max) {
            throw new CliInputException(n + " must be between " + min + " and " + max);
        }
        return n;
    }

    private int validateInteger(String word) throws CliInputException {
        try {
            return Integer.parseInt(word);
        } catch (NumberFormatException e) {
            throw new CliInputException(word + " is not a number");
        }
    }
}
