package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.ContactDto;
import com.smartrestaurant.backend.dto.ContactMessageRequestDto;
import com.smartrestaurant.backend.service.ContactService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contacts")
@CrossOrigin(origins = "*")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'INVENTORY')")
    public ResponseEntity<List<ContactDto>> getAll(HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(contactService.getAllContacts(restaurantCode));
    }

    @PostMapping
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<ContactDto> create(@RequestBody ContactDto dto, HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(contactService.createContact(restaurantCode, dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<ContactDto> update(@PathVariable Long id,
                                             @RequestBody ContactDto dto,
                                             HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(contactService.updateContact(restaurantCode, id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        contactService.deleteContact(restaurantCode, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/send")
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<String> sendMessage(@RequestBody ContactMessageRequestDto dto,
                                              HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        contactService.sendContactMessage(restaurantCode, dto);
        return ResponseEntity.ok("Message sent successfully");
    }
}
