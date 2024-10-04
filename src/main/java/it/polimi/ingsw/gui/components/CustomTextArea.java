package it.polimi.ingsw.gui.components;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;

// https://gist.github.com/IdelsTak/a33a5ff0d09a63bd30ed21dbecce4c24
public class CustomTextArea extends TextArea {
    final TextArea myTextArea = this;

    public CustomTextArea() {
        addEventFilter(KeyEvent.KEY_PRESSED, new TabAndEnterHandler());
        addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
    }

    class TabAndEnterHandler implements EventHandler<KeyEvent> {
        private KeyEvent recodedEvent;

        @Override
        public void handle(KeyEvent event) {
            if (recodedEvent != null) {
                recodedEvent = null;
                return;
            }

            Parent parent = myTextArea.getParent();
            if (parent != null) {
                switch (event.getCode()) {
                    case ENTER:
                        if (event.isControlDown()) {
                            recodedEvent = recodeWithoutControlDown(event);
                            myTextArea.fireEvent(recodedEvent);
                        } else {
                            Event parentEvent = event.copyFor(parent, parent);
                            myTextArea.getParent().fireEvent(parentEvent);
                        }
                        event.consume();
                        break;

                    case TAB:
                        if (event.isControlDown() || event.isShiftDown()) {
                            recodedEvent = recodeWithoutControlDown(event);
                            myTextArea.fireEvent(recodedEvent);
                        } else {
                            Node node = myTextArea;
                            while (parent != null && !focusForward(node)) {
                                node = parent;
                                parent = parent.getParent();
                            }
                        }
                        event.consume();
                        break;
                }
            }
        }

        private void focusNext(Node node) {
            Parent parent = node.getParent();
            if (parent != null) {
                ObservableList<Node> children = parent.getChildrenUnmodifiable();
                int idx = children.indexOf(node);
                if (idx >= 0) {
                    for (int i = idx + 1; i < children.size(); i++) {
                        if (children.get(i).isFocusTraversable()) {
                            children.get(i).requestFocus();
                            break;
                        }
                    }
                    for (int i = 0; i < idx; i++) {
                        if (children.get(i).isFocusTraversable()) {
                            children.get(i).requestFocus();
                            break;
                        }
                    }
                }
            }
        }

        private boolean focusForward(Node node) {
            Parent parent = node.getParent();
            if (parent != null) {
                ObservableList<Node> children = parent.getChildrenUnmodifiable();
                int idx = children.indexOf(node);
                if (idx >= 0) {
                    for (int i = idx + 1; i < children.size(); i++) {
                        if (children.get(i).isFocusTraversable()) {
                            children.get(i).requestFocus();
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private KeyEvent recodeWithoutControlDown(KeyEvent event) {
            return new KeyEvent(
                    event.getEventType(),
                    event.getCharacter(),
                    event.getText(),
                    event.getCode(),
                    event.isShiftDown(),
                    false,
                    event.isAltDown(),
                    event.isMetaDown()
            );
        }
    }

}
