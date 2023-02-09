package com.victor.service;

import com.victor.dto.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserService {

    List<User> users = new ArrayList<>();

    public List<User> getAll() {
        return users;
    }

    public boolean add(User user) {
        return users.add(user);
    }
}
