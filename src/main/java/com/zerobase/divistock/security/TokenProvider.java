package com.zerobase.divistock.security;

import com.zerobase.divistock.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenProvider {
    private static final long TOKEN_EXPIRE_TIME=1000 * 60 * 60 *6;//6시간
    private static final String KEY_ROLE ="roles";

    private final MemberService memberService;

    /**
     *  토큰 생성 메소드(발급)
     * */

    @Value("${spring.jwt.secret}")
    private String secretKey;

    public String generateToken(String username, List<String> roles){

        Claims claims= Jwts.claims().setSubject(username);
        claims.put(KEY_ROLE,roles);

        var now = new Date();
        var expireDate = new Date(now.getTime()+TOKEN_EXPIRE_TIME);

     return   Jwts.builder()
                .setClaims(claims)
                .setIssuer(String.valueOf(now))// 토큰 생성 시간
                .setExpiration(expireDate)// 만료 날짜
                .signWith(SignatureAlgorithm.HS512,this.secretKey) //사용할 암호화 알고리즘, 비밀키
                .compact();


    }

    public String getUsername(String token){

        return this.parseClaim(token).getSubject();
    }

    public boolean validateToken(String token){
        if(!StringUtils.hasText(token))return false;

        var claims = this.parseClaim(token);

        return !claims.getExpiration().before(new Date());
    }

    private Claims parseClaim(String token){
        try{
            return Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody();
        }catch (ExpiredJwtException e){
            return e.getClaims();
        }
    }

    public Authentication getAuthentication(String jwt){

        UserDetails user= this.memberService.loadUserByUsername(this.getUsername(jwt));

        return new UsernamePasswordAuthenticationToken(user,"",user.getAuthorities());

    }


}
