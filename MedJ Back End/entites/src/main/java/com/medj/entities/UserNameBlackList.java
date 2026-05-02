package com.medj.entities;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class UserNameBlackList {
    private final Set<String> blacklistedUsernames = new HashSet<>();

    public void addToBlacklist(String username) {
        blacklistedUsernames.add(username);
    }

    public boolean isBlacklisted(String username) {
        return blacklistedUsernames.contains(username);
    }

    public void removeFromBlacklist(String username) {
        blacklistedUsernames.remove(username);
    }
}
