package com.example.task.controllers;


import com.aspose.cells.*;
import com.ibm.icu.text.Transliterator;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.*;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.util.*;

@Controller
@RequestMapping(path="/tasks/")
public class taskcontroller {

public static final String CYRILLIC_TO_LATIN = "Cyrillic-Latin";

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;
    private final String testlink = "api.ok654.org";
    @GetMapping(path="/")
    public String hometask(Model model /*OAuth2AuthenticationToken authentication*/) throws IOException, ParseException, java.text.ParseException {

        if (Objects.equals(maincontroller.cocok, "")){
            return "redirect:/success/";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            Object prince = authentication.getPrincipal();
            String pfp = ((DefaultOidcUser) prince).getPicture();
            model.addAttribute("pfp",pfp);
            String username=((DefaultOidcUser) prince).getGivenName()+" "+((DefaultOidcUser) prince).getFamilyName();
            model.addAttribute("username",username);

        String id="";

        try {

            if (!(authentication instanceof AnonymousAuthenticationToken)) {
                prince = authentication.getPrincipal();
                id=((DefaultOidcUser) prince).getName();
            }

            URL url = new URL("https://"+testlink+"/user/tasks/given");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");

        con.setUseCaches(true);

                con.setRequestProperty("Cookie",
                        maincontroller.cocok);

            con.connect();
        int status = con.getResponseCode();

        BufferedReader in = new BufferedReader(new InputStreamReader(
                con.getInputStream(),"utf-8"));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
                JSONParser parse = new JSONParser();
                JSONArray data_obj = (JSONArray) parse.parse(response.toString());


                SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm",new Locale("ru", "RU"));
                model.addAttribute("df1",df1);
                SimpleDateFormat df2 = new SimpleDateFormat("dd MMMM yyyy HH:mm",new Locale("ru", "RU"));
                model.addAttribute("df2",df2);
            Date now = df1.parse(LocalDateTime.now().toString().replace("T"," "));
            model.addAttribute("now",now);

                model.addAttribute("task",data_obj);


        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("my",0);



        }
        model.addAttribute("my",0);



        return "tasks/tasklist";
    }


    @GetMapping(path="/filter/{fparam}")
    public String filterget (@PathVariable("fparam") String fparam, Model model){
        if (Objects.equals(maincontroller.cocok, "")){
            return "redirect:/success/";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String id="";

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Object prince = authentication.getPrincipal();
            String idd=((DefaultOidcUser) prince).getName();
            model.addAttribute("usid",idd);
            String pfp = ((DefaultOidcUser) prince).getPicture();
            model.addAttribute("pfp",pfp);
            String username=((DefaultOidcUser) prince).getGivenName()+" "+((DefaultOidcUser) prince).getFamilyName();
            model.addAttribute("username",username);
        }
        try {
            if (!(authentication instanceof AnonymousAuthenticationToken)) {
                Object prince = authentication.getPrincipal();
                id=((DefaultOidcUser) prince).getName();
            }
            URL url = new URL("https://"+testlink+"/user/tasks/given");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            conn.setUseCaches(true);

            conn.setRequestProperty("Cookie",
                    maincontroller.cocok);

            conn.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(),"utf-8"));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();



            //Using the JSON simple library parse the string into a json object
            JSONParser parse = new JSONParser();
            JSONArray data_obj = (JSONArray) parse.parse(response.toString());

            SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm",new Locale("ru", "RU"));
            model.addAttribute("df1",df1);
            SimpleDateFormat df2 = new SimpleDateFormat("dd MMMM yyyy HH:mm",new Locale("ru", "RU"));
            model.addAttribute("df2",df2);
            Date now = df1.parse(LocalDateTime.now().toString().replace("T"," "));
            model.addAttribute("now",now);

            Iterator iter=data_obj.iterator();
            Object prince = authentication.getPrincipal();
            String usid = ((DefaultOidcUser) prince).getName();
            if(Objects.equals(fparam, "Overdue")){
                while(iter.hasNext()) {
                    JSONObject element = (JSONObject) iter.next();

                            if (df1.parse(element.get("deadline").toString().replace('T',' ')).after(now)) {
                                iter.remove();
                            }
                }
                model.addAttribute("task",data_obj);
                model.addAttribute("my",0);



                return "tasks/tasklist";
            }
            else if(Objects.equals(fparam, "Due")){
                while(iter.hasNext()) {
                    JSONObject element = (JSONObject) iter.next();
                            if (!df1.parse(element.get("deadline").toString().replace('T',' ')).after(now)) {
                                iter.remove();
                            }
                }

                model.addAttribute("task",data_obj);
                model.addAttribute("my",0);



                return "tasks/tasklist";
            }
            else {

                model.addAttribute("task",data_obj);
                model.addAttribute("my",0);



                return "metasks/tasklist";
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("my",0);



        return "tasks/tasklist";
    }

    @GetMapping(value="/export", produces = { "application/octet-stream" })
    public ResponseEntity<byte[]> exportToCSV(HttpServletResponse response) throws Exception {

        String id="";
        String name="";
        JSONArray data_obj=new JSONArray();
        JSONArray realdata=new JSONArray();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            if (!(authentication instanceof AnonymousAuthenticationToken)) {
                Object prince = authentication.getPrincipal();
                id=((DefaultOidcUser) prince).getName();
                name=((DefaultOidcUser) prince).getFullName();
            }
            URL url = new URL("https://"+testlink+"/user/tasks/given");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setUseCaches(true);

            conn.setRequestProperty("Cookie",
                    maincontroller.cocok);

            conn.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(),"utf-8"));
            String inputLine;
            StringBuffer responsee = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                responsee.append(inputLine);
            }
            in.close();

                //Using the JSON simple library parse the string into a json object
                JSONParser parse = new JSONParser();
                data_obj = (JSONArray) parse.parse(responsee.toString());
            SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm",new Locale("ru", "RU"));
            SimpleDateFormat df2 = new SimpleDateFormat("dd MMMM yyyy HH:mm",new Locale("ru", "RU"));
                for(Object i:data_obj){
                    JSONObject task = new JSONObject();
                    JSONArray performers=new JSONArray();
                    for(Object o:((JSONArray)((JSONObject) i).get("performs"))){
                        JSONObject performer=new JSONObject();
                        performer.put("Email",((JSONObject)((JSONObject)o).get("user")).get("email"));
                        performer.put("Имя",((JSONObject)((JSONObject)o).get("user")).get("firstName"));
                        performer.put("Фамилия",((JSONObject)((JSONObject)o).get("user")).get("secondName"));
                        performer.put("Отчество",((JSONObject)((JSONObject)o).get("user")).get("middleName"));
                        if(Objects.equals(((JSONObject) o).get("status").toString(), "Completed")){
                            performer.put("Статус выполнения","Выполнено");
                        }
                        if(Objects.equals(((JSONObject) o).get("status").toString(), "Waiting")){
                            performer.put("Статус выполнения","Не просмотренно");
                        }
                        if(Objects.equals(((JSONObject) o).get("status").toString(), "InProgress")){
                            performer.put("Статус выполнения","Выполняется");
                        }
                        if(((JSONObject)o).get("statusChanged")!=null)
                            performer.put("Дата изменения статуса",df2.format(df1.parse(((JSONObject)o).get("statusChanged").toString().replace('T',' '))));
                        else
                            performer.put("Дата изменения статуса",null);

                        performer.put("Комментарий",((JSONObject)o).get("comment"));
                        performers.add(performer);
                    }
                    task.put("Исполнители", performers);

                    JSONObject author = new JSONObject();
                    author.put("Email",((JSONObject)((JSONObject)i).get("author")).get("email"));
                    author.put("Имя",((JSONObject)((JSONObject)i).get("author")).get("firstName"));
                    author.put("Фамилия",((JSONObject)((JSONObject)i).get("author")).get("secondName"));
                    author.put("Отчество",((JSONObject)((JSONObject)i).get("author")).get("middleName"));
                    task.put("Автор",author);

                    JSONObject obaji = new JSONObject();

                    obaji.put("Срок сдачи",df2.format(df1.parse(((JSONObject)i).get("deadline").toString().replace('T',' '))));
                    obaji.put("Дата создания",df2.format(df1.parse(((JSONObject)i).get("created").toString().replace('T',' '))));
                    obaji.put("Описание",((JSONObject)i).get("desc"));
                    obaji.put("Название",((JSONObject)i).get("title"));
                    task.put("Информация о задании",obaji);

                    realdata.add(task);

                }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Workbook workbook = new Workbook();
        Worksheet worksheet = workbook.getWorksheets().get(0);

        CellsFactory factory = new CellsFactory();
        Style style = factory.createStyle();
        style.setHorizontalAlignment(TextAlignmentType.CENTER);
        style.getFont().setColor(Color.getDarkGreen());
        style.getFont().setBold(true);

