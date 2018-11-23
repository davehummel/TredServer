package me.davehummel.tredserver.fish.history;


import java.util.function.Supplier;

public interface ResettingSupplier extends Supplier<Double> {

    // These suppliers can be cleared on an important event (timed interval..)
    void resetState();

}
