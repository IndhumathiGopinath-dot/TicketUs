package com.ticketsystem.service;

import com.ticketsystem.model.User;
import com.ticketsystem.model.enums.Category;
import com.ticketsystem.model.enums.Role;
import com.ticketsystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoutingService {

    private static final Map<Category, String> CATEGORY_TO_DEPARTMENT = Map.of(
            Category.IT, "IT",
            Category.BUG, "IT",
            Category.HR, "HR"
    );

    private final UserRepository userRepository;
    private final TicketLoadCounter loadCounter;

    public RoutingService(UserRepository userRepository, TicketLoadCounter loadCounter) {
        this.userRepository = userRepository;
        this.loadCounter = loadCounter;
    }

    /**
     * Pick a least-loaded admin in the matching department.
     */
    public Optional<User> routeTicket(Category category) {
        String department = CATEGORY_TO_DEPARTMENT.get(category);
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        List<User> candidates = admins.stream()
                .filter(u -> department == null || department.equalsIgnoreCase(u.getDepartment()))
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            candidates = admins; // fall back to any admin
        }
        if (candidates.isEmpty()) return Optional.empty();

        return candidates.stream()
                .min(Comparator.comparingLong(u -> loadCounter.countOpen(u.getId())));
    }
}
