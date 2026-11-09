package com.andre.papertrader.service;

import com.andre.papertrader.common.BusinessException;
import com.andre.papertrader.common.ErrorCode;
import com.andre.papertrader.dto.ChangePasswordReq;
import com.andre.papertrader.dto.CreateUserReq;
import com.andre.papertrader.dto.UserListReq;
import com.andre.papertrader.dto.UserPageRes;
import com.andre.papertrader.dto.UserRes;
import com.andre.papertrader.dto.UpdateUserReq;
import com.andre.papertrader.entity.User;
import com.andre.papertrader.entity.UserRole;
import com.andre.papertrader.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserPageRes list(UserListReq req) {
        int page = req == null || req.page() == null ? 1 : Math.max(req.page(), 1);
        int size = req == null || req.size() == null ? 10 : Math.min(Math.max(req.size(), 1), 100);
        Page<User> userPage = userRepository.findAll(
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"))
        );

        return new UserPageRes(
                userPage.getContent().stream().map(UserRes::from).toList(),
                userPage.getTotalElements(),
                page,
                size,
                userPage.getTotalPages()
        );
    }

    public UserRes get(Long id) {
        return UserRes.from(findEntity(id));
    }

    public UserRes create(CreateUserReq req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS, "用户名已存在：" + req.username());
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, "邮箱已存在：" + req.email());
        }
        if (req.phone() != null && !req.phone().isBlank() && userRepository.existsByPhone(req.phone())) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS, "手机号已存在：" + req.phone());
        }

        User user = new User();
        user.setUsername(req.username());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setEmail(req.email());
        user.setPhone(req.phone());
        user.setRole(req.role() == null ? UserRole.USER : req.role());
        return UserRes.from(userRepository.save(user));
    }

    public UserRes update(Long id, UpdateUserReq req) {
        User user = findEntity(id);

        if (userRepository.existsByUsernameAndIdNot(req.username(), id)) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS, "用户名已存在：" + req.username());
        }
        if (userRepository.existsByEmailAndIdNot(req.email(), id)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, "邮箱已存在：" + req.email());
        }
        if (req.phone() != null && !req.phone().isBlank() && userRepository.existsByPhoneAndIdNot(req.phone(), id)) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS, "手机号已存在：" + req.phone());
        }

        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPhone(req.phone());
        user.setRole(req.role() == null ? user.getRole() : req.role());
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
