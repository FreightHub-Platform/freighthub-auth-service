package com.freighthub.auth_service.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
//import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {
    //    @Value("${jwt.secret}")
//    private String JWT_SECRET;
    private String JWT_SECRET= "c1b5f182c547446f06452cf50ae9a48cee78af851703475e3d0490204a2cf0af17bb20b2c3824c77f72c4654d4b4d19e25c198a3747312c8c8a742ad9048218b86bffaaa9b260578d35ae266bf97ef9449c59450f7b7f648939ee5c8a9054e24d10a289e1e005b983aeeeb63cf1f2fc734f4294dc1416da3efaf1a91d17cc73aa6a87b986238d47051bc7ab3dbc582d78a688cae3d8bebc8810475e99ebb3d3b1262ce1df1d3924766539ebc2074275a4bcbdba8e70f3795ea35e2d3fa6b1eca1147934c1024b6997d910cccd06a4aa6464f93455f3388f9da443f10a456922aac17e4999870bfd295f2622dc27dfd89b8c68b3ce504250c02d894f02c706879";

    //    @Value("${jwt.expiration}")
//    private int JWT_EXPIRATION;
    private int JWT_EXPIRATION=3600000;

    public String generateJwtToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + JWT_EXPIRATION))
                .signWith(SignatureAlgorithm.ES256, JWT_SECRET)
                .compact();

    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .setSigningKey(JWT_SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJwt(authToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}