package messenger.controller;

import messenger.exception.ResourceNotFoundException;
import netscape.security.ForbiddenTargetException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionController {
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String runtimeException(Model model) {
        model.addAttribute("errorMessage", "Error on the server side!");
        return "error";
    }

    @ExceptionHandler(ForbiddenTargetException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String accessException(Model model) {
        model.addAttribute("errorMessage", "You don't have permission to access!");
        return "error";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String notFound(Model model) {
        model.addAttribute("errorMessage", "Resource not found!");
        return "error";
    }
}
