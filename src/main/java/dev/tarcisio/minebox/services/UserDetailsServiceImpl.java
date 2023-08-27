package dev.tarcisio.minebox.services;

import dev.tarcisio.minebox.entities.User;
import dev.tarcisio.minebox.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
  @Autowired UserRepository userRepository;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String email)
      throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email).orElseThrow(
        ()
            -> new UsernameNotFoundException("User not found with email: " +
                                             email));

    return UserDetailsImpl.build(user);
  }

  @Transactional
  public UserDetails loadUserById(String id) throws UsernameNotFoundException {
    User user = userRepository.findById(id).orElseThrow(
        () -> new UsernameNotFoundException("User not found with id: " + id));

    return UserDetailsImpl.build(user);
  }
}
