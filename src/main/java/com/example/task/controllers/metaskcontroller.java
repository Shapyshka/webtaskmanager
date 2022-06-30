package com.example.task.controllers;


import com.aspose.cells.*;
import com.ibm.icu.text.Transliterator;
import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping(path="/metasks/")
@WebServlet(name = "FileUploadServlet", urlPatterns = { "/fileuploadservlet" })
@MultipartConfig(
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 100)
public class metaskcontroller extends HttpServlet {
    public static final String CYRILLIC_TO_LATIN = "Cyrillic-Latin";


    private final String testlink = "api.ok654.org";

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
            URL url = new URL("https://"+testlink+"/user/tasks/todo");

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
//            }

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
//        String fileName="Tasks";
        Transliterator toLatinTrans = Transliterator.getInstance(CYRILLIC_TO_LATIN);
        String result = toLatinTrans.transliterate(name);

        String fileName="Zadaniya dlya "+result+" "+ currentDateTime + ".xlsx";

        ByteArrayOutputStream ms=new ByteArrayOutputStream();
        workbook.save(ms, FileFormatType.XLSX);
        byte[] b = ms.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
        ResponseEntity<byte[]> response1 = new ResponseEntity<>(b, headers, HttpStatus.OK);

        return response1;
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
            URL url = new URL("https://"+testlink+"/user/tasks/todo");

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
                    JSONArray array=(JSONArray)element.get("performs");
                    for(Object el:array) {
                        if(Objects.equals(((JSONObject) ((JSONObject) el).get("user")).get("id").toString(), usid)) {
                            if (!(!df1.parse(element.get("deadline").toString().replace('T',' ')).after(now)&&!Objects.equals(((JSONObject) el).get("status").toString(), "Completed"))) {
                                iter.remove();
                            }
                        }
                    }
                }
                model.addAttribute("task",data_obj);
                model.addAttribute("my",1);

                return "metasks/tasklist";
            }
            else if(Objects.equals(fparam, "Due")){
                while(iter.hasNext()) {
                    JSONObject element = (JSONObject) iter.next();
                    JSONArray array=(JSONArray)element.get("performs");
                    for(Object el:array) {
                        if(Objects.equals(((JSONObject) ((JSONObject) el).get("user")).get("id").toString(), usid)) {
                            if (!df1.parse(element.get("deadline").toString().replace('T',' ')).after(now)&&!Objects.equals(((JSONObject) el).get("status").toString(), "Completed")) {
                                iter.remove();
                            }
                        }
                    }
                }

                model.addAttribute("task",data_obj);
                model.addAttribute("my",1);

                return "metasks/tasklist";
            }
            else {
                while (iter.hasNext()) {
                    JSONObject element = (JSONObject) iter.next();
                    JSONArray array = (JSONArray) element.get("performs");
                    for (Object el : array) {
                        if (Objects.equals(((JSONObject) ((JSONObject) el).get("user")).get("id").toString(), usid)) {
                            if (!Objects.equals(((JSONObject) el).get("status").toString(), fparam)) {
                                iter.remove();
                            }
                        }
                    }
                }

                model.addAttribute("task",data_obj);
                model.addAttribute("my",1);

                return "metasks/tasklist";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("my",1);

        return "metasks/tasklist";
    }

    @GetMapping(path="/")
    public String hometask(Model model){
        if (Objects.equals(maincontroller.cocok, "")){
            return "redirect:/success/";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Object prince = authentication.getPrincipal();
            String idd=((DefaultOidcUser) prince).getName();
            model.addAttribute("usid",idd);
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
            URL url = new URL("https://"+testlink+"/user/tasks/todo");

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

            model.addAttribute("task",data_obj);


        } catch (Exception e) {
            e.printStackTrace();

        }
        model.addAttribute("my",1);

        return "metasks/tasklist";
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

        String idd="";
        String idee="";
        Long idishnik=null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Object prince = authentication.getPrincipal();
            idd=((DefaultOidcUser) prince).getName();
            model.addAttribute("usid",idd);

            String pfp = ((DefaultOidcUser) prince).getPicture();
            model.addAttribute("pfp",pfp);

            String username=((DefaultOidcUser) prince).getGivenName()+" "+((DefaultOidcUser) prince).getFamilyName();
            model.addAttribute("username",username);
        }
        try {

            URL url = new URL("https://"+testlink+"/user/tasks/todo");

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
                JSONObject objee=new JSONObject();
                try {
                    if(((JSONObject) data_obj.get(0)).get("id") == id){
                        model.addAttribute("tsk", data_obj.get(0));
                        objee=(JSONObject) data_obj.get(0);
                    }
                    else {
                        for (int index = 0; ((JSONObject) data_obj.get(index)).get("id") != id; index++) {
                            if ( ((Long)((JSONObject) data_obj.get(index + 1)).get("id")).intValue() == id.intValue() ) {
                                model.addAttribute("tsk", data_obj.get(index + 1));
                                objee=(JSONObject) data_obj.get(index + 1);
                                break;
                            }
                        }
                    }
                }
                catch (Exception e){
                    model.addAttribute("my",1);

                    return "redirect:/metasks/";
                }
            JSONArray performers=(JSONArray) objee.get("performs");
            try {
                Object prince = authentication.getPrincipal();
                idee=((DefaultOidcUser) prince).getName();
                if(Objects.equals(((JSONObject) ((JSONObject) performers.get(0)).get("user")).get("id").toString(), idee)){
                    model.addAttribute("sts",((JSONObject) performers.get(0)).get("status").toString()) ;
                }
                else {
                    for (int index = 0; !Objects.equals(((JSONObject) ((JSONObject) performers.get(index)).get("user")).get("id").toString(), idee); index++) {
                        if (Objects.equals(((JSONObject) ((JSONObject) performers.get(index+1)).get("user")).get("id").toString(), idee)) {
                            model.addAttribute("sts", ((JSONObject) performers.get(index+1)).get("status").toString());
                        }
                    }
                }
            }

            catch (Exception e){
                model.addAttribute("my",1);

                return "redirect:/metasks/";
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("my",1);

        return "metasks/taskdetail";

    }


    @PostMapping("/{id}/comptask")
    public String editview(@RequestParam(value="comment",required = false) String comment,
                           @PathVariable("id") Long id, HttpServletRequest request1, HttpServletResponse response1, Model model) throws ServletException, IOException, ParseException {
        if (Objects.equals(maincontroller.cocok, "")){
            return "redirect:/success/";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String idee = "";
        Long idishnik = null;
        try {
            if (!(authentication instanceof AnonymousAuthenticationToken)) {
                Object prince = authentication.getPrincipal();
                idee = ((DefaultOidcUser) prince).getName();
            }
            URL url = new URL("https://"+testlink+"/user/tasks/todo");

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
            JSONObject objee = new JSONObject();
            try {
                if (((JSONObject) data_obj.get(0)).get("id") == id) {
                    objee = (JSONObject) data_obj.get(0);
                } else {
                    for (int index = 0; ((JSONObject) data_obj.get(index)).get("id") != id; index++) {
                        if (((Long) ((JSONObject) data_obj.get(index + 1)).get("id")).intValue() == id.intValue()) {
                            objee = (JSONObject) data_obj.get(index + 1);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                model.addAttribute("my",1);

                return "redirect:/metasks/";
            }


            JSONArray performers = (JSONArray) objee.get("performs");
            try {
                Object prince = authentication.getPrincipal();
                idee = ((DefaultOidcUser) prince).getName();
                //JSONObject oneuser=new JSONObject();
                if (Objects.equals(((JSONObject) ((JSONObject) performers.get(0)).get("user")).get("id").toString(), idee)) {
                    idishnik = (Long) ((JSONObject) performers.get(0)).get("id");
                } else {
                    for (int index = 0; !Objects.equals(((JSONObject) ((JSONObject) performers.get(index)).get("user")).get("id").toString(), idee); index++) {
                        if (Objects.equals(((JSONObject) ((JSONObject) performers.get(index + 1)).get("user")).get("id").toString(), idee)) {
                            idishnik = (Long) ((JSONObject) performers.get(index + 1)).get("id");
                        }
                    }
                }
            } catch (Exception e) {
                model.addAttribute("my",1);

                return "redirect:/metasks/";
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

            Part filePart = request1.getPart("file");


            byte[] bytes = filePart.getInputStream().readAllBytes();
            if (bytes.length != 0) {
                URL url = new URL("https://"+testlink+"/document/perform/" + idishnik.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setUseCaches(true);

                conn.setRequestProperty("Cookie",
                        maincontroller.cocok);


                Object prince = authentication.getPrincipal();
                String fileName = filePart.getSubmittedFileName();
                String ext = FilenameUtils.getExtension(fileName);
                float size = bytes.length/1024/1024;
                String truefilename = fileName.replace("."+ext,"");
                String jsonInputString2 = "{\"document\":{" +
                        "        \"author\": {\n" +
                        "            \"id\": \"" + ((DefaultOidcUser) prince).getName() + "\",\n" +
                        "            \"firstName\": \"" + ((DefaultOidcUser) prince).getGivenName() + "\",\n" +
                        "            \"secondName\": \"" + ((DefaultOidcUser) prince).getFamilyName() + "\",\n" +
                        "            \"middleName\": \"" + ((DefaultOidcUser) prince).getMiddleName() + "\",\n" +
                        "            \"email\": \"" + ((DefaultOidcUser) prince).getEmail() + "\",\n" +
                        "            \"role\": null\n" +
                        "        }," +
                        "\"title\": \"" + fileName + "\"" +
                        "}," +
                        "\"file\":{" +
                        "\"name\": \"" + truefilename+ "\",\n" +
                        "\"size\": " + size+ ",\n" +
                        "\"file\": " + Arrays.toString(bytes)+ ",\n" +
                        "\"extension\": \"" + ext+ "\" \n" +

                        "}}";
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString2.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }
            }


        URL url1 = new URL("https://"+testlink+"/task/"+idishnik.toString()+"/status");
        HttpURLConnection httpCon1 = (HttpURLConnection) url1.openConnection();

        httpCon1.setRequestMethod("PUT");
        httpCon1.setUseCaches(true);

        httpCon1.setRequestProperty("Cookie",
                maincontroller.cocok);


        httpCon1.setRequestProperty("Content-Type", "application/json; utf-8");
        httpCon1.setRequestProperty("Accept", "application/json");
        httpCon1.setDoOutput(true);
        String jsonInputString = "\"Completed\"";
        try(OutputStream os = httpCon1.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(httpCon1.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        }

        URL url = new URL("https://"+testlink+"/task/"+idishnik.toString()+"/comment");


        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setRequestMethod("PUT");
        httpCon.setUseCaches(true);

        httpCon.setRequestProperty("Cookie",
                maincontroller.cocok);


        httpCon.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        httpCon.setRequestProperty("Accept", "application/json; charset=utf-8");
        httpCon.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(httpCon.getOutputStream());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "utf-8"));
        writer.write(comment);
        writer.close();
        wr.close();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(httpCon.getInputStream(),"utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        }

        model.addAttribute("my",1);
        return "redirect:/metasks/{id}";
    }


    @PostMapping("/{id}/start")
    public String start(@PathVariable("id") Long id, Model model) throws IOException, ParseException {
        if (Objects.equals(maincontroller.cocok, "")){
            return "redirect:/success/";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String idee="";
        Long idishnik=null;
        try {
            if (!(authentication instanceof AnonymousAuthenticationToken)) {
                Object prince = authentication.getPrincipal();
                idee=((DefaultOidcUser) prince).getName();
            }
            URL url = new URL("https://"+testlink+"/user/tasks/todo");

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
            JSONObject objee= new JSONObject();
            try {
                if(((JSONObject) data_obj.get(0)).get("id") == id){
                    objee=(JSONObject)data_obj.get(0);
                }
                else {
                    for (int index = 0; ((JSONObject) data_obj.get(index)).get("id") != id; index++) {
                        if ( ((Long)((JSONObject) data_obj.get(index + 1)).get("id")).intValue() == id.intValue() ) {
                            objee=(JSONObject)data_obj.get(index + 1);
                            break;
                        }
                    }
                }
            }

            catch (Exception e){
                model.addAttribute("my",1);

                return "redirect:/metasks/";
            }
            JSONArray performers=(JSONArray) objee.get("performs");
            try {
                Object prince = authentication.getPrincipal();
                idee=((DefaultOidcUser) prince).getName();
                //JSONObject oneuser=new JSONObject();
                if(Objects.equals(((JSONObject) ((JSONObject) performers.get(0)).get("user")).get("id").toString(), idee)){
                    idishnik= (Long) ((JSONObject) performers.get(0)).get("id");
                }
                else {
                    for (int index = 0; !Objects.equals(((JSONObject) ((JSONObject) performers.get(index)).get("user")).get("id").toString(), idee); index++) {
                        if (Objects.equals(((JSONObject) ((JSONObject) performers.get(index+1)).get("user")).get("id").toString(), idee)) {
                            idishnik=(Long) ((JSONObject) performers.get(index+1)).get("id");
                        }
                    }
                }
            }

            catch (Exception e){
                model.addAttribute("my",1);

                return "redirect:/metasks/";
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        URL url = new URL("https://"+testlink+"/task/"+idishnik.toString()+"/status");
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();

        httpCon.setRequestMethod("PUT");
        httpCon.setRequestProperty("Content-Type", "application/json; utf-8");
        httpCon.setRequestProperty("Accept", "application/json");
        httpCon.setDoOutput(true);
        httpCon.setUseCaches(true);

        httpCon.setRequestProperty("Cookie",
                maincontroller.cocok);

        String jsonInputString = "\"InProgress\"";
        try(OutputStream os = httpCon.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(httpCon.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        }

        model.addAttribute("my",1);

        return "redirect:/metasks/{id}";
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
