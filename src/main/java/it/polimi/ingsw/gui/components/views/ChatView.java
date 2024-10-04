package it.polimi.ingsw.gui.components.views;

import it.polimi.ingsw.controller.ChatMessage;
import it.polimi.ingsw.controller.event.chat.ChatEvents;
import it.polimi.ingsw.controller.interfaces.ClientChatController;
import it.polimi.ingsw.gui.support.FXBind;
import it.polimi.ingsw.gui.support.FXUtils;
import it.polimi.ingsw.gui.support.AnimationBuilder;
import it.polimi.ingsw.gui.support.ComponentBuilder;
import it.polimi.ingsw.gui.support.context.ChatContext;
import it.polimi.ingsw.gui.support.context.ContextManager;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.*;
import java.util.function.Function;

public class ChatView extends VBox implements Initializable {
    private final String mainUsername;
    private final ChatContext chatCtx;
    private final ObjectProperty<String> currentRecipient;
    private final StringProperty unreadMessage;
    public VBox unreadBox;
    private ObservableList<Node> messagesNodes;
    @FXML
    private VBox chatMessagesBox;
    private List<String> usernameList;
    @FXML
    private ScrollPane messageScroll;
    @FXML
    private ComboBox<String> userComboBox;
    private final Map<String, Pane> messagePanes;

    @FXML
    private TextArea textArea;

    public ChatView(ContextManager ctxMan) {
        this.chatCtx = ctxMan.chatCtx();
        FXUtils.loadRootFXMLView(this);
        mainUsername = chatCtx.cc().getMyUsername();
        messagePanes = new HashMap<>();

        unreadMessage = new SimpleStringProperty();
        DoubleProperty ratio = new SimpleDoubleProperty(18);

        currentRecipient = new SimpleObjectProperty<>(ClientChatController.BROADCAST_SENDER);

        FXBind.subscribe(currentRecipient,
                recipient -> {
                    chatMessagesBox.getChildren().set(0, getMessagesBox(recipient));
                    AnimationBuilder.moveScrollDown(messageScroll);
                    chatCtx.readMessages(recipient);
                    unreadMessage.set(String.valueOf(chatCtx.sumUnreadMessages()));
                }
        );


        unreadBox.getChildren().add(ComponentBuilder.cirleName((FXBind.map(unreadMessage, Function.identity())), ratio));


        chatCtx.cc().addEventHandler(ChatEvents.BROADCAST_MESSAGE,
                e -> onNewMessage(ClientChatController.BROADCAST_SENDER, e.message())
        );

        chatCtx.cc().addEventHandler(ChatEvents.PRIVATE_MESSAGE, e -> {
            String sender = e.message().sender();
            String chatName = sender.equals(mainUsername) ? e.recipient() : sender;
            onNewMessage(chatName, e.message());
        });

        FXBind.subscribe(
                ctxMan.matchCtx().currentLobbyProperty(),
                l -> {
                    System.out.println("UPDATE RECIPIENTS " + chatCtx.cc().getRecipients());
                    userComboBox.getItems().setAll(chatCtx.getRecipients());
                }
        );

        ComponentBuilder.speedUpScroll(messageScroll);

        FXBind.subscribe(userComboBox.getSelectionModel().selectedItemProperty(), s -> {
            if (s != null) {
                currentRecipient.set(s);
            } else currentRecipient.set(ClientChatController.BROADCAST_SENDER);
        });

        userComboBox.getSelectionModel().select(ClientChatController.BROADCAST_SENDER);
        HBox.setHgrow(this, Priority.ALWAYS);

    }

    private Pane getMessagesBox(String sender) {
        return messagePanes.computeIfAbsent(sender, k -> new VBox());
    }

    private void onNewMessage(String s, ChatMessage m) {
        chatCtx.readMessages(currentRecipient.get());
        ObservableList<Node> messagesNode = getMessagesBox(s).getChildren();
        System.out.println("handler");

        Platform.runLater(() -> {
            unreadMessage.set(String.valueOf(chatCtx.sumUnreadMessages()));

            messagesNode.add(newMessageNode(m, s));
            AnimationBuilder.moveScrollDown(messageScroll);
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        textArea.addEventFilter(KeyEvent.KEY_PRESSED, this::handlerKeyPress);
    }

    private void handlerKeyPress(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            keyEvent.consume();
            String text = textArea.getText();
            if (!text.isEmpty()) {
                sendMessage(text);
            }
            textArea.setText("");
        }
    }

    private void sendMessage(String text) {
        String recipient = currentRecipient.get();
        System.out.println(mainUsername + text + " " + recipient);

        if (recipient != null) {
            if (recipient.equals(ClientChatController.BROADCAST_SENDER)) {
                chatCtx.cc().sendBroadcastMessage(text);
            } else {
                chatCtx.cc().sendPrivateMessage(text, recipient);
            }
        }

    }

    private Pane newMessageNode(ChatMessage m, String chatName) {
        String sender = m.sender();
        List<ChatMessage> messages = chatCtx.cc().getMessagesFrom(chatName);
        if (messages == null) messages = new ArrayList<>();
        int i = messages.size();

        VBox box = new VBox();
        if (i > 1) {
            ChatMessage lastMessage = messages.get(i - 2);
            String lastSender = lastMessage.sender();
            if (!sender.equals(mainUsername) && !lastSender.equals(sender))
                box.getChildren().add(ComponentBuilder.vSpacer());

            box.getChildren().add(messagePane(m, !lastSender.equals(sender)));
        } else {
            box.getChildren().add(messagePane(m, true));
        }
        return box;
    }


    Pane messagePane(ChatMessage m, boolean showSender) {
        if (m.sender().equals(mainUsername)) {
            return ComponentBuilder.messageMine(m);
        } else return ComponentBuilder.messageOther(m, showSender);
    }

}

