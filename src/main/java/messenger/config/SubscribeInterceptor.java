package messenger.config;

import messenger.model.RoleType;
import messenger.model.User;
import messenger.services.UserService;
import netscape.security.ForbiddenTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class SubscribeInterceptor extends ChannelInterceptorAdapter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Principal principal = accessor.getUser();
            if (!validateSubscribe(principal, accessor.getDestination())) {
                logger.error("User {} try subscribe on {} without permission",
                        principal.getName(), accessor.getDestination());
                throw new ForbiddenTargetException("No permission for this topic");
            }
        }
        return message;
    }

    private boolean validateSubscribe(Principal principal, String destination) {
        String prefix = "/topic/talk/";
        if (destination.startsWith(prefix)) {
            String strIdTalk = destination.substring(prefix.length());
            int idTalk = Integer.parseInt(strIdTalk);
            User user = (User) ((Authentication) principal).getPrincipal();
            return userService.isUserInTalk(user, idTalk) ||
                    userService.isUserInRole(user, RoleType.ROLE_SUPER_USER) ||
                    userService.isUserInRole(user, RoleType.ROLE_ADMIN);
        }
        return false;
    }
}
