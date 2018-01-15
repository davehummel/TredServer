package me.davehummel.tredserver.services;

import me.davehummel.tredserver.serial.StandardLine;

/**
 * Created by dmhum_000 on 9/6/2015.
 */
public abstract class CommandListener {

    public abstract boolean matches(StandardLine line);

    protected abstract void processData(StandardLine data);

}
