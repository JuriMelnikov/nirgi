package ee.jvm.nirgi_java.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            // Redirect based on user roles
            String authorities = userDetails.getAuthorities().toString();
            if (authorities.contains("ROLE_ADMINISTRATOR")) {
                return "redirect:/employees";
            } else if (authorities.contains("ROLE_MANAGER")) {
                return "redirect:/employees";
            } else if (authorities.contains("ROLE_TECHNOLOGIST")) {
                return "redirect:/models";
            } else if (authorities.contains("ROLE_MASTER")) {
                return "redirect:/orders";
            } else if (authorities.contains("ROLE_EMPLOYEE")) {
                return "redirect:/work-results";
            } else if (authorities.contains("ROLE_ACCOUNTANT")) {
                return "redirect:/salary";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/employees")
    public String employees() {
        return "employees";
    }

    @GetMapping("/orders")
    public String orders() {
        return "orders";
    }

    @GetMapping("/work-results")
    public String workResults() {
        return "work-results";
    }

    @GetMapping("/salary")
    public String salary() {
        return "salary";
    }
}
