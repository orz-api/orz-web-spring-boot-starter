package orz.springboot.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import lombok.SneakyThrows;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import orz.springboot.alarm.exception.OrzAlarmException;
import orz.springboot.alarm.exception.OrzUnexpectedException;
import orz.springboot.web.api.scope_v1.TestMutationV1Api;
import orz.springboot.web.api.scope_v1.TestQueryV1Api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AppTests {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @SneakyThrows
    @Test
    void testScopeV1TestQueryV1Api() {
        var url = "/ScopeV1/Test/QueryV1";
        var rspClass = TestQueryV1Api.TestQueryV1ApiRsp.class;

        // 0: OrzAlarmException
        {
            var req = new TestQueryV1Api.TestQueryV1ApiReq("0");
            var exception = ExceptionUtils.throwableOfType(
                    assertThrowsExactly(
                            ServletException.class,
                            () -> queryMockMvc(url, req)
                    ),
                    OrzAlarmException.class
            );
            assertEquals("test", exception.getEvent());

            var response = queryTestRestTemplate(url, req, rspClass);
            assertEquals(500, response.getStatusCode().value());
            assertFalse(response.getHeaders().containsKey("Orz-Version"));

            triggerTestInterceptor(true, url, req, rspClass);
        }

        // 1: OrzWebException
        {
            var req = new TestQueryV1Api.TestQueryV1ApiReq("1");
            queryMockMvc(url, req)
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.header().exists("Orz-Version"))
                    .andExpect(MockMvcResultMatchers.header().string("Orz-Code", "1"));

            var response = queryTestRestTemplate(url, req, rspClass);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getHeaders().containsKey("Orz-Version"));
            assertEquals("1", response.getHeaders().getFirst("Orz-Code"));

            triggerTestInterceptor(true, url, req, rspClass);
        }

        // 2: OrzWebException + OrzAlarmException
        {
            var req = new TestQueryV1Api.TestQueryV1ApiReq("2");
            queryMockMvc(url, req)
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.header().exists("Orz-Version"))
                    .andExpect(MockMvcResultMatchers.header().string("Orz-Code", "1"));

            var response = queryTestRestTemplate(url, req, rspClass);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getHeaders().containsKey("Orz-Version"));
            assertEquals("1", response.getHeaders().getFirst("Orz-Code"));

            triggerTestInterceptor(true, url, req, rspClass);
        }

        // 3: OrzUnexpectedException
        {
            var req = new TestQueryV1Api.TestQueryV1ApiReq("3");
            var exception = ExceptionUtils.throwableOfType(
                    assertThrowsExactly(
                            ServletException.class,
                            () -> queryMockMvc(url, req)
                    ),
                    OrzUnexpectedException.class
            );
            assertEquals("@ORZ_UNEXPECTED_ERROR", exception.getEvent());
            assertEquals("request error", exception.getSummary());

            var response = queryTestRestTemplate(url, req, rspClass);
            assertEquals(500, response.getStatusCode().value());
            assertFalse(response.getHeaders().containsKey("Orz-Version"));

            triggerTestInterceptor(true, url, req, rspClass);
        }

        // 4: OrzWebException not exists code
        {
            var req = new TestQueryV1Api.TestQueryV1ApiReq("4");
            queryMockMvc(url, req)
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.header().exists("Orz-Version"))
                    .andExpect(MockMvcResultMatchers.header().string("Orz-Code", "undefined"));

            var response = queryTestRestTemplate(url, req, rspClass);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getHeaders().containsKey("Orz-Version"));
            assertEquals("undefined", response.getHeaders().getFirst("Orz-Code"));

            triggerTestInterceptor(true, url, req, rspClass);
        }

        // 5: OrzWebException
        {
            var req = new TestQueryV1Api.TestQueryV1ApiReq("5");
            queryMockMvc(url, req)
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.header().exists("Orz-Version"))
                    .andExpect(MockMvcResultMatchers.header().string("Orz-Code", "2"));

            var response = queryTestRestTemplate(url, req, rspClass);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getHeaders().containsKey("Orz-Version"));
            assertEquals("2", response.getHeaders().getFirst("Orz-Code"));

            triggerTestInterceptor(true, url, req, rspClass);
        }

        // 6: Successful response
        {
            var req = new TestQueryV1Api.TestQueryV1ApiReq("6");
            queryMockMvc(url, req)
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.header().exists("Orz-Version"))
                    .andExpect(MockMvcResultMatchers.header().doesNotExist("Orz-Code"));

            var response = queryTestRestTemplate(url, req, rspClass);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getHeaders().containsKey("Orz-Version"));
            assertFalse(response.getHeaders().containsKey("Orz-Code"));
            assertNotNull(response.getBody());
            assertEquals(req.getTest(), response.getBody().getTest());

            triggerTestInterceptor(true, url, req, rspClass);
        }
    }

    @SneakyThrows
    @Test
    void testScopeV1TestMutationV1Api() {
        var url = "/ScopeV1/Test/MutationV1";
        var rspClass = TestMutationV1Api.TestMutationV1ApiRsp.class;

        // 0: OrzAlarmException
        {
            var req = new TestMutationV1Api.TestMutationV1ApiReq("0");
            var exception = ExceptionUtils.throwableOfType(
                    assertThrowsExactly(
                            ServletException.class,
                            () -> mutationMockMvc(url, req)
                    ),
                    OrzAlarmException.class
            );
            assertEquals("test", exception.getEvent());

            var response = mutationTestRestTemplate(url, req, rspClass);
            assertEquals(500, response.getStatusCode().value());
            assertFalse(response.getHeaders().containsKey("Orz-Version"));

            triggerTestInterceptor(false, url, req, rspClass);
        }

        // 1: OrzWebException
        {
            var req = new TestMutationV1Api.TestMutationV1ApiReq("1");
            mutationMockMvc(url, req)
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.header().exists("Orz-Version"))
                    .andExpect(MockMvcResultMatchers.header().string("Orz-Code", "1"));

            var response = mutationTestRestTemplate(url, req, rspClass);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getHeaders().containsKey("Orz-Version"));
            assertEquals("1", response.getHeaders().getFirst("Orz-Code"));

            triggerTestInterceptor(false, url, req, rspClass);
        }

        // 2: OrzWebException + OrzAlarmException
        {
            var req = new TestMutationV1Api.TestMutationV1ApiReq("2");
            mutationMockMvc(url, req)
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.header().exists("Orz-Version"))
                    .andExpect(MockMvcResultMatchers.header().string("Orz-Code", "1"));

            var response = mutationTestRestTemplate(url, req, rspClass);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getHeaders().containsKey("Orz-Version"));
            assertEquals("1", response.getHeaders().getFirst("Orz-Code"));

            triggerTestInterceptor(false, url, req, rspClass);
        }

        // 3: OrzUnexpectedException
        {
            var req = new TestMutationV1Api.TestMutationV1ApiReq("3");
            var exception = ExceptionUtils.throwableOfType(
                    assertThrowsExactly(
                            ServletException.class,
                            () -> mutationMockMvc(url, req)
                    ),
                    OrzUnexpectedException.class
            );
            assertEquals("@ORZ_UNEXPECTED_ERROR", exception.getEvent());
            assertEquals("request error", exception.getSummary());

            var response = mutationTestRestTemplate(url, req, rspClass);
            assertEquals(500, response.getStatusCode().value());
            assertFalse(response.getHeaders().containsKey("Orz-Version"));

            triggerTestInterceptor(false, url, req, rspClass);
        }

        // 4: OrzWebException not exists code
        {
            var req = new TestMutationV1Api.TestMutationV1ApiReq("4");
            mutationMockMvc(url, req)
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.header().exists("Orz-Version"))
                    .andExpect(MockMvcResultMatchers.header().string("Orz-Code", "undefined"));

            var response = mutationTestRestTemplate(url, req, rspClass);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getHeaders().containsKey("Orz-Version"));
            assertEquals("undefined", response.getHeaders().getFirst("Orz-Code"));

            triggerTestInterceptor(false, url, req, rspClass);
        }

        // 5: OrzWebException
        {
            var req = new TestMutationV1Api.TestMutationV1ApiReq("5");
            mutationMockMvc(url, req)
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.header().exists("Orz-Version"))
                    .andExpect(MockMvcResultMatchers.header().string("Orz-Code", "2"));

            var response = mutationTestRestTemplate(url, req, rspClass);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getHeaders().containsKey("Orz-Version"));
            assertEquals("2", response.getHeaders().getFirst("Orz-Code"));

            triggerTestInterceptor(false, url, req, rspClass);
        }

        // 6: Successful response
        {
            var req = new TestMutationV1Api.TestMutationV1ApiReq("6");
            mutationMockMvc(url, req)
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.header().exists("Orz-Version"))
                    .andExpect(MockMvcResultMatchers.header().doesNotExist("Orz-Code"));

            var response = mutationTestRestTemplate(url, req, rspClass);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getHeaders().containsKey("Orz-Version"));
            assertFalse(response.getHeaders().containsKey("Orz-Code"));
            assertNotNull(response.getBody());
            assertEquals(req.getTest(), response.getBody().getTest());

            triggerTestInterceptor(false, url, req, rspClass);
        }
    }

    @SneakyThrows
    private <T> void triggerTestInterceptor(boolean query, String baseUrl, Object req, Class<T> rspClass) {
        // 0: OrzAlarmException
        {
            var url = baseUrl + "?test=0";
            var exception = ExceptionUtils.throwableOfType(
                    assertThrowsExactly(
                            ServletException.class,
                            () -> requestMockMvc(query, url, req)
                    ),
                    OrzAlarmException.class
            );
            assertEquals("TestInterceptor", exception.getEvent());

            var response = requestTestRestTemplate(query, url, req, rspClass);
            assertEquals(500, response.getStatusCode().value());
            assertFalse(response.getHeaders().containsKey("Orz-Version"));
        }

        // 1: OrzWebException
        {
            var url = baseUrl + "?test=1";
            requestMockMvc(query, url, req)
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.header().exists("Orz-Version"))
                    .andExpect(MockMvcResultMatchers.header().string("Orz-Code", "1"));

            var response = requestTestRestTemplate(query, url, req, rspClass);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getHeaders().containsKey("Orz-Version"));
            assertEquals("1", response.getHeaders().getFirst("Orz-Code"));
        }

        // 2: OrzWebException + OrzAlarmException
        {
            var url = baseUrl + "?test=2";
            requestMockMvc(query, url, req)
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.header().exists("Orz-Version"))
                    .andExpect(MockMvcResultMatchers.header().string("Orz-Code", "1"));

            var response = requestTestRestTemplate(query, url, req, rspClass);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getHeaders().containsKey("Orz-Version"));
            assertEquals("1", response.getHeaders().getFirst("Orz-Code"));
        }

        // 3: OrzUnexpectedException
        {
            var url = baseUrl + "?test=3";
            var exception = ExceptionUtils.throwableOfType(
                    assertThrowsExactly(
                            ServletException.class,
                            () -> requestMockMvc(query, url, req)
                    ),
                    OrzUnexpectedException.class
            );
            assertEquals("@ORZ_UNEXPECTED_ERROR", exception.getEvent());
            assertEquals("TestInterceptor error", exception.getSummary());

            var response = requestTestRestTemplate(query, url, req, rspClass);
            assertEquals(500, response.getStatusCode().value());
            assertFalse(response.getHeaders().containsKey("Orz-Version"));
        }

        // 4: ResponseStatusException(401)
        {
            var url = baseUrl + "?test=4";
            requestMockMvc(query, url, req)
                    .andExpect(status().isUnauthorized())
                    .andExpect(MockMvcResultMatchers.header().doesNotExist("Orz-Version"));

            var response = requestTestRestTemplate(query, url, req, rspClass);
            assertEquals(401, response.getStatusCode().value());
            assertFalse(response.getHeaders().containsKey("Orz-Version"));
        }
    }

    @SneakyThrows
    private ResultActions queryMockMvc(String url, Object req) {
        return mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        );
    }

    @SneakyThrows
    private ResultActions mutationMockMvc(String url, Object req) {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        );
    }

    private ResultActions requestMockMvc(boolean query, String url, Object req) {
        return query ? queryMockMvc(url, req) : mutationMockMvc(url, req);
    }

    @SneakyThrows
    private <T> ResponseEntity<T> queryTestRestTemplate(String url, Object req, Class<T> rspClass) {
        var restTemplate = testRestTemplate.getRestTemplate();
        var requestCallback = restTemplate.httpEntityCallback(req, rspClass);
        var responseExtractor = restTemplate.<T>responseEntityExtractor(rspClass);
        return restTemplate.execute(url, HttpMethod.PUT, requestCallback, responseExtractor);
    }

    @SneakyThrows
    private <T> ResponseEntity<T> mutationTestRestTemplate(String url, Object req, Class<T> rspClass) {
        return testRestTemplate.postForEntity(url, req, rspClass);
    }

    private <T> ResponseEntity<T> requestTestRestTemplate(boolean query, String url, Object req, Class<T> rspClass) {
        return query ? queryTestRestTemplate(url, req, rspClass) : mutationTestRestTemplate(url, req, rspClass);
    }
}
