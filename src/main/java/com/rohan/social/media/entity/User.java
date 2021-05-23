package com.rohan.social.media.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class User {
    @Id
    private String userName;

    @ElementCollection
    private Set<String> friendRequestSent;

    @ElementCollection
    private  Set<String> friendRequestPending;

    @ElementCollection
    private Set<String> friends;
}
