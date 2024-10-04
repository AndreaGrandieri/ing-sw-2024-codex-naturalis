package it.polimi.ingsw.gui.components.panes.board;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;
import static java.lang.Math.random;

public class MyColors {
    public static final Color MY_BROWN = Color.web("563b1b");
    public static final Color MY_LIGHT_BROWN = Color.web("90622c");
    public static final Color MY_DARK_YELLOW = Color.web("b2a21d");
    public static final Color MY_LIGHT_YELLOW = Color.web("e3dbb5");

    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(List.of(0, -1, -2, -3));


        for (int k = 0; k < 10; k++) {
            int currInd = k % 4;
            boolean moves = ((int) (random() * 10)) % 2 == 0;

            if (moves) {

                list = getList(currInd, list);

            }

            System.out.println(moves + " " + currInd);
            System.out.println(list);
        }
    }

    private static List<Integer> getList(int currInd, List<Integer> list) {

        list = new ArrayList<>(list);
        list.set(currInd, 3);

        for (int i = -3; i < 0; i++) {
            int index = list.indexOf(i);
            if (index < 0) break;
            list.set(index, -(list.get(index) + 1));
        }

        return list.stream().map(l -> min(-l, l)).toList();
    }
}
