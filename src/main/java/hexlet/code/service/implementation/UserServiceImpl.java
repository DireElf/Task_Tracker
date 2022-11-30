package hexlet.code.service.implementation;

import hexlet.code.dto.UserDto;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static hexlet.code.config.security.SecurityConfig.DEFAULT_AUTHORITIES;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createNewUser(UserDto dto) {
        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User updateUser(long id, UserDto dto) {
        final User userToUpdate = userRepository.findById(id).get();
        userToUpdate.setEmail(dto.getEmail());
        userToUpdate.setFirstName(dto.getFirstName());
        userToUpdate.setLastName(dto.getLastName());
        userToUpdate.setPassword(passwordEncoder.encode(dto.getPassword()));
        return userRepository.save(userToUpdate);
    }

    @Override
    public String getCurrentUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public User getCurrentUser() {
        return userRepository.findByEmail(getCurrentUserName()).get();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .map(user -> buildSpringUser(user))
                .orElseThrow(() -> new UsernameNotFoundException("Not found user with 'email': " + username));
    }

    private UserDetails buildSpringUser(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                DEFAULT_AUTHORITIES
        );
    }
}
