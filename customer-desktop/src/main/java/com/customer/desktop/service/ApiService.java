package com.customer.desktop.service;

import com.customer.desktop.model.Customer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class ApiService {

    private final String baseUrl;
    private String apiToken;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getApiToken() {
        return apiToken;
    }

    public String login(String username, String password) throws ApiException {
        String url = baseUrl + "/auth/login";
        var map = new LinkedHashMap<String, String>();
        map.put("username", username);
        map.put("password", password);
        String json = toJsonString(map);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String token = root.get("token").asText();
                this.apiToken = token;
                return token;
            } else {
                throw new ApiException(extractMessage(response.body()),
                        response.statusCode(), response.body());
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Unable to connect to the API server: " + e.getMessage(), 0, "");
        }
    }

    public void register(String username, String password) throws ApiException {
        String url = baseUrl + "/auth/register";
        var map = new LinkedHashMap<String, String>();
        map.put("username", username);
        map.put("password", password);
        String json = toJsonString(map);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201) {
                throw new ApiException(extractMessage(response.body()),
                        response.statusCode(), response.body());
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Unable to connect to the API server: " + e.getMessage(), 0, "");
        }
    }

    public void logout() throws ApiException {
        if (apiToken == null || apiToken.isEmpty()) return;

        String url = baseUrl + "/auth/logout";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiToken)
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new ApiException("Logout failed: " + e.getMessage(), 0, "");
        } finally {
            this.apiToken = null;
        }
    }

    public List<Customer> getAllCustomers() throws ApiException {
        return getAllCustomers(0, 100);
    }

    public List<Customer> getAllCustomers(int page, int size) throws ApiException {
        String url = baseUrl + "/customers?page=" + page + "&size=" + size;
        HttpRequest request = buildGetRequest(url);

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode content = root.get("content");
                if (content != null && content.isArray()) {
                    return objectMapper.convertValue(content,
                            new TypeReference<List<Customer>>() {});
                }
                return Collections.emptyList();
            } else {
                throw new ApiException("Failed to fetch customers",
                        response.statusCode(), response.body());
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Unable to connect to the API server: " + e.getMessage(), 0, "");
        }
    }

    public List<Customer> searchCustomers(String query) throws ApiException {
        String url = baseUrl + "/customers?search=" + encodeParam(query) + "&size=100";
        HttpRequest request = buildGetRequest(url);

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode content = root.get("content");
                if (content != null && content.isArray()) {
                    return objectMapper.convertValue(content,
                            new TypeReference<List<Customer>>() {});
                }
                return Collections.emptyList();
            } else {
                throw new ApiException("Search failed",
                        response.statusCode(), response.body());
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Unable to connect to the API server: " + e.getMessage(), 0, "");
        }
    }

    public Customer getCustomerById(Long id) throws ApiException {
        String url = baseUrl + "/customers/" + id;
        HttpRequest request = buildGetRequest(url);

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), Customer.class);
            } else if (response.statusCode() == 404) {
                throw new ApiException("Customer not found", 404, response.body());
            } else {
                throw new ApiException("Failed to fetch customer",
                        response.statusCode(), response.body());
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Unable to connect to the API server: " + e.getMessage(), 0, "");
        }
    }

    public Customer createCustomer(Customer customer) throws ApiException {
        String url = baseUrl + "/customers";
        String json = toJson(customer);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiToken)
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                return objectMapper.readValue(response.body(), Customer.class);
            } else if (response.statusCode() == 400) {
                throw new ApiException("Validation error: " + extractMessage(response.body()),
                        400, response.body());
            } else {
                throw new ApiException("Failed to create customer",
                        response.statusCode(), response.body());
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Unable to connect to the API server: " + e.getMessage(), 0, "");
        }
    }

    public Customer updateCustomer(Long id, Customer customer) throws ApiException {
        String url = baseUrl + "/customers/" + id;
        String json = toJson(customer);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiToken)
                .timeout(Duration.ofSeconds(10))
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), Customer.class);
            } else if (response.statusCode() == 404) {
                throw new ApiException("Customer not found", 404, response.body());
            } else if (response.statusCode() == 400) {
                throw new ApiException("Validation error: " + extractMessage(response.body()),
                        400, response.body());
            } else {
                throw new ApiException("Failed to update customer",
                        response.statusCode(), response.body());
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Unable to connect to the API server: " + e.getMessage(), 0, "");
        }
    }

    public void deleteCustomer(Long id) throws ApiException {
        String url = baseUrl + "/customers/" + id;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiToken)
                .timeout(Duration.ofSeconds(10))
                .DELETE()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 204) {
                if (response.statusCode() == 404) {
                    throw new ApiException("Customer not found", 404, response.body());
                }
                throw new ApiException("Failed to delete customer",
                        response.statusCode(), response.body());
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Unable to connect to the API server: " + e.getMessage(), 0, "");
        }
    }

    private HttpRequest buildGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiToken)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
    }

    private String toJson(Customer customer) throws ApiException {
        try {
            var map = new LinkedHashMap<String, String>();
            map.put("name", customer.getName());
            map.put("email", customer.getEmail());
            map.put("phone", customer.getPhone());
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new ApiException("Failed to serialize customer data", 0, "");
        }
    }

    private String toJsonString(Object obj) throws ApiException {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new ApiException("Failed to serialize request data", 0, "");
        }
    }

    private String extractMessage(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.has("message")) {
                return root.get("message").asText();
            }
            if (root.has("fields")) {
                return root.get("fields").toString();
            }
            return responseBody;
        } catch (Exception e) {
            return responseBody;
        }
    }

    private String encodeParam(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    public static class ApiException extends Exception {
        private final int statusCode;
        private final String responseBody;

        public ApiException(String message, int statusCode, String responseBody) {
            super(message);
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getResponseBody() {
            return responseBody;
        }
    }
}
