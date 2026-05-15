package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.ContactDto;
import com.smartrestaurant.backend.dto.ContactMessageRequestDto;
import com.smartrestaurant.backend.entity.Activity;
import com.smartrestaurant.backend.entity.Contact;
import com.smartrestaurant.backend.entity.Restaurant;
import com.smartrestaurant.backend.repository.ContactRepository;
import com.smartrestaurant.backend.repository.RestaurantRepository;
import com.smartrestaurant.backend.service.ActivityService;
import com.smartrestaurant.backend.service.ContactService;
import com.smartrestaurant.backend.service.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContactServiceImpl implements ContactService {

    private final RestaurantRepository restaurantRepository;
    private final ContactRepository contactRepository;
    private final EmailService emailService;
    private final ActivityService activityService;

    public ContactServiceImpl(RestaurantRepository restaurantRepository,
                              ContactRepository contactRepository,
                              EmailService emailService,
                              ActivityService activityService) {
        this.restaurantRepository = restaurantRepository;
        this.contactRepository = contactRepository;
        this.emailService = emailService;
        this.activityService = activityService;
    }

    private Restaurant getRestaurant(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant code is required");
        }
        return restaurantRepository.findByCode(code.trim())
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with code " + code));
    }

    @Override
    public List<ContactDto> getAllContacts(String restaurantCode) {
        Restaurant restaurant = getRestaurant(restaurantCode);
        return contactRepository.findByRestaurantOrderByNameAsc(restaurant)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ContactDto createContact(String restaurantCode, ContactDto dto) {
        Restaurant restaurant = getRestaurant(restaurantCode);

        Contact contact = Contact.builder()
                .restaurant(restaurant)
                .name(dto.getName())
                .email(dto.getEmail())
                .countryCode(dto.getCountryCode())
                .phoneNumber(dto.getPhoneNumber())
                .build();

        Contact saved = contactRepository.save(contact);

        activityService.log(
                restaurant.getCode(),
                Activity.Type.CONTACT_CREATED,
                "Contact added: " + saved.getName() + " (" + saved.getEmail() + ")",
                "INVENTORY"
        );

        return toDto(saved);
    }

    @Override
    public ContactDto updateContact(String restaurantCode, Long id, ContactDto dto) {
        Restaurant restaurant = getRestaurant(restaurantCode);

        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        if (!contact.getRestaurant().getId().equals(restaurant.getId())) {
            throw new IllegalArgumentException("Contact does not belong to this restaurant");
        }

        contact.setName(dto.getName());
        contact.setEmail(dto.getEmail());
        contact.setCountryCode(dto.getCountryCode());
        contact.setPhoneNumber(dto.getPhoneNumber());

        Contact updated = contactRepository.save(contact);

        activityService.log(
                restaurant.getCode(),
                Activity.Type.CONTACT_UPDATED,
                "Contact updated: " + updated.getName() + " (" + updated.getEmail() + ")",
                "INVENTORY"
        );

        return toDto(updated);
    }

    @Override
    public void deleteContact(String restaurantCode, Long id) {
        Restaurant restaurant = getRestaurant(restaurantCode);

        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        if (!contact.getRestaurant().getId().equals(restaurant.getId())) {
            throw new IllegalArgumentException("Contact does not belong to this restaurant");
        }

        activityService.log(
                restaurant.getCode(),
                Activity.Type.CONTACT_DELETED,
                "Contact deleted: " + contact.getName() + " (" + contact.getEmail() + ")",
                "INVENTORY"
        );

        contactRepository.delete(contact);
    }
    
    @Override
    public void sendContactMessage(String restaurantCode, ContactMessageRequestDto dto) {
        Restaurant restaurant = getRestaurant(restaurantCode);

        Contact contact = contactRepository.findById(dto.getContactId())
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        if (!contact.getRestaurant().getId().equals(restaurant.getId())) {
            throw new IllegalArgumentException("Contact does not belong to this restaurant");
        }

        String subject = (dto.getSubject() == null || dto.getSubject().trim().isEmpty())
                ? "Leftover Food Available - " + restaurant.getName()
                : dto.getSubject().trim();

        String body = (dto.getMessage() == null || dto.getMessage().trim().isEmpty())
                ? "Dear " + contact.getName() + ",\n\nGreetings from " + restaurant.getName() + "."
                : dto.getMessage().trim();

        emailService.sendEmail(contact.getEmail(), subject, body);

        activityService.log(
                restaurant.getCode(),
                Activity.Type.CONTACT_MESSAGE_SENT,
                "Leftover food message sent to contact: " + contact.getName() + " (" + contact.getEmail() + ")",
                "INVENTORY"
        );
    }

    private ContactDto toDto(Contact contact) {
        ContactDto dto = new ContactDto();
        dto.setId(contact.getId());
        dto.setName(contact.getName());
        dto.setEmail(contact.getEmail());
        dto.setCountryCode(contact.getCountryCode());
        dto.setPhoneNumber(contact.getPhoneNumber());
        return dto;
    }
}
