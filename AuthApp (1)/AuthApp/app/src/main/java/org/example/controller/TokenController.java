package org.example.controller;

import org.example.entities.RefreshToken;
import org.example.request.AuthRequest;
import org.example.request.RefreshTokenRequest;
import org.example.response.JwtResponse;
import org.example.service.JwtService;
import org.example.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class TokenController {
    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("auth/v1/login")
    public ResponseEntity login(@RequestBody AuthRequest authRequest){
        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(),authRequest.getPassword()));
        if(auth.isAuthenticated()){
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                    authRequest.getUsername());
            return new ResponseEntity<>(JwtResponse.builder()
                    .accessToken(jwtService.GenerateToken(authRequest.getUsername()))
                    .token(refreshToken.getToken())
                    .build(),HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("Exception in service",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("auth/v1/refreshToken")
    public JwtResponse genRefreshToken (@RequestBody RefreshTokenRequest refreshTokenRequest){
        return refreshTokenService.findByToken(refreshTokenRequest.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUserInfo)
                .map(userInfo -> {
                    String accessToken = jwtService.GenerateToken(userInfo.getUsername());
                    return JwtResponse.builder()
                            .accessToken(accessToken)
                            .token(refreshTokenRequest.getToken())
                            .build();
                }).orElseThrow(()-> new RuntimeException("Refresh Token is not in DB"));
    }
}
