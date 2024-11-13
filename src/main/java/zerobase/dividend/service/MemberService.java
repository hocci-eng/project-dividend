package zerobase.dividend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import zerobase.dividend.exception.impl.AlreadyExistUserException;
import zerobase.dividend.exception.impl.NoUserException;
import zerobase.dividend.exception.impl.NotMatchedPasswordException;
import zerobase.dividend.model.Auth;
import zerobase.dividend.model.MemberEntity;
import zerobase.dividend.persist.MemberRepository;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("couldn't find user -> " + username));
    }

    public MemberEntity register(Auth.SignUp member) {
        boolean exists = memberRepository.existsByUsername(member.getUsername());

        if (exists) {
            throw new AlreadyExistUserException();
        }

        member.setPassword(passwordEncoder.encode(member.getPassword()));

        return memberRepository.save(member.toEntity());
    }

    public MemberEntity authenticate(Auth.SignIn member) {
        MemberEntity user = memberRepository.findByUsername(member.getUsername())
                .orElseThrow(NoUserException::new);

        if (!passwordEncoder.matches(member.getPassword(), user.getPassword())) {
            throw new NotMatchedPasswordException();
        }

        return user;
    }
}
