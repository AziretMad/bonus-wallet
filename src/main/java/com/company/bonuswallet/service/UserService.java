package com.company.bonuswallet.service;

import com.company.bonuswallet.entity.Role;
import com.company.bonuswallet.entity.User;
import com.company.bonuswallet.exception.BonusIsNull;
import com.company.bonuswallet.exception.SubtractionMoreBonus;
import com.company.bonuswallet.model.UserModel;
import com.company.bonuswallet.repository.RoleRepository;
import com.company.bonuswallet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    private static final Integer percent = 10;

    public User save(UserModel userModel){
        Role role = Role.builder().roleName("USER").build();
        User user = User.builder()
                .login(userModel.getLogin())
                .password(passwordEncoder.encode(userModel.getPassword()))
                .qrId(createUniqueQrId())
                .bonus(BigDecimal.valueOf(0))
                .build();
        roleRepository.save(role);
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        Set<User> users = new HashSet<>();
        users.add(user);
        if(role.getUsers() == null){
            role.setUsers(users);
        }
        else {
            role.getUsers().add(user);
        }
        return userRepository.save(user);
    }

    public void deleteById(Long id){
        userRepository.deleteById(id);
    }

    public User getById(Long id){
        return userRepository.getOne(id);
    }

    public List<User> findAll(){
        return userRepository.findAll();
    }

    public String createUniqueQrId(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public User addBonus(BigDecimal amount, User user){
        BigDecimal bonus = amount.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100));
        BigDecimal increasedBonus = user.getBonus().add(bonus);
        user.setBonus(increasedBonus);
        return userRepository.save(user);
    }

    public User subtractBonus(BigDecimal amount, User user) throws Exception{
        if (user.getBonus().compareTo(BigDecimal.valueOf(0)) == 0){
            throw new BonusIsNull();
        }
        if(user.getBonus().compareTo(amount) == -1){
           throw new SubtractionMoreBonus();
        }
        BigDecimal decreasedBonus = user.getBonus().subtract(amount);
        user.setBonus(decreasedBonus);
        return userRepository.save(user);
    }

    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(login);
        if(user == null){
            throw new UsernameNotFoundException("Invalid login or password");
        }
        return new org.springframework.security.core.userdetails.User(user.getLogin(), user.getPassword(), getAuthority(user) );
    }

    private Set<SimpleGrantedAuthority> getAuthority(User user){
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));
        });
        return authorities;
    }

    public User findByQrId(String qrId){
        return userRepository.findByQrId(qrId);
    }

    public User getByLogin(String login){
        return userRepository.findByLogin(login);
    }

}
