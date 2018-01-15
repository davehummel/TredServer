package me.davehummel.tredserver.services.alert;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 * Created by dmhum_000 on 4/9/2017.
 */
public class SMSSender {

    public static final String ACCOUNT_SID = "ACdc8a2675dbd0a3a6b1af82719933a068";
    public static final String AUTH_TOKEN = "c73b8909ab4124f953bcc65ca3ab0cf4";
    private static boolean armed = false;

    static {
        Twilio.init(ACCOUNT_SID,AUTH_TOKEN);
    }

    public static final PhoneNumber davePhone = new PhoneNumber("+18157618266");
    public static final PhoneNumber nickiPhone = new PhoneNumber("+18157629195");

    public static final PhoneNumber smsPhoneNumber = new PhoneNumber("+13312072608");

    private static final PhoneNumber[] NUMBERS = new PhoneNumber[]{davePhone, nickiPhone};

    public static void sendSMS(String body) {
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
