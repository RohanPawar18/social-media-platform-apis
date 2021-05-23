package com.rohan.social.media.service;

import com.rohan.social.media.entity.User;
import com.rohan.social.media.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserService {
    @Autowired
    private UserRepository userRepository;

   // @Autowired
    private User user;
    Set<String> userSet;
    Set<String> pendingUserSet;
    Set<String> friends;

    public ResponseEntity saveUser(User userName) {
        user = new User();
        log.info("Inside saveUser of UserService");
        try {
            user = userRepository.save(userName);
        }catch(Exception e){
            return new ResponseEntity<>("{'status':'failure', 'reason':'"+e.getMessage()+"'}",
                                        HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    public ResponseEntity sendFriendRequest(String userA, String userB) {

        try {
            User currentUser = userRepository.findByUserName(userA);

            // Checking if user A has already sent request to user B
            Set<String> currentUserFriendRequestSent = currentUser.getFriendRequestSent();
            if(currentUserFriendRequestSent!=null && currentUserFriendRequestSent.size()>0){
                for(String requestedTo : currentUser.getFriendRequestSent()) {
                    if (requestedTo.equalsIgnoreCase(userB)) {
                       log.error(userA + " has already sent request to " + userB);
                      return new ResponseEntity<>("{'status':'failure', 'reason':'Request already sent'}",
                             HttpStatus.BAD_REQUEST);
                 }
                 }
            }

            User pendingUser = userRepository.findByUserName(userB);

            userSet = currentUser.getFriendRequestSent();
            if(userSet==null){
                log.info("--- Creating userSet");
                userSet = new HashSet<>();
            }
            pendingUserSet = pendingUser.getFriendRequestPending();
            if(pendingUser==null){
                log.info("--- Creating pendingUserSet");
                pendingUserSet = new HashSet<>();
            }

            userSet.add(pendingUser.getUserName());
            currentUser.setFriendRequestSent(userSet);

            userRepository.save(currentUser);

            pendingUserSet.add(currentUser.getUserName());
            pendingUser.setFriendRequestPending(pendingUserSet);

            userRepository.save(pendingUser);

            // Check if both users sent request to each other
            Set<String> currentUserRequestSendSet = currentUser.getFriendRequestSent();
            Set<String> currentUserRequestPendingSet = currentUser.getFriendRequestPending();
            Set<String> intersectSet = currentUserRequestSendSet.stream()
                                        .filter(currentUserRequestPendingSet::contains)
                                        .collect(Collectors.toSet());

            log.info("Common user is : "+intersectSet);

            if(intersectSet.size()>0){
                friends = currentUser.getFriends();
                friends.add(pendingUser.getUserName());
                currentUser.setFriends(friends);

                friends = pendingUser.getFriends();
                friends.add(currentUser.getUserName());
                pendingUser.setFriends(friends);

                log.info("Friend request accepted");

                // Remove userName from pending and sent list for both the users
                if(currentUser.getFriendRequestPending().contains(userB))
                    currentUser.getFriendRequestPending().remove(userB);
                if(currentUser.getFriendRequestSent().contains(userB))
                    currentUser.getFriendRequestSent().remove(userB);
                if(pendingUser.getFriendRequestPending().contains(userA))
                    pendingUser.getFriendRequestPending().remove(userA);
                if(pendingUser.getFriendRequestSent().contains(userA))
                    pendingUser.getFriendRequestSent().remove(userA);

                userRepository.save(currentUser);
                userRepository.save(pendingUser);
            }


            return new ResponseEntity<>("{'status':'sucess'}", HttpStatus.ACCEPTED);
        }catch (Exception e){
            log.info("Exception occured while updating sendFriendRequest to other user: "+e);
            return new ResponseEntity<>("{'status':'failure', 'reason':'"+e.getMessage()+"'}",
                    HttpStatus.BAD_REQUEST);
        }
    }

    public List<User> getAllUsers() {
        log.info("Inside getAllUsers of UserService");
        return userRepository.findAll();
    }

    public ResponseEntity<?> getPendingFriendRequests(String userName) {
        log.info("Inside getPendingFriendRequests of UserService");
        User user = userRepository.findByUserName(userName);
        if(user.getFriendRequestPending().size()>0)
            return new ResponseEntity<>("'friend_requests':"+user.getFriendRequestPending(), HttpStatus.OK);
        else
            return new ResponseEntity<>("{'status':'failure', 'reason':'failure'", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> getFriendSuggestion(String userName) {
        log.info("Inside getFriendSuggestion of UserService");
        User user = userRepository.findByUserName(userName);
        if(user==null){
            return new ResponseEntity<>("{'status':'failure', 'reason':'User does not exists'", HttpStatus.BAD_REQUEST);
        }
        if(user.getUserName().isEmpty()){
            return new ResponseEntity<>("{'status':'failure', 'reason':'User does not exists'", HttpStatus.BAD_REQUEST);
        }
        Set<String> friends = user.getFriends();
        Set<String> finalSuggestion = new HashSet<>();

        if(friends!=null && friends.size()>0){
            for(String uName : friends){
                Set<String> currentUserFriends = null;
                User tempUser = userRepository.findByUserName(uName);
                if(tempUser!=null) {
                    currentUserFriends = tempUser.getFriends();
                }
                if(currentUserFriends!=null && currentUserFriends.size()>0) {
                    for (String name : currentUserFriends) {
                        log.info("Friend suggestion found: "+name);
                        if(! name.equalsIgnoreCase(userName)) // This indicates we are not suggesting those users who are already friends
                            finalSuggestion.add(name);
                    }
                }
            }
        }
        if(finalSuggestion.size()==0){
            log.info("User "+userName+" has no friends");
            return new ResponseEntity<>("{'status':'failure', 'reason':'User has no friends'", HttpStatus.NOT_FOUND);
        }
        else{
            return new ResponseEntity<>("'suggestions': "+finalSuggestion, HttpStatus.OK);
        }

    }
}
