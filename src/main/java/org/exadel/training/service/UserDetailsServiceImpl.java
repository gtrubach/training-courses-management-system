package org.exadel.training.service;

import org.exadel.training.dao.UserDAO;
import org.exadel.training.model.CustomUserDetails;
import org.exadel.training.model.Role;
import org.exadel.training.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.exadel.training.utils.RoleUtil.EXTERNAL_VISITOR_SMALL;
import static org.exadel.training.utils.RoleUtil.buildRoleForAuthorization;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserDAO userDAO;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userDAO.getUserByLogin(userName);
        if (user == null) {
            throw new UsernameNotFoundException("Wrong login");
        }
        List<GrantedAuthority> authorities = buildUserAuthority(user.getRoles());
        for (Role role : user.getRoles()) {
            if (role.getRole().equals(EXTERNAL_VISITOR_SMALL)) {
                return new CustomUserDetails(user.getUserId(), user.getFirstName(), user.getLastName(), user.getLogin(), user.getPassword(), false, authorities);
            }
        }
        return new CustomUserDetails(user.getUserId(), user.getFirstName(), user.getLastName(), user.getLogin(), user.getPassword(), authorities);
    }

    private List<GrantedAuthority> buildUserAuthority(Set<Role> roles) {
        Set<GrantedAuthority> authoritySet = roles.stream().map(role -> new SimpleGrantedAuthority(buildRoleForAuthorization(role.getRole()))).collect(Collectors.toSet());
        return new ArrayList<>(authoritySet);
    }
}
