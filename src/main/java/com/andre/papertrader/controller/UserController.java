package com.andre.papertrader.controller;

import com.andre.papertrader.common.Result;
import com.andre.papertrader.dto.ChangePasswordReq;
import com.andre.papertrader.dto.CreateUserReq;
import com.andre.papertrader.dto.UserListReq;
import com.andre.papertrader.dto.UserPageRes;
import com.andre.papertrader.dto.UserRes;
import com.andre.papertrader.dto.UpdateUserReq;
import com.andre.papertrader.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/me")
    public Result<UserRes> me(@AuthenticationPrincipal Long userId) {
        return Result.ok(userService.get(userId));
    }

    @PostMapping("/me/password")
    public Result<Void> changeMyPassword(
            @AuthenticationPrincipal Long userId,
            @RequestBody ChangePasswordReq req
    ) {
        userService.changePassword(userId, req);
        return Result.ok();
    }

    @PostMapping("/list")
    public Result<UserPageRes> list(@RequestBody(required = false) UserListReq req) {
        return Result.ok(userService.list(req));
    }

    @PostMapping("/{id}")
    public Result<UserRes> get(@PathVariable Long id) {
        return Result.ok(userService.get(id));
    }

    @PostMapping("/create")
    public Result<UserRes> create(@RequestBody CreateUserReq req) {
        return Result.ok(userService.create(req));
    }

    @PostMapping("/{id}/update")
    public Result<UserRes> update(@PathVariable Long id, @RequestBody UpdateUserReq req) {
        return Result.ok(userService.update(id, req));
    }

    @PostMapping("/{id}/delete")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok();
    }

    @PostMapping("/{id}/password")
    public Result<Void> changePassword(@PathVariable Long id, @RequestBody ChangePasswordReq req) {
        userService.changePassword(id, req);
        return Result.ok();
    }
}
