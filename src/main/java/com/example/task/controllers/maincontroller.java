package com.example.task.controllers;

import org.json.simple.parser.ParseException;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller

public class maincontroller {
    private final String testlink = "api.ok654.org";
    public static String cocok="";
    @GetMapping("/")
    public String home(  Model model) throws IOException {

        return "redirect:/tasks/";
    }
    @GetMapping("/success")
    public String success(Model model) throws IOException, ParseException {
        if(Objects.equals(cocok, "")) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication instanceof AnonymousAuthenticationToken)) {
                Object prince = authentication.getPrincipal();

                //model.addAttribute("token",((DefaultOidcUser) prince).getIdToken().getTokenValue());
                Map<String, List<String>> headerFields;


                URL url = new URL("https://" + testlink + "/login");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                String jsonInputString = "idToken=" + ((DefaultOidcUser) prince).getIdToken().getTokenValue();
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                //?
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("charset", "utf-8");

                try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                    wr.write(input);
                }
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }

                final String COOKIES_HEADER = "Set-Cookie";
                headerFields = conn.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

                if (cookiesHeader != null) {
                    cocok = cookiesHeader.get(0);

                }
            }


        }
            return "redirect:/tasks/";
    }

    @GetMapping("/error/{ercode}")
    public String error(@PathVariable("ercode") String ercode, Model model) throws IOException, ParseException {
        if (Objects.equals(maincontroller.cocok, "")){
            return "redirect:/success/";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String idd="";
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Object prince = authentication.getPrincipal();
            idd=((DefaultOidcUser) prince).getName();
            String pfp = ((DefaultOidcUser) prince).getPicture();
            model.addAttribute("pfp",pfp);
            String username=((DefaultOidcUser) prince).getGivenName()+" "+((DefaultOidcUser) prince).getFamilyName();
            model.addAttribute("username",username);

            //model.addAttribute("token",((DefaultOidcUser) prince).getIdToken().getTokenValue());

        }

        if(Objects.equals(ercode, "noacc"))
            model.addAttribute("error","Произошла ошибка. Возможно, стоит попробовать авторизоваться с помощью другого аккаунта. \n Если проблема не исчезла, то обратитесь к разработчику");
        else
            model.addAttribute("error","Неизвестная ошибка.");
        model.addAttribute("ercode",ercode);
        model.addAttribute("my",2);
        return "errorpage";
    }


}
