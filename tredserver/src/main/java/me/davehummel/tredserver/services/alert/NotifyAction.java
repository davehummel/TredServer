package me.davehummel.tredserver.services.alert;

/**
 * Created by dmhum_000 on 4/2/2017.
 */
public interface NotifyAction {

    void alert(Alert parent);

    void endAlert(Alert parent);

    void critical(Alert parent);

}
