package com.formulamanager.sokker.acciones.chatbot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import com.formulamanager.sokker.auxiliares.JSONUtil;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

@WebServlet("/dalle")
public class DalleServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private static String OPENAI_KEY = "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";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String question = request.getParameter("texto");

        // Obtener el InputStream de la imagen
        InputStream inputStream = request.getInputStream();

        // Leer el InputStream y almacenarlo en un arreglo de bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        byte[] imagenBytes = outputStream.toByteArray();
        
		
        // Send question to OpenAI API
        try {
	        String answer = sendQuestion(question, imagenBytes);
	
	        response.setContentType("text/plain");
	        response.getWriter().write(answer);
        } catch (Exception e) {
        	response.setContentType("application/json");
        	response.setCharacterEncoding("UTF-8");
	        response.getWriter().write(e.getMessage());
	        response.setStatus(500);
        }
    }

    private String sendQuestion(String question, byte[] imagenBytes) {
        String url = "https://api.openai.com/v1/images/edits";

        try (final WebClient webClient = new WebClient()) {
            // Set OpenAI API credentials
            webClient.addRequestHeader("Authorization", "Bearer " + OPENAI_KEY);

//            String requestBody="{\"image\":\"" + Base64.getEncoder().encodeToString(imagenBytes) + "\",\"prompt\":\"" + StringEscapeUtils.escapeJson(question) + "\"}";
//System.out.println(requestBody);
            WebRequest requestSettings = new WebRequest(new URL(url), HttpMethod.POST);
    		requestSettings.setAdditionalHeader("Content-Type", "multipart/form-data");
//    		requestSettings.setRequestBody(requestBody);
    		requestSettings.setCharset(StandardCharsets.UTF_8.name());
    		
    		List<NameValuePair> parts = new ArrayList<NameValuePair>();
    		parts.add(new NameValuePair("prompt", question));
    		parts.add(new NameValuePair("image", Base64.getEncoder().encodeToString(imagenBytes)));
    		requestSettings.setRequestParameters(parts);

            // Send request to OpenAI API
    		Page page = webClient.getPage(requestSettings);

            // Get response from OpenAI API
            String responseText = page.getWebResponse().getContentAsString();
            Object json = Configuration.defaultConfiguration().jsonProvider().parse(responseText);
            List<LinkedHashMap<String, Object>> data = JsonPath.read(json, "$.data");
            String answer = JSONUtil.getString(data.get(0), "url");
System.out.println(answer);
            return answer;
        } catch (IOException e) {
            e.printStackTrace();
            return "Sorry, I couldn't understand your question.";
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher("/jsp/chatbot/dalle.jsp").forward(request, response);
    }
}
