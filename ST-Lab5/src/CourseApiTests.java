import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class CourseApiTests {
    private HttpClient client;
    private final String baseUrl = "http://localhost:8080";

    @BeforeEach
    void setup() {
        client = HttpClient.newHttpClient();
    }

    @Test
    void createCourseSuccessfully() throws IOException, InterruptedException {
        String json = """
            {
              "name": "Software Testing",
              "size": 2,
              "room": "R101",
              "instructor": "Mr. Baarsch",
              "roster": []
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/courses"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("\"name\":\"Software Testing\""));
        assertTrue(response.body().contains("\"size\":2"));
    }

    @Test
    void retrieveCourseSuccessfully() throws IOException, InterruptedException {
        String createJson = """
            {
              "name": "Spell Casting",
              "size": 3,
              "room": "R102",
              "instructor": "Dr. Strange",
              "roster": []
            }
            """;

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/courses"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(createJson))
                .build();

        HttpResponse<String> createResponse =
                client.send(createRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, createResponse.statusCode());

        int courseId = extractId(createResponse.body());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/courses/" + courseId))
                .GET()
                .build();

        HttpResponse<String> getResponse =
                client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getResponse.statusCode());
        assertTrue(getResponse.body().contains("\"id\":" + courseId));
        assertTrue(getResponse.body().contains("\"name\":\"Spell Casting\""));
    }

    @Test
    void updateCourseRoomAndInstructor() throws IOException, InterruptedException {
        String createJson = """
            {
              "name": "Something IDK",
              "size": 2,
              "room": "R103",
              "instructor": "Dr. Doom",
              "roster": []
            }
            """;

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/courses"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(createJson))
                .build();

        HttpResponse<String> createResponse =
                client.send(createRequest, HttpResponse.BodyHandlers.ofString());

        int courseId = extractId(createResponse.body());

        String updateJson = """
            {
              "name": "Something IDK",
              "size": 2,
              "room": "R202",
              "instructor": "Dr. Boom",
              "roster": []
            }
            """;

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/courses/" + courseId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(updateJson))
                .build();

        HttpResponse<String> updateResponse =
                client.send(updateRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, updateResponse.statusCode());
        assertTrue(updateResponse.body().contains("\"room\":\"R202\""));
        assertTrue(updateResponse.body().contains("\"instructor\":\"Dr. Boom\""));
    }

    private int extractId(String json) {
        String marker = "\"id\":";
        int start = json.indexOf(marker);
        if (start == -1) {
            fail("No id found in response: " + json);
        }
        start += marker.length();

        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }

        return Integer.parseInt(json.substring(start, end));
    }
}
