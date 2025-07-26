package com.example.chatbot.controller;

import com.example.chatbot.model.ChatRequest;
import com.example.chatbot.model.ChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.*;

@Controller
public class ChatController {

    @Value("${cohere.api.key}")
    private String apiKey;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OidcUser user, Model model) {
        if (user == null) {
            return "redirect:/oauth2/authorization/auth0"; //  force redirect if not logged in
        }
        String username = user.getFullName() != null ? user.getFullName() : "User";
        model.addAttribute("username", username);
        return "chat";
    }

    @PostMapping("/api/chat")
    @ResponseBody
    public ChatResponse chat(@RequestBody ChatRequest request) {
        try {
            String prompt = request.getPrompt();
                    String jsonBody = """
        {
          "message": "%s",
          "model": "command-r",
          "temperature": %.1f,
          "max_tokens": %d
        }
        """.formatted(prompt, request.getTemperature(), request.getMaxTokens());

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.cohere.ai/v1/chat"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            String body = response.body();
            System.out.println("Cohere API Response: " + body);


            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(body);

            String answer = root.path("text").asText("No response text found.");

            return new ChatResponse(answer);

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt(); // Properly handle interruption
            return new ChatResponse("Error: Operation was interrupted.");
        } catch (Exception e) {
            return new ChatResponse("Error: " + e.getMessage());
        }

    }
}
