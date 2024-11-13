package zerobase.dividend.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.dividend.model.Auth;
import zerobase.dividend.model.MemberEntity;
import zerobase.dividend.security.TokenProvider;
import zerobase.dividend.service.MemberService;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request) {
        MemberEntity result = memberService.register(request);
        log.info("Signup result: {}", result);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request) {
        MemberEntity member = memberService.authenticate(request);

        log.info("user login ->" + request.getUsername());
        return ResponseEntity.ok(
                tokenProvider.generateToken(
                        member.getUsername(), member.getRoles()
                )
        );
    }

}
