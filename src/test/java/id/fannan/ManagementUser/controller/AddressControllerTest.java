package id.fannan.ManagementUser.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.fannan.ManagementUser.entity.Address;
import id.fannan.ManagementUser.entity.Contact;
import id.fannan.ManagementUser.entity.User;
import id.fannan.ManagementUser.model.AddressResponse;
import id.fannan.ManagementUser.model.CreateAddressRequest;
import id.fannan.ManagementUser.model.UpdateAddressRequest;
import id.fannan.ManagementUser.model.WebResponse;
import id.fannan.ManagementUser.repository.AddressRepository;
import id.fannan.ManagementUser.repository.ContactRepository;
import id.fannan.ManagementUser.repository.UserRepository;
import id.fannan.ManagementUser.security.BCrypt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AddressControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Clear the repositories before each test
        addressRepository.deleteAll();
        contactRepository.deleteAll();
        userRepository.deleteAll();

        // Create a sample user and contact for testing
        User user = new User();
        user.setUsername("test");
        user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
        user.setName("Test");
        user.setToken("test");
        user.setTokenExpiredAt(System.currentTimeMillis() + 1000000);
        userRepository.save(user);

        Contact contact = new Contact();
        contact.setId("test");
        contact.setUser(user);
        contact.setFirstName("fannan");
        contact.setLastName("fauzan");
        contact.setEmail("11@example.com");
        contact.setPhone("9238423432");
        contactRepository.save(contact);
    }

    @Test
    void createAddressBadRequest() throws Exception {
        CreateAddressRequest request = new CreateAddressRequest();
        request.setContactId("test");  // Add the contactId field

        mockMvc.perform(
                        post("/api/contacts/test/addresses")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .header("X-API-TOKEN", "test")
                )
                .andExpect(status().isBadRequest())
                .andDo(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());
                });
    }

    @Test
    void createAddressSuccess() throws Exception {
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreet("Jalan");
        request.setCity("Jakarta");
        request.setProvince("DKI");
        request.setCountry("Indonesia");
        request.setPostalCode("123123");

        mockMvc.perform(
                        post("/api/contacts/test/addresses")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .header("X-API-TOKEN", "test")
                )
                .andExpect(status().isOk())
                .andDo(result -> {
                    WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNull(response.getErrors());
                    assertEquals(request.getStreet(), response.getData().getStreet());
                    assertEquals(request.getCity(), response.getData().getCity());
                    assertEquals(request.getProvince(), response.getData().getProvince());
                    assertEquals(request.getCountry(), response.getData().getCountry());
                    assertEquals(request.getPostalCode(), response.getData().getPostalCode());
                });
    }

    @Test
    void getAddressNotFound() throws Exception {
        mockMvc.perform(
                        get("/api/contacts/test/addresses/test")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-API-TOKEN", "test")
                )
                .andExpect(status().isNotFound())
                .andDo(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());
                });
    }

    @Test
    void getAddressSuccess() throws Exception {
        Contact contact = contactRepository.findById("test").orElseThrow();
        Address address = new Address();
        address.setId("test");
        address.setContact(contact);
        address.setStreet("Jalan");
        address.setCity("Jakarta");
        address.setProvince("DKI");
        address.setCountry("Indonesia");
        address.setPostalCode("1234123");
        addressRepository.save(address);

        mockMvc.perform(
                        get("/api/contacts/test/addresses/test")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-API-TOKEN", "test")
                )
                .andExpect(status().isOk())
                .andDo(result -> {
                    WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNull(response.getErrors());
                    assertEquals(address.getId(), response.getData().getId());
                    assertEquals(address.getStreet(), response.getData().getStreet());
                    assertEquals(address.getCity(), response.getData().getCity());
                    assertEquals(address.getProvince(), response.getData().getProvince());
                    assertEquals(address.getCountry(), response.getData().getCountry());
                    assertEquals(address.getPostalCode(), response.getData().getPostalCode());
                });
    }
//
//    @Test
//    void updateAddressBadRequest() throws Exception {
//        UpdateAddressRequest request = new UpdateAddressRequest();
//        request.setCountry("test");  // Add the contactId field
//
//        mockMvc.perform(
//                        put("/api/contacts/test/addresses/test")
//                                .accept(MediaType.APPLICATION_JSON)
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(objectMapper.writeValueAsString(request))
//                                .header("X-API-TOKEN", "test")
//                )
//                .andExpect(status().isBadRequest())
//                .andDo(result -> {
//                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
//                    });
//                    assertNotNull(response.getErrors());
//                });
//    }

    @Test
    void updateAddressSuccess() throws Exception {

        Contact contact = contactRepository.findById("test").orElseThrow();
        Address address = new Address();
        address.setId("test");
        address.setContact(contact);
        address.setStreet("lama");
        address.setCity("lama");
        address.setProvince("lama");
        address.setCountry("lama");
        address.setPostalCode("1234123");
        addressRepository.save(address);

        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreet("Jalan");
        request.setCity("Jakarta");
        request.setProvince("DKI");
        request.setCountry("Indonesia");
        request.setPostalCode("123123");

        mockMvc.perform(
                        put("/api/contacts/test/addresses/test")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .header("X-API-TOKEN", "test")
                )
                .andExpect(status().isOk())
                .andDo(result -> {
                    WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNull(response.getErrors());
                    assertEquals(request.getStreet(), response.getData().getStreet());
                    assertEquals(request.getCity(), response.getData().getCity());
                    assertEquals(request.getProvince(), response.getData().getProvince());
                    assertEquals(request.getCountry(), response.getData().getCountry());
                    assertEquals(request.getPostalCode(), response.getData().getPostalCode());
                });
    }

    @Test
    void deleteAddressNotFound() throws Exception {
        mockMvc.perform(
                        delete("/api/contacts/test/addresses/test")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-API-TOKEN", "test")
                )
                .andExpect(status().isNotFound())
                .andDo(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());
                });
    }

    @Test
    void deleteAddressSuccess() throws Exception {
        Contact contact = contactRepository.findById("test").orElseThrow();
        Address address = new Address();
        address.setId("test");
        address.setContact(contact);
        address.setStreet("Jalan");
        address.setCity("Jakarta");
        address.setProvince("DKI");
        address.setCountry("Indonesia");
        address.setPostalCode("1234123");
        addressRepository.save(address);

        mockMvc.perform(
                        delete("/api/contacts/test/addresses/test")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-API-TOKEN", "test")
                )
                .andExpect(status().isOk())
                .andDo(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNull(response.getErrors());
                    assertEquals("OK", response.getData());
                    assertFalse(addressRepository.existsById("test"));
                });
    }

    @Test
    void listAddressNotFound() throws Exception {
        mockMvc.perform(
                        get("/api/contacts/salah/addresses")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-API-TOKEN", "test")
                )
                .andExpect(status().isNotFound())
                .andDo(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());
                });
    }

    @Test
    void listAddressSuccess() throws Exception {
        Contact contact = contactRepository.findById("test").orElseThrow();
        for (int i = 0; i < 5; i++) {
            Address address = new Address();
            address.setId("test " + i);
            address.setContact(contact);
            address.setStreet("Jalan");
            address.setCity("Jakarta");
            address.setProvince("DKI");
            address.setCountry("Indonesia");
            address.setPostalCode("1234123");
            addressRepository.save(address);

        }

        mockMvc.perform(
                        get("/api/contacts/test/addresses")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-API-TOKEN", "test")
                )
                .andExpect(status().isOk())
                .andDo(result -> {
                    WebResponse<List<AddressResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNull(response.getErrors());
                    assertEquals(5, response.getData().size());
                });
    }


}