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

    private final String ACTIVATIONPARM = "activation";

    public ActionInterceptor(
            @Value("${tank.activationkey:0000}") String activationKey) {

        this.activationKey = activationKey;
    }

    private String activationKey;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {
        Cookie cookie = WebUtils.getCookie(request, ACTIVATIONPARM);
        if (cookie == null){
            System.out.println("Ignoring post request without activation key cookie");
            return false;
        }
        String activation = cookie.getValue();
        boolean doIt = activationKey.equals(activation);
        if (!doIt){
            System.out.println("Ignoring post request without correct activation key");
        }
        return ( doIt );
    }

}
