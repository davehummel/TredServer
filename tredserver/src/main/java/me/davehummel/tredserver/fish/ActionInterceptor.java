package me.davehummel.tredserver.fish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ActionInterceptor extends HandlerInterceptorAdapter {

    Logger logger = LoggerFactory.getLogger(ActionInterceptor.class);

    private static final String GET = "GET";
    private final String ACTIVATIONPARM = "activation";

    public ActionInterceptor(
            @Value("${tank.activationkey:0000}") String activationKey) {
        logger.info(activationKey);
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
            logger.info("Cookie value found");
        }

        boolean doIt = activationKey.equals(activation);

        if (!doIt){
            activation = request.getParameter(ACTIVATIONPARM);
            doIt = activationKey.equals(activation);
            logger.info("Cookie wrong, trying parameter");
        }
        if (!doIt){
            logger.error("Ignoring post request without correct activation key");
        }
        return ( doIt );
    }

}
