package com.company.bonuswallet.controller;

import com.company.bonuswallet.entity.User;
import com.company.bonuswallet.jwt.AuthToken;
import com.company.bonuswallet.jwt.TokenProvider;
import com.company.bonuswallet.model.*;
import com.company.bonuswallet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenProvider tokenProvider;

    @PostMapping("/signUp")
    @CrossOrigin
    public ResponseEntity save(@RequestBody UserModel userModel) throws AuthenticationException{
        try {
            User user = userService.save(userModel);
            final Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userModel.getLogin(),
                            userModel.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            final String token = tokenProvider.generateToken(authentication);
            ResponseUserData responseUserData = ResponseUserData.builder()
                    .qrId(user.getQrId())
                    .bonus(user.getBonus())
                    .token(token)
                    .build();
            return ResponseEntity.ok(responseUserData);
        }
        catch (Exception e){
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin
    @PostMapping("/signIn")
    public ResponseEntity signIn(@RequestBody UserModel userModel) throws AuthenticationException {

        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userModel.getLogin(),
                        userModel.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final String token = tokenProvider.generateToken(authentication);
        User user = userService.getByLogin(userModel.getLogin());
        ResponseUserData responseUserData = ResponseUserData.builder()
                .qrId(user.getQrId())
                .token(token)
                .bonus(user.getBonus())
                .build();

        return ResponseEntity.ok(responseUserData);
    }

    @GetMapping("/all")
    @CrossOrigin
    public ResponseEntity getAll(){
        try {
            return new ResponseEntity(userService.findAll(), HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity getById(@PathVariable("id") Long id){
        try {
            return new ResponseEntity(userService.getById(id), HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteById(@PathVariable("id") Long id){
        try {
            userService.deleteById(id);
            return new ResponseEntity(HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/addBonusByPercent")
    public ResponseEntity addBonusByPercent(@RequestBody OperationModel operationModel){
        try {
            User user = userService.findByQrId(operationModel.getQrId());
            return new ResponseEntity(userService.addBonus(operationModel.getAmount(), user), HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/subtractBonus")
    public ResponseEntity subtractBonus(@RequestBody OperationModel operationModel) {
        try {
            User user = userService.findByQrId(operationModel.getQrId());
            return new ResponseEntity(userService.subtractBonus(operationModel.getAmount(), user), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/getBonus/{qrId}")
    public ResponseEntity getBonus(@PathVariable("qrId") String qrId){
        try {
            User user = userService.findByQrId(qrId);
            Bonus bonus = Bonus.builder()
                    .bonus(user.getBonus())
                    .build();
            return new ResponseEntity(bonus, HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
