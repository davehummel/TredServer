package me.davehummel.tredserver.fish.history;


import java.util.function.Supplier;

public interface ResetingSupplier extends Supplier<Double> {

    // These suppliers can be cleared on an improtant event (timed interval..)
    void resetState();

}
