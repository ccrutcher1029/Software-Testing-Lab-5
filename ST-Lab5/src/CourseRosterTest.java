import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class CourseRosterTest {
    private HttpClient client;
    private final String baseUrl = "http://localhost:8080";

    @BeforeEach
    void setup() {
        client = HttpClient.newHttpClient();
    }

    @Test
    void cannotAddStudentWhenCourseIsFull() throws IOException, InterruptedException {
        int student1 = createStudent("Student One", "CS", 3.0);
        int student2 = createStudent("Student Two", "Math", 3.1);
        int student3 = createStudent("Student Three", "Bio", 3.2);

        int courseId = createCourse("Software Testing", 2, "R101", "Mr. Baarsch");

        HttpResponse<String> add1 = addStudentToCourse(courseId, student1);
        assertTrue(add1.statusCode() == 200 || add1.statusCode() == 201);

        HttpResponse<String> add2 = addStudentToCourse(courseId, student2);
        assertTrue(add2.statusCode() == 200 || add2.statusCode() == 201);

        HttpResponse<String> add3 = addStudentToCourse(courseId, student3);
        assertEquals(400, add3.statusCode());
        assertTrue(add3.body().toLowerCase().contains("full"));
    }

    private int createStudent(String name, String major, double gpa) throws IOException, InterruptedException {
        String json = """
            {
              "name": "%s",
              "major": "%s",
              "gpa": %s
            }
            """.formatted(name, major, gpa);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/students"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        return extractId(response.body());
    }

    private int createCourse(String name, int size, String room, String instructor) throws IOException, InterruptedException {
        String json = """
            {
              "name": "%s",
              "size": %d,
              "room": "%s",
              "instructor": "%s",
              "roster": []
            }
            """.formatted(name, size, room, instructor);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/courses"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        return extractId(response.body());
    }

    private HttpResponse<String> addStudentToCourse(int courseId, int studentId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/courses/" + courseId + "/students/" + studentId))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
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

::contentReference[oaicite:2]{index=2}
        }
        return Integer.parseInt(json.substring(start, end));
    }
}
