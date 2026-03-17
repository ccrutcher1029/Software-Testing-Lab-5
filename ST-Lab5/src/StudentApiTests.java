import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class StudentApiTests {
    private HttpClient client;
    private final String baseUrl = "http://localhost:8080";

    @BeforeEach
    void setup() {
        client = HttpClient.newHttpClient();
    }

    @Test
    void createValidStudent() throws IOException, InterruptedException {
        String json = """
            {
              "name": "Jane Doe",
              "major": "Computer Science",
              "gpa": 3.5
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("\"name\":\"Jane Doe\""));
        assertTrue(response.body().contains("\"major\":\"Computer Science\""));
        assertTrue(response.body().contains("\"gpa\":3.5"));
    }

    @Test
    void rejectStudentWithGpaAboveMax() throws IOException, InterruptedException {
        String json = """
            {
              "name": "Bob Ross",
              "major": "Math",
              "gpa": 4.1
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    void acceptStudentWithGpaAtBoundaryZero() throws IOException, InterruptedException {
        String json = """
            {
              "name": "John Doe",
              "major": "Biology",
              "gpa": 0.0
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("\"gpa\":0.0")
                || response.body().contains("\"gpa\":0"));
    }

    @Test
    void rejectStudentWithNameTooLong() throws IOException, InterruptedException {
        String longName = "A".repeat(256);

        String json = """
            {
              "name": "%s",
              "major": "History",
              "gpa": 3.0
            }
            """.formatted(longName);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }
}
