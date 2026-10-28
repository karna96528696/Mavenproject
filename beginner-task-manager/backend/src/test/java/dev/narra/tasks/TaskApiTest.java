package dev.narra.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:task-tests;DB_CLOSE_DELAY=-1")
class TaskApiTest {
    @Value("${local.server.port}") int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;
    final HttpClient http = HttpClient.newHttpClient();

    @BeforeEach void clearDatabase() { jdbc.update("DELETE FROM tasks"); }

    HttpResponse<String> request(String method, String path, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/tasks" + path));
        if (body != null) builder.header("Content-Type", "application/json");
        return http.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test void startsWithAnEmptyList() throws Exception {
        var response = request("GET", "", null);
        assertEquals(200, response.statusCode());
        assertEquals(0, json.readTree(response.body()).size());
    }

    @Test void createsReadsUpdatesAndDeletesATask() throws Exception {
        var created = request("POST", "", "{\"title\":\" Learn Java \",\"description\":\" Practice daily \",\"completed\":false}");
        assertEquals(201, created.statusCode(), created.body());
        var task = json.readTree(created.body());
        String path = "/" + task.get("id").asText();
        assertEquals("Learn Java", task.get("title").asText());
        assertEquals("Practice daily", task.get("description").asText());
        assertEquals("/api/tasks" + path, created.headers().firstValue("Location").orElseThrow());
        assertEquals(200, request("GET", path, null).statusCode());
        var updated = request("PUT", path, "{\"title\":\"Learn REST\",\"description\":\"\",\"completed\":true}");
        assertEquals(200, updated.statusCode(), updated.body());
        assertTrue(json.readTree(updated.body()).get("completed").asBoolean());
        assertEquals(task.get("createdAt").asText(), json.readTree(updated.body()).get("createdAt").asText());
        assertEquals(1, json.readTree(request("GET", "", null).body()).size());
        assertEquals(204, request("DELETE", path, null).statusCode());
        assertEquals(404, request("GET", path, null).statusCode());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM tasks", Integer.class));
    }

    @Test void rejectsBlankAndOversizedTitlesWithoutWritingRows() throws Exception {
        for (String title : new String[] {"   ", "x".repeat(101)}) {
            var response = request("POST", "", json.writeValueAsString(new TaskRequest(title, "", false)));
            assertEquals(400, response.statusCode(), response.body());
            assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/problem+json"));
        }
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM tasks", Integer.class));
    }

    @Test void rejectsMissingRequiredFieldsAndOversizedDescriptions() throws Exception {
        for (String body : new String[] {"{}", "{\"title\":\"Java\",\"description\":\"\"}",
                json.writeValueAsString(new TaskRequest("Java", "x".repeat(1001), false))}) {
            assertEquals(400, request("POST", "", body).statusCode());
        }
    }

    @Test void missingUpdatesAndDeletesReturn404() throws Exception {
        String path = "/" + UUID.randomUUID();
        assertEquals(404, request("PUT", path, "{\"title\":\"Java\",\"description\":\"\",\"completed\":false}").statusCode());
        assertEquals(404, request("DELETE", path, null).statusCode());
    }

    @Test void malformedJsonAndIdentifiersReturn400() throws Exception {
        assertEquals(400, request("POST", "", "{bad-json").statusCode());
        assertEquals(400, request("GET", "/not-a-uuid", null).statusCode());
    }

    @Test void sqlPunctuationIsStoredAsText() throws Exception {
        String title = "Read 'SQL'; DROP TABLE tasks; --";
        var response = request("POST", "", json.writeValueAsString(new TaskRequest(title, "", false)));
        assertEquals(201, response.statusCode(), response.body());
        assertEquals(title, json.readTree(response.body()).get("title").asText());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM tasks", Integer.class));
    }
}
