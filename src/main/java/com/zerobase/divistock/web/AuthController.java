package com.zerobase.divistock.web;


import com.zerobase.divistock.model.Auth;
import com.zerobase.divistock.security.TokenProvider;
import com.zerobase.divistock.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {


    private final MemberService memberService;

    private final TokenProvider tokenProvider;


    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody Auth.SignUp request){
       var result = this.memberService.register(request);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request){

      var member =  this.memberService.authenticate(request);
        //Sign in 검증, 토큰 생성해서 반환
      var token=  this.tokenProvider.generateToken(member.getUsername(),member.getRoles());
    log.info("userLogin ->"+request.getUsername());
      return ResponseEntity.ok(token);  
    }
}
