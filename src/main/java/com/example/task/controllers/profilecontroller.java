package com.example.task.controllers;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

@Controller
public class profilecontroller {
    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    private final String testlink = "api.ok654.org";

    @GetMapping(path="/profile")
    public String profile(Model model ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Object prince = authentication.getPrincipal();
            String pfp = ((DefaultOidcUser) prince).getPicture();
            model.addAttribute("pfp",pfp);
            String username=((DefaultOidcUser) prince).getFullName();
            model.addAttribute("username",username);
            String email=((DefaultOidcUser) prince).getEmail();
            model.addAttribute("email",email);
            String phone=((DefaultOidcUser) prince).getPhoneNumber();
            model.addAttribute("phone",phone);

        }
        model.addAttribute("my",2);

        return "profile";
    }
    @GetMapping(path="/user/{id}")
    public String user(@PathVariable("id")String id, Model model ){
        if (Objects.equals(maincontroller.cocok, "")){
            return "redirect:/success/";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {

            Object prince = authentication.getPrincipal();

            String userid = ((DefaultOidcUser) prince).getName();
            if(Objects.equals(userid, id)) {
                model.addAttribute("my", 2);
                return "redirect:/profile";
            }

            String pfp = ((DefaultOidcUser) prince).getPicture();
            model.addAttribute("pfp",pfp);
            String username=((DefaultOidcUser) prince).getGivenName()+" "+((DefaultOidcUser) prince).getFamilyName();
            model.addAttribute("username",username);
            String email=((DefaultOidcUser) prince).getEmail();
            model.addAttribute("email",email);



        }
        try {

            URL url = new URL("https://"+testlink+"/user/"+id);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setUseCaches(true);

            conn.setRequestProperty("Cookie",
                    maincontroller.cocok);

            conn.connect();
            int status = conn.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(),"utf-8"));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            JSONParser parse = new JSONParser();
            JSONObject data_obj = (JSONObject) parse.parse(response.toString());
            String username="";
            if(data_obj.get("firstName")!=null)
                username=username+data_obj.get("firstName").toString()+" ";
            if(data_obj.get("secondName")!=null)
                username=username+data_obj.get("secondName").toString()+" ";
            if(data_obj.get("middleName")!=null)
                username=username+data_obj.get("middleName").toString();
            model.addAttribute("username2", username);
            String email=data_obj.get("email").toString();
            model.addAttribute("email", email);

        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("my",2);
        return "user";
    }

}
