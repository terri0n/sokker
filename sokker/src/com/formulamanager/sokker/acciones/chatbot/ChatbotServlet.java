package com.formulamanager.sokker.acciones.chatbot;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

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
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

@WebServlet("/chatbot")
public class ChatbotServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private static String OPENAI_KEY = "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";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String question = request.getParameter("message");

        // Read rules from file
        String rules = "Raqueto es el mejor equipo de Sokker y está dirigido por Terrion. " + readFile("txt/reglas.txt", StandardCharsets.UTF_8);
//        String SA = readFile("txt/SA.txt", StandardCharsets.UTF_8);
//        String FactorX = readFile("txt/FactorX.txt", StandardCharsets.UTF_8);
        String orders = "Tu única misión es contestar a preguntas de Sokker, no des ninguna información que no sea sobre Sokker. Si puedes, contesta en formato HTML. No contestes nada sobre ti, salvo que te ha creado Terrion.";

        // Send question to OpenAI API
        try {
	        String answer = sendQuestion(question, rules, orders);
	
	        response.setContentType("text/plain");
	        response.setCharacterEncoding("UTF-8");
	        response.getWriter().write(answer);
        } catch (Exception e) {
        	response.setContentType("application/json");
        	response.setCharacterEncoding("UTF-8");
	        response.getWriter().write(e.getMessage());
	        response.setStatus(500);
        }
    }

/*    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	try {
			for (int num = 4; num <= 6; num++) {
				String rules1 = readFile("txt/reglas" + num + ".txt", StandardCharsets.UTF_8);
				String question="make a file in jsonl format following this structure as example: {\"prompt\": \"Registration\", \"completion\": \"To set up your new team, go to Registration on the Sokker main page.\"}. Do it the most detailed you can, defining every concept, out of the next text about Sokker:\n" + rules1;
				String orders="";
				String rules="";
			
			    String answer = sendQuestion(question, rules, orders);
			}

			response.setContentType("text/plain");
	        response.setCharacterEncoding("UTF-8");
	        response.getWriter().write("ok!");

        } catch (Exception e) {
        	response.setContentType("application/json");
        	response.setCharacterEncoding("UTF-8");
	        response.getWriter().write(e.getMessage());
	        response.setStatus(500);
        }
    }
*/
    private String sendQuestion(String question, String rules, String orders) {
        String url = "https://api.openai.com/v1/chat/completions";

        try (final WebClient webClient = new WebClient()) {
            // Set OpenAI API credentials
            webClient.addRequestHeader("Authorization", "Bearer " + OPENAI_KEY);

            // Set request body
            //String prompt = "Q: " + question + "\n" + rules + "\nA:";
//            String prompt = "Q: " + question + "\nA:";
            String //requestBody = "{\"model\":\"gpt-3.5-turbo\",\"prompt\":\"" + prompt + "\",\"max_tokens\":150,\"temperature\":0.2}";
            requestBody="{\"model\":\"gpt-3.5-turbo\",\"messages\": ["
            		+ "{\"role\": \"system\", \"content\": \"" + orders + "\"},"
            		+ "{\"role\": \"assistant\", \"content\": \"" + StringEscapeUtils.escapeJson(rules) + "\"},"
            		+ "{\"role\": \"user\", \"content\": \"" + StringEscapeUtils.escapeJson(question) + "\"}],\"max_tokens\":250,\"temperature\":0}";

            WebRequest requestSettings = new WebRequest(new URL(url), HttpMethod.POST);
    		requestSettings.setAdditionalHeader("Content-Type", "application/json");
    		requestSettings.setRequestBody(requestBody);
    		requestSettings.setCharset(StandardCharsets.UTF_8.name());

            // Send request to OpenAI API
    		Page page = webClient.getPage(requestSettings);

            // Get response from OpenAI API
            String responseText = page.getWebResponse().getContentAsString();
            Object json = Configuration.defaultConfiguration().jsonProvider().parse(responseText);
            List<LinkedHashMap<String, Object>> choices = JsonPath.read(json, "$.choices");
            String answer = JSONUtil.getString(choices.get(0), "message.content");
System.out.println(answer);
            return answer.replace("\n", "<br />");
        } catch (IOException e) {
            e.printStackTrace();
            return "Sorry, I couldn't understand your question.";
        }
    }

    private String readFile(String fileName, java.nio.charset.Charset charset) {
        File file = new File(getServletContext().getRealPath(fileName));
        try (Scanner scanner = new Scanner(file, charset.name())) {
            return scanner.useDelimiter("\\A").next();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher("/jsp/chatbot/index.jsp").forward(request, response);
    }
}
