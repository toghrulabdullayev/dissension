package app.dissension.demo.auth.security;

import app.dissension.demo.auth.entity.AppUser;
import app.dissension.demo.auth.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // marks the class as a service component (Bean)
public class CustomUserDetailsService implements UserDetailsService {

  private final AppUserRepository appUserRepository; // composition (dependency injection)

  public CustomUserDetailsService(AppUserRepository appUserRepository) {
    this.appUserRepository = appUserRepository;
  }

  @Override // UserDetailsService's method, which loads user specific data during auth
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    AppUser appUser = appUserRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return User.withUsername(appUser.getUsername())
        .password(appUser.getPasswordHash())
        .roles("USER")
        .build();
  }
}
