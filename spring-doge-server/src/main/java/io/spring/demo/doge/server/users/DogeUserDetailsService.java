package io.spring.demo.doge.server.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Adapts {@link io.spring.demo.doge.server.users.UserRepository}
 * to the {@link org.springframework.security.core.userdetails.UserDetailsService} contract.
 *
 * @author Josh Long
 */
@Service
public class DogeUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    public static final String ROLE_USER = "ROLE_USER";

    @Autowired
    public DogeUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        io.spring.demo.doge.server.users.User user = this.userRepository.findOne(s);
        boolean enabled = user.isEnabled();
        return new User(user.getId(), user.getPassword(), enabled, enabled, enabled, enabled,
                Arrays.asList(new SimpleGrantedAuthority(ROLE_USER)));
    }
}
