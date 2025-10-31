package com.scapuz.msa_clients.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scapuz.msa_clients.infrastructure.adapter.rest.dto.ClientRequest;
import com.scapuz.msa_clients.infrastructure.adapter.rest.dto.ClientResponse;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String createdClientId;

    @Test
    @Order(1)
    @DisplayName("Should create client successfully - Integration Test")
    void shouldCreateClientSuccessfully() throws Exception {
        // Arrange
        ClientRequest request = ClientRequest.builder()
                .name("John Doe")
                .phone("+593987654321")
                .birthDate(LocalDate.of(1990, 5, 15))
                .build();

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        // Store the created ID for subsequent tests
        ClientResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ClientResponse.class);
        createdClientId = response.getClientId().toString();
    }

    @Test
    @Order(2)
    @DisplayName("Should get client by ID - Integration Test")
    void shouldGetClientById() throws Exception {
        mockMvc.perform(get("/api/v1/clients/" + createdClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdClientId))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.clientCode").value("CLT000001"));
    }

    @Test
    @Order(3)
    @DisplayName("Should get client by code - Integration Test")
    void shouldGetClientByCode() throws Exception {
        mockMvc.perform(get("/api/v1/clients/code/CLT000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientCode").value("CLT000001"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @Order(4)
    @DisplayName("Should get all clients - Integration Test")
    void shouldGetAllClients() throws Exception {
        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(5)
    @DisplayName("Should update client successfully - Integration Test")
    void shouldUpdateClientSuccessfully() throws Exception {
        // Arrange
        ClientRequest updateRequest = ClientRequest.builder()
                .name("John Doe")
                .phone("+593987654321")
                .birthDate(LocalDate.of(1990, 5, 15))
                .status("ACTIVE")
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/v1/clients/" + createdClientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Doe Updated"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @Order(6)
    @DisplayName("Should return 404 for non-existent client - Integration Test")
    void shouldReturn404ForNonExistentClient() throws Exception {
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        mockMvc.perform(get("/api/v1/clients/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    @Order(7)
    @DisplayName("Should return 400 for invalid client data - Integration Test")
    void shouldReturn400ForInvalidClientData() throws Exception {
        // Arrange - Invalid email
        ClientRequest invalidRequest = ClientRequest.builder()
                .name("John Doe")
                .phone("+593987654321")
                .birthDate(LocalDate.of(1990, 5, 15))
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    @Order(8)
    @DisplayName("Should return 400 for duplicate client code - Integration Test")
    void shouldReturn400ForDuplicateClientCode() throws Exception {
        // Arrange - Same client code as the first created client
        ClientRequest duplicateRequest = ClientRequest.builder()
                .name("John Doe")
                .phone("+593987654321")
                .birthDate(LocalDate.of(1990, 5, 15))
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Client code already exists")));
    }

    @Test
    @Order(9)
    @DisplayName("Should delete client successfully - Integration Test")
    void shouldDeleteClientSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/v1/clients/" + createdClientId))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/v1/clients/" + createdClientId))
                .andExpect(status().isNotFound());
    }
}