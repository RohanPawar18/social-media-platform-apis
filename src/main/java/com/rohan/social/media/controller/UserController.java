package com.rohan.social.media.controller;

import com.rohan.social.media.entity.User;
import com.rohan.social.media.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("users")
    public List<User> getAllUsers(){
        log.info("Inside getAllUsers of UserController");
        return userService.getAllUsers();
    }
    @PostMapping("")
    public ResponseEntity<?> saveUser(@RequestBody User userName){
        log.info("Inside saveUser of UserController");
        return userService.saveUser(userName);
    }

    @PostMapping("/add/{userA}/{userB}")
    public ResponseEntity<?> sendFriendRequest(@PathVariable String userA, @PathVariable String userB){
        log.info("Inside sendFriendRequest of UserController");

        return userService.sendFriendRequest(userA, userB);
    }

    @GetMapping("friendRequests/{user}")
    public ResponseEntity<?> getPendingFriendRequests(@PathVariable String user){
        log.info("Inside getPendingFriendRequests of UserController");
        return userService.getPendingFriendRequests(user);
    }

    @GetMapping("/suggestions/{userName}")
    public ResponseEntity<?> getFriendSuggestion(@PathVariable String userName){
        log.info("Inside getFriendSuggestion of UserControl");
        return userService.getFriendSuggestion(userName);
    }
}
