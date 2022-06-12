package com.gydronium.storedemo.controller;

import com.gydronium.storedemo.dao.UserDao;
import com.gydronium.storedemo.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class UserController {

    private final UserDao userDao;

    @Autowired
    public UserController(UserDao userDao) {
        this.userDao = userDao;
    }

    @GetMapping("/user/all")
    Iterable<UserModel> getAll() {
        return userDao.findAll();
    }

    @GetMapping("/user/{id}")
    UserModel getUserById(@PathVariable Long id) {
        return userDao.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/user/save")
    UserModel save(@RequestBody UserModel user) {
        return userDao.save(user);
    }
}
