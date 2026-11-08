package com.andre.papertrader.service;

import com.andre.papertrader.dto.ChangePasswordReq;
import com.andre.papertrader.dto.CreateUserReq;
import com.andre.papertrader.dto.UserRes;
import com.andre.papertrader.entity.User;
import com.andre.papertrader.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserRes> list() {
        return userRepository.findAll().stream().map(UserRes::from).toList();
    }

    public UserRes get(Long id) {
        return UserRes.from(findEntity(id));
    }

    public UserRes create(CreateUserReq req) {
        User user = new User();
        user.setUsername(req.username());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setEmail(req.email());
        user.setPhone(req.phone());
        return UserRes.from(userRepository.save(user));
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在: " + id);
        }
        userRepository.deleteById(id);
    }

    public void changePassword(Long id, ChangePasswordReq req) {
        User user = findEntity(id);
        if (!passwordEncoder.matches(req.oldPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "旧密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    private User findEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在: " + id));
    }
}
