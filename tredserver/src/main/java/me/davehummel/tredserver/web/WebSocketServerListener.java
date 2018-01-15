package me.davehummel.tredserver.web;

/**
 * Created by dmhum_000 on 8/9/2015.
 */
public interface WebSocketServerListener {
    void receive(String text);
}