// Set JsonLayoutOptions
        JsonLayoutOptions options = new JsonLayoutOptions();
        options.setTitleStyle(style);
        options.setArrayAsTable(true);

// Export JSON Data
        JsonUtility.importData(realdata.toJSONString(), worksheet.getCells(), 0, 0, options);

// Save Excel file
        DateFormat dateFormatter = new SimpleDateFormat("dd MM yyyy HH-mm-ss", new Locale("ru", "RU"));
        String currentDateTime = dateFormatter.format(new Date());


        Transliterator toLatinTrans = Transliterator.getInstance(CYRILLIC_TO_LATIN);
        String result = toLatinTrans.transliterate(name);

        String fileName="Zadaniya ot "+result+" "+ currentDateTime + ".xlsx";

        ByteArrayOutputStream ms=new ByteArrayOutputStream();
        workbook.save(ms, FileFormatType.XLSX);
        byte[] b = ms.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
        ResponseEntity<byte[]> response1 = new ResponseEntity<>(b, headers, HttpStatus.OK);

        return response1;

    }



    @GetMapping("/addtask")
    public String addtask(Model model){
        if (Objects.equals(maincontroller.cocok, "")){
            return "redirect:/success/";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Object prince = authentication.getPrincipal();
            String pfp = ((DefaultOidcUser) prince).getPicture();
            model.addAttribute("pfp",pfp);
            String username=((DefaultOidcUser) prince).getGivenName()+" "+((DefaultOidcUser) prince).getFamilyName();
            model.addAttribute("username",username);
        }
        String id="";
        try {
            if (!(authentication instanceof AnonymousAuthenticationToken)) {
                Object prince = authentication.getPrincipal();
                id=((DefaultOidcUser) prince).getName();
            }
            URL url = new URL("https://"+testlink+"/users");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setUseCaches(true);

            conn.setRequestProperty("Cookie",
                    maincontroller.cocok);

            conn.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(),"utf-8"));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
                JSONParser parse = new JSONParser();
                JSONArray data_obj = (JSONArray) parse.parse(response.toString());
                Iterator iter=data_obj.iterator();
                //List<String> namez=new ArrayList<String>();
                Map<String, String>namez=new HashMap<String,String>();
                //List<String> idz=new ArrayList<String>();

            String namee="";
            Object prince = authentication.getPrincipal();
            String userid=((DefaultOidcUser) prince).getName();

            while(iter.hasNext()) {
                        namee = "";
                        JSONObject element = (JSONObject) iter.next();
                    if (!Objects.equals(element.get("id").toString(), userid)){
                        if (element.get("firstName") != null)
                            namee += element.get("firstName").toString() + " ";
                        if (element.get("secondName") != null)
                            namee += element.get("secondName").toString() + " ";
                        if (element.get("middleName") != null)
                            namee += element.get("middleName").toString();
                        namez.put(element.get("id").toString(), namee);
                    }

                }

                model.addAttribute("namez",namez);
            model.addAttribute("keyz",namez.keySet());


            SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm",new Locale("ru", "RU"));
                model.addAttribute("df1",df1);
                SimpleDateFormat df2 = new SimpleDateFormat("dd MMMM yyyy HH:mm",new Locale("ru", "RU"));
                model.addAttribute("df2",df2);
                model.addAttribute("task",data_obj);



        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("my",0);



        return "tasks/addtask";
    }

    @PostMapping("/addtask")
    public String savetask(
                           @RequestParam(value="name",required = false) String name,
                           @RequestParam(value="details",required = false) String details,
                           @RequestParam(value="until",required = false) String until,
                           @RequestParam(value="doer",required = false) String doer, Model model
                           ) throws IOException, ParseException, java.text.ParseException {
        if (Objects.equals(maincontroller.cocok, "")){
            return "redirect:/success/";
        }
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm",new Locale("ru", "RU"));
        Date now = df1.parse(LocalDateTime.now().toString().replace("T"," "));
try {
    if(name==null||until==null||doer==null||name.isEmpty()||until.isEmpty()||doer.isEmpty()||!df1.parse(until.replace('T',' ')).after(now)) {

        if (name==null||name.isEmpty()) {

            model.addAttribute("error1", "Заполните название задания");
        }

        if (until==null||until.isEmpty()) {
            model.addAttribute("error3", "Заполните срок сдачи");
        }
        else if (!df1.parse(until.replace('T',' ')).after(now)) {
            model.addAttribute("error3", "Выберите дату и время, которые находятся в будущем");
        }
        if (doer==null||doer.isEmpty()) {
            model.addAttribute("error4", "Выберите исполнителей");
        }
        String id="";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Object prince = authentication.getPrincipal();
            String pfp = ((DefaultOidcUser) prince).getPicture();
            model.addAttribute("pfp",pfp);
            String username=((DefaultOidcUser) prince).getGivenName()+" "+((DefaultOidcUser) prince).getFamilyName();
            model.addAttribute("username",username);
        }
            if (!(authentication instanceof AnonymousAuthenticationToken)) {
                Object prince = authentication.getPrincipal();
                id=((DefaultOidcUser) prince).getName();
            }
            URL url = new URL("https://"+testlink+"/users");

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
            JSONArray data_obj = (JSONArray) parse.parse(response.toString());
            Iterator iter=data_obj.iterator();
            //List<String> namez=new ArrayList<String>();
            Map<String, String>namez=new HashMap<String,String>();
            //List<String> idz=new ArrayList<String>();

            String namee="";
            Object prince = authentication.getPrincipal();
            String userid=((DefaultOidcUser) prince).getName();

            while(iter.hasNext()) {
                namee = "";
                JSONObject element = (JSONObject) iter.next();
                    if (!Objects.equals(element.get("id").toString(), userid)){
                if (element.get("firstName") != null)
                    namee += element.get("firstName").toString() + " ";
                if (element.get("secondName") != null)
                    namee += element.get("secondName").toString() + " ";
                if (element.get("middleName") != null)
                    namee += element.get("middleName").toString();
                namez.put(element.get("id").toString(), namee);
                    }

            }

            model.addAttribute("namez",namez);
            model.addAttribute("keyz",namez.keySet());


            model.addAttribute("df1",df1);
            SimpleDateFormat df2 = new SimpleDateFormat("dd MMMM yyyy HH:mm",new Locale("ru", "RU"));
            model.addAttribute("df2",df2);
            model.addAttribute("task",data_obj);
        model.addAttribute("my",0);
        model.addAttribute("nomus",name);
        model.addAttribute("detoilus",details);
        model.addAttribute("untilus",until);

        return "tasks/addtask";

    }
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String idd = "";
    if (!(authentication instanceof AnonymousAuthenticationToken)) {
        Object prince = authentication.getPrincipal();
        idd = ((DefaultOidcUser) prince).getName();
    }

    String id = "";
    JSONArray arina = new JSONArray();
//    try {
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Object prince = authentication.getPrincipal();
            id = ((DefaultOidcUser) prince).getName();
        }
        URL url1 = new URL("https://"+testlink+"/users");

        HttpURLConnection conn1 = (HttpURLConnection) url1.openConnection();
        conn1.setRequestMethod("GET");
    conn1.setUseCaches(true);

    conn1.setRequestProperty("Cookie",
            maincontroller.cocok);

    conn1.connect();
    BufferedReader in = new BufferedReader(new InputStreamReader(
            conn1.getInputStream(),"utf-8"));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
    }
    in.close();

        //Using the JSON simple library parse the string into a json object
        JSONParser parse = new JSONParser();
        JSONArray data_obj = (JSONArray) parse.parse(response.toString());


        String[] doerz = doer.replace("&", "").split("doer=");
        List<String> doerarray = Arrays.asList(Arrays.toString(doerz).replace("[", "").replace("]", "").split("\\s*,\\s*"));


        for (Object o : data_obj) {
            JSONObject element = (JSONObject) o;
            String idee = element.get("id").toString();
            for (String i : doerarray) {

                if (idee.equals(i)) {
                    arina.add(element);
                }
            }
        }
        System.out.println(doerarray);
        System.out.println(arina);


        URL url = new URL("https://"+testlink+"/task");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
    conn.setUseCaches(true);

    conn.setRequestProperty("Cookie",
            maincontroller.cocok);


        Object prince = authentication.getPrincipal();
        StringBuilder jsonInputString = new StringBuilder("{\"task\":{\n" +
                "        \"title\":\"" + name + "\" ,\n" +
                "        \"desc\": \"" + details + "\",\n" +
                "        \"deadline\": \"" + until + "\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"" + ((DefaultOidcUser) prince).getName() + "\",\n" +
                "            \"firstName\": \"" + ((DefaultOidcUser) prince).getGivenName() + "\",\n" +
                "            \"secondName\": \"" + ((DefaultOidcUser) prince).getFamilyName() + "\",\n" +
                "            \"middleName\": \"" + ((DefaultOidcUser) prince).getMiddleName() + "\",\n" +
                "            \"email\": \"" + ((DefaultOidcUser) prince).getEmail() + "\",\n" +
                "            \"role\": null\n" +
                "        },\n" +
                "        \"performs\":" +
                " [\n");

        Iterator itorik = arina.iterator();
        while (itorik.hasNext()) {
            jsonInputString.append("{" +
                    "\"user\":");
            jsonInputString.append(itorik.next());
            jsonInputString.append("}");
            if (itorik.hasNext())
                jsonInputString.append(",\n");
        }
        jsonInputString.append("        ],\n" +
                "        \"documents\":[]\n" +
 //               "        \"files\":[]\n" +

                "    },"+
                "\"files\":[]}\n"

        );

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            StringBuilder response2 = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response2.append(responseLine.trim());
            }
        }


}
    catch (Exception e){
        model.addAttribute("error",e.getMessage());
        if (Objects.equals(maincontroller.cocok, "")){
            return "redirect:/success/";
        }
        String id="";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Object prince = authentication.getPrincipal();
            String pfp = ((DefaultOidcUser) prince).getPicture();
            model.addAttribute("pfp",pfp);
            String username=((DefaultOidcUser) prince).getGivenName()+" "+((DefaultOidcUser) prince).getFamilyName();
            model.addAttribute("username",username);
        }
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Object prince = authentication.getPrincipal();
            id=((DefaultOidcUser) prince).getName();
        }
        URL url = new URL("https://"+testlink+"/users");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setUseCaches(true);

        conn.setRequestProperty("Cookie",
                maincontroller.cocok);

        conn.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                conn.getInputStream(),"utf-8"));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JSONParser parse = new JSONParser();
        JSONArray data_obj = (JSONArray) parse.parse(response.toString());
        Iterator iter=data_obj.iterator();
        //List<String> namez=new ArrayList<String>();
        Map<String, String>namez=new HashMap<String,String>();
        //List<String> idz=new ArrayList<String>();

        String namee="";
        Object prince = authentication.getPrincipal();
        String userid=((DefaultOidcUser) prince).getName();

        while(iter.hasNext()) {
            namee = "";
            JSONObject element = (JSONObject) iter.next();
            if (!Objects.equals(element.get("id").toString(), userid)){
                if (element.get("firstName") != null)
                    namee += element.get("firstName").toString() + " ";
                if (element.get("secondName") != null)
                    namee += element.get("secondName").toString() + " ";
                if (element.get("middleName") != null)
                    namee += element.get("middleName").toString();
                namez.put(element.get("id").toString(), namee);
            }

        }

        model.addAttribute("namez",namez);
        model.addAttribute("keyz",namez.keySet());


        model.addAttribute("df1",df1);
        SimpleDateFormat df2 = new SimpleDateFormat("dd MMMM yyyy HH:mm",new Locale("ru", "RU"));
        model.addAttribute("df2",df2);
        model.addAttribute("task",data_obj);
        model.addAttribute("my",0);



        return "tasks/addtask";

    }
        model.addAttribute("my",0);



        return "redirect:/tasks/";
}

    @GetMapping("/{id}")
    public String viewtask(@PathVariable("id") Long id, Model model) throws IOException, ParseException, java.text.ParseException {
        if (Objects.equals(maincontroller.cocok, "")){
            return "redirect:/success/";
        }
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm",new Locale("ru", "RU"));
        model.addAttribute("df1",df1);
        SimpleDateFormat df2 = new SimpleDateFormat("dd MMMM yyyy HH:mm",new Locale("ru", "RU"));
        model.addAttribute("df2",df2);
        Date now = df1.parse(LocalDateTime.now().toString().replace("T"," "));
        model.addAttribute("now",now);
        FilenameUtils futil = new FilenameUtils();
        model.addAttribute("futil",futil);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String idd="";
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Object prince = authentication.getPrincipal();
            idd=((DefaultOidcUser) prince).getName();
            String pfp = ((DefaultOidcUser) prince).getPicture();
            model.addAttribute("pfp",pfp);
            String username=((DefaultOidcUser) prince).getGivenName()+" "+((DefaultOidcUser) prince).getFamilyName();
            model.addAttribute("username",username);
        }
        try {

            URL url = new URL("https://"+testlink+"/user/tasks/given");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setUseCaches(true);

            conn.setRequestProperty("Cookie",maincontroller.cocok);

            conn.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(),"utf-8"));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
                JSONParser parse = new JSONParser();
                JSONArray data_obj = (JSONArray) parse.parse(response.toString());

                try {

                    if(((JSONObject) data_obj.get(0)).get("id") == id){
                        model.addAttribute("tsk", data_obj.get(0));
                    }
                    else {
                        for (int index = 0; ((JSONObject) data_obj.get(index)).get("id") != id; index++) {
                            if ( ((Long)((JSONObject) data_obj.get(index + 1)).get("id")).intValue() == id.intValue() ) {
                                model.addAttribute("tsk", data_obj.get(index + 1));
                                break;
                            }
                        }
                    }

        //model.addAttribute("tsk", data_obj.get(3));

                }
                catch (Exception e){
                    model.addAttribute("my",0);



                    return "redirect:/tasks/";
                }


        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("my",0);



        return "tasks/taskdetail";

    }


    @PostMapping("/{id}/del")
    public String deltask(@PathVariable("id") Long id, Model model) throws IOException {
        if (Objects.equals(maincontroller.cocok, "")){
            return "redirect:/success/";
        }
        URL url = new URL("https://"+testlink+"/task/"+id.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setUseCaches(true);

        conn.setRequestProperty("Cookie",
                maincontroller.cocok);


        conn.connect();
        int responsecode = conn.getResponseCode();
        if (responsecode != 200) {
            throw new RuntimeException("HttpResponseCode: " + responsecode);
        }
        model.addAttribute("my",0);

        return ("redirect:/tasks/");
    }
    @PostMapping(value="/{id}/download",produces = { "application/octet-stream" })
    public ResponseEntity<byte[]> download(@PathVariable("id") Long id,@RequestParam(value = "fileid", required = false) Long fileid) throws IOException, ParseException, ServletException {

        URL url = new URL("https://"+testlink+"/document/"+fileid.toString()+"/file");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setUseCaches(true);

        conn.setRequestProperty("Cookie",
                maincontroller.cocok);

        conn.connect();
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
        JSONArray bytejson=new JSONArray();
        bytejson=(JSONArray) data_obj.get("file");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for(Object o:bytejson){
            byteArrayOutputStream.write(((Long)o).byteValue());
        }
        byte[] myByteArray=byteArrayOutputStream.toByteArray();
        String fileName=data_obj.get("name").toString();
        String e= data_obj.get("extension").toString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        Transliterator toLatinTrans = Transliterator.getInstance(CYRILLIC_TO_LATIN);
        String result = toLatinTrans.transliterate(fileName);
        headers.setContentDisposition(ContentDisposition.attachment().filename(result+'.'+e).build());

        return new ResponseEntity<>(myByteArray, headers, HttpStatus.OK);
    }



}
