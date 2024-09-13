package io.github.ardoco.rest.api.controller;

import io.github.ardoco.rest.api.service.ArDoCoForSadCodeTLRService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ArDoCoForSadCodeTLRController.class)
public class ArDoCoForSadCodeTLRControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArDoCoForSadCodeTLRService service; // inject mock for Service, otherwise the application context cannot start. Its expectations can be set using Mockito

    @Test
    void serviceShouldReturnID() throws Exception {
        //when(service.runPipeline()).thenReturn("ID123"); // use mockito to specify answer of the mocked service
        MockMultipartFile inputText = new MockMultipartFile("inputText", "inputText.txt", "text/plain", "Sample text".getBytes());
        MockMultipartFile inputCode = new MockMultipartFile("inputCode", "inputCode.acm", "text/plain", "Sample code".getBytes());

        mockMvc.perform(multipart("/api/sad-code/start")
                        .file(inputText)
                        .file(inputCode)
                        .param("projectName", "TestProject"))
                .andExpect(status().isOk());
    }
}
