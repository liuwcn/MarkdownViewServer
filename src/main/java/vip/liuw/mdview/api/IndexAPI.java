package vip.liuw.mdview.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RequestMapping
@Controller
public class IndexAPI {
    @Value("${myconfig.token}")
    private String systemToken;

    @GetMapping
    public String index(@RequestParam String token, HttpServletResponse resp) {
        if (systemToken.equals(token)) {
            Cookie cookie = new Cookie("token", systemToken);
            resp.addCookie(cookie);
            return "index.html";
        }
        return "404";
    }
}
