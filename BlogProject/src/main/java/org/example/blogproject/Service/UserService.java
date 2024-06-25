package org.example.blogproject.Service;

import lombok.RequiredArgsConstructor;
import org.example.blogproject.domain.Role;
import org.example.blogproject.domain.User;
import org.example.blogproject.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    //user create
    @Transactional
    public void createUser(User user){
        if(userRepository.existsByUsername(user.getUsername())){
            throw new IllegalArgumentException("Username already exists.");
        }
        userRepository.save(user);
    }
    @Transactional
    public boolean isExistUsername(String username){
        return userRepository.existsByUsername(username);
    }
    @Transactional
    public User getUser(String username){
        return userRepository.findByUsername(username);
    }
}
