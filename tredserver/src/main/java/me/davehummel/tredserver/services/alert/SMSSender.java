package me.davehummel.tredserver.services.alert;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by dmhum_000 on 4/9/2017.
 */

@Component
public class SMSSender {

    private String ACCOUNT_SID ;
    private String AUTH_TOKEN ;

    private static boolean armed = false;

    public SMSSender(
            @Value("${twilio.account_sid:0000}") String ACCOUNT_SID,  @Value("${twilio.auth_token:0000}") String AUTH_TOKEN) {

        this.ACCOUNT_SID = ACCOUNT_SID;
        this.AUTH_TOKEN = AUTH_TOKEN;

        Twilio.init(ACCOUNT_SID,AUTH_TOKEN);
    }


    public static final PhoneNumber davePhone = new PhoneNumber("+18157618266");
    public static final PhoneNumber nickiPhone = new PhoneNumber("+18157629195");

    public static final PhoneNumber smsPhoneNumber = new PhoneNumber("+13312072608");

    private static final PhoneNumber[] NUMBERS = new PhoneNumber[]{davePhone, nickiPhone};

    public void sendSMS(String body) {
        if (!armed){
            System.err.println("Sending SMS (Disarmed):"+body);
            return;
        }
        for (PhoneNumber number : NUMBERS) {
            Message message = Message.creator(number,  // to
                    smsPhoneNumber,  // from
                    body)
                    .create();
        }
    }


    public static void arm() {
        armed = true;
    }

    public static void disarm() {
        armed = false;
    }
}
