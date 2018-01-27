package me.davehummel.tredserver.fish;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ActionInterceptor extends HandlerInterceptorAdapter {

    private static final String GET = "GET";
    private final String ACTIVATIONPARM = "activation";

    public ActionInterceptor(
            @Value("${tank.activationkey:0000}") String activationKey) {
        System.out.println(activationKey);
        this.activationKey = activationKey;
    }

    private String activationKey;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        if (GET.equalsIgnoreCase(request.getMethod())){
            return true;
        }

        String activation = null;
        Cookie cookie = WebUtils.getCookie(request, ACTIVATIONPARM);

        if (cookie != null && cookie.getValue() != null  ) {
            activation = cookie.getValue();
            System.out.println("Cookie value found");
        }

        boolean doIt = activationKey.equals(activation);

        if (!doIt){
            activation = request.getParameter(ACTIVATIONPARM);
            doIt = activationKey.equals(activation);
            System.out.println("Cookie wrong, trying parameter");
        }
        if (!doIt){
            System.out.println("Ignoring post request without correct activation key");
        }
        return ( doIt );
    }

}
