package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.ContactDto;
import com.smartrestaurant.backend.dto.ContactMessageRequestDto;

import java.util.List;

public interface ContactService {
    List<ContactDto> getAllContacts(String restaurantCode);
    ContactDto createContact(String restaurantCode, ContactDto dto);
    ContactDto updateContact(String restaurantCode, Long id, ContactDto dto);
    void deleteContact(String restaurantCode, Long id);
    void sendContactMessage(String restaurantCode, ContactMessageRequestDto dto);
}
