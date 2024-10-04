package it.polimi.ingsw.gui.support;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.Math.min;
import static java.util.stream.IntStream.range;

public class FXBind {
    public static <T> void updateObservablePropList(List<T> dataList, ObservableList<ObjectProperty<T>> obsList) {
        List<T> oldList = dataList.subList(0, min(obsList.size(), dataList.size()));

        // update old
        range(0, oldList.size()).forEach(i -> obsList.get(i).set(dataList.get(i)));

        //remove
        if (obsList.size() >= dataList.size()) obsList.remove(dataList.size(), obsList.size());
        else {
            // add new
            List<T> newList = dataList.subList(obsList.size(), dataList.size());
            obsList.addAll(newList.stream().map(SimpleObjectProperty::new).toList());
        }
    }

    public static <T> void updateObservableList(List<T> dataList, ObservableList<T> obsList) {
        obsList.addAll(dataList);
    }

    public static <T> Runnable subscribe(ObservableValue<T> observable, Consumer<? super T> subscriber) {
        subscriber.accept(observable.getValue());
        ChangeListener<? super T> listener = (obs, oldValue, newValue) -> subscriber.accept(newValue);
        observable.addListener(listener);
        return () -> observable.removeListener(listener);
    }

    public static <T, U> ObjectBinding<U> map(ObservableValue<T> src, Function<? super T, ? extends U> f) {
        return new ObjectBinding<>() {
            {
                bind(src);
            }

            @Override
            protected U computeValue() {
                T baseVal = src.getValue();
                return baseVal != null ? f.apply(baseVal) : null;
            }

            @Override
            public void dispose() {
                unbind(src);
            }
        };
    }

    public static <F, E> ObservableList<E> mapList(ObservableList<? extends F> source, Function<F, E> mapper) {
        ObservableList<E> mapList = new MappedList<>(source, mapper);
        ObservableList<E> newList = FXCollections.observableArrayList();
        Bindings.bindContentBidirectional(newList, mapList);
        return newList;
    }
}
