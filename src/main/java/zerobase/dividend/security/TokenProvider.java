package zerobase.dividend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import zerobase.dividend.service.MemberService;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

import static io.jsonwebtoken.Jwts.SIG.HS512;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;
    private final MemberService memberService;

    private static final long EXPIRATION_TIME = 60 * 60 * 1000; // 1시간

    public SecretKey getKey() {
        log.info("getKey");
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(String username, List<String> roles) {
        log.info("Generating token for user: {}", username);
        long millis = System.currentTimeMillis();
        Date now = new Date(millis);
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles) // 토큰에 담을 claim 정보
                .issuedAt(now) // 토큰 생성 시간
                .expiration(expiration) // 토큰 만료 시간
                .signWith(getKey(), HS512) // 비밀키로 서명
                .compact();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = memberService.loadUserByUsername(
                getUsername(token));

        return new UsernamePasswordAuthenticationToken(
                userDetails, "", userDetails.getAuthorities()
        );
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) return false;

        Claims claims = parseClaims(token);
        return !claims.getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        log.info("claims: {}", claims);
        return claims;
    }
}
