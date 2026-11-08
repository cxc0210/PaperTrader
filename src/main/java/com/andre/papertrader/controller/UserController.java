package com.andre.papertrader.controller;

import com.andre.papertrader.common.Result;
import com.andre.papertrader.dto.ChangePasswordReq;
import com.andre.papertrader.dto.CreateUserReq;
import com.andre.papertrader.dto.UserRes;
import com.andre.papertrader.service.UserService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/list")
    public Result<List<UserRes>> list() {
        return Result.ok(userService.list());
    }

    @PostMapping("/{id}")
    public Result<UserRes> get(@PathVariable Long id) {
        return Result.ok(userService.get(id));
    }

    @PostMapping("/create")
    public Result<UserRes> create(@RequestBody CreateUserReq req) {
        return Result.ok(userService.create(req));
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
