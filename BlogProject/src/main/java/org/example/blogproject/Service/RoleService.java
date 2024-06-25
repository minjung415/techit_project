package org.example.blogproject.Service;

import lombok.RequiredArgsConstructor;
import org.example.blogproject.domain.Role;
import org.example.blogproject.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    @Transactional
    public Role getRole(String name){
        return roleRepository.findByName(name);
    }
}
