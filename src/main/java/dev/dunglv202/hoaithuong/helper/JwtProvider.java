package dev.dunglv202.hoaithuong.helper;

import dev.dunglv202.hoaithuong.model.Token;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;

@Component
public class JwtProvider {
    private final SecretKey secretKey;

    public JwtProvider(@Value("${auth.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Token generateToken(String subject, Duration lifetime, Map<String, String> extras) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime expiration = now.plus(lifetime);
        JwtBuilder builder =  Jwts.builder()
            .subject(subject)
            .issuedAt(Date.from(now.toInstant(ZoneOffset.UTC)))
            .expiration(Date.from(expiration.toInstant(ZoneOffset.UTC)))
            .claims(extras)
            .signWith(secretKey);

        return new Token(builder.compact(), LocalDateTime.from(expiration.minusSeconds(5)));
    }

    public Claims verifyToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
