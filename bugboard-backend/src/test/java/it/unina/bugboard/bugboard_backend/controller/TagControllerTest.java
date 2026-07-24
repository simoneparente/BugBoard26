package it.unina.bugboard.bugboard_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unina.bugboard.bugboard_backend.dto.TagRequest;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TagController.class)
@AutoConfigureMockMvc(addFilters = false)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagService tagService;

    @MockitoBean
    private it.unina.bugboard.bugboard_backend.security.JwtService jwtService;

    @MockitoBean
    private it.unina.bugboard.bugboard_backend.security.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private it.unina.bugboard.bugboard_backend.security.CustomAccessDeniedHandler customAccessDeniedHandler;

    @MockitoBean
    private it.unina.bugboard.bugboard_backend.repository.UserRepository userRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private TagRequest tagRequest;
    private TagResponse tagResponse;
    private UUID tagId;
    private String projectKey;

    @BeforeEach
    void setUp() {
        tagId = UUID.randomUUID();
        projectKey = "FRONT";

        tagRequest = TagRequest.builder()
                .name("Bug")
                .color("#FF0000")
                .projectKey(projectKey)
                .build();

        tagResponse = TagResponse.builder()
                .id(tagId)
                .name("Bug")
                .color("#FF0000")
                .projectKey(projectKey)
                .build();
    }

    @Test
    void createTag_ShouldReturnCreatedAndTagResponse() throws Exception {
        when(tagService.createTag(any(TagRequest.class))).thenReturn(tagResponse);

        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(tagId.toString()))
                .andExpect(jsonPath("$.name").value("Bug"))
                .andExpect(jsonPath("$.color").value("#FF0000"))
                .andExpect(jsonPath("$.projectKey").value(projectKey));
    }

    @Test
    void getTagById_ShouldReturnOkAndTagResponse() throws Exception {
        when(tagService.getTagById(tagId)).thenReturn(tagResponse);

        mockMvc.perform(get("/api/tags/{id}", tagId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tagId.toString()))
                .andExpect(jsonPath("$.name").value("Bug"))
                .andExpect(jsonPath("$.color").value("#FF0000"))
                .andExpect(jsonPath("$.projectKey").value(projectKey));
    }

    @Test
    void getTagsByProjectKey_ShouldReturnOkAndListOfTagResponse() throws Exception {
        List<TagResponse> tagList = Arrays.asList(tagResponse);
        when(tagService.getAllTagsByProjectKey(projectKey)).thenReturn(tagList);

        mockMvc.perform(get("/api/tags/project/{projectKey}", projectKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(tagId.toString()))
                .andExpect(jsonPath("$[0].name").value("Bug"))
                .andExpect(jsonPath("$[0].color").value("#FF0000"))
                .andExpect(jsonPath("$[0].projectKey").value(projectKey));
    }
}
