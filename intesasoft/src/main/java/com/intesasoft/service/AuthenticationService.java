package com.intesasoft.service;

import com.intesasoft.exception.UserNotCreateException;
import com.intesasoft.model.User;
import com.intesasoft.repository.UserRepository;
import com.intesasoft.request.UserLoginRequest;
import com.intesasoft.request.UserRequest;
import com.intesasoft.response.UserResponse;
import com.intesasoft.response.UserTokenResponse;
import com.intesasoft.util.LoggerHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;

    public UserTokenResponse save(UserRequest userDto) {
        passwordControl(userDto.getPassword());
        emailControl(userDto.getEmail());

        User user =
                User.builder()
                        .email(userDto.getEmail().toLowerCase())
                        .password(userDto.getPassword())
                        .name(userDto.getName())
                        .role(userDto.getRole()).build();


        String encodedPassword = new BCryptPasswordEncoder().encode(userDto.getPassword());
        user.setPassword(encodedPassword);


        try {
            userRepository.save(user);

            LoggerHandler.getLogger().log(Level.INFO,
                    "AuthenticationService -->save()--> userRepository'e yeni kullanıcı kayıt edildi.");


            var token = jwtService.generateToken(user);
            return new UserTokenResponse(token);


        } catch (Exception e) {
            LoggerHandler.getLogger().log(Level.WARNING,
                    "AuthenticationService -->save()--> Bu email adresi zaten kayıtlı!");
            throw new UserNotCreateException("Bu email adresi zaten kayıtlı!");
        }
    }

    public List<UserResponse> getList() {
        return convert(userRepository.findAll());
    }

    private List<UserResponse> convert(List<User> all) {
        List<UserResponse> responseList = new ArrayList<>();
        all.stream().forEach(s -> {
            responseList.add(convert(s));
        });
        return responseList;
    }

    private UserResponse convert(User all) {
        UserResponse userResponse = new UserResponse();
        userResponse.setEmail(all.getEmail());
        userResponse.setName(all.getName());
        return userResponse;
    }

    private void emailControl(String email) {
        boolean isControl = Pattern.compile("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\" +
                        ".[A-Za-z0-9-]+)*(\\" +
                        ".[A-Za-z]{2,})$")
                .matcher(email)
                .matches();
        if (!isControl) {
            LoggerHandler.getLogger().log(Level.WARNING,
                    "AuthenticationService -->save()--> Geçerli bir email girilmedi.");

            throw new UserNotCreateException("Geçerli bir email girilmedi. Geçersiz durumlar:\nYerel kısmın başında " +
                    "ve sonunda noktaya izin verilmez.\n" +
                    "Ardışık noktalara izin verilmez.\n" +
                    "Yerel kısım için maksimum 64 karaktere izin verilir.\n" +
                    "Tire \"-\" ve nokta \".\" etki alanı bölümünün başında ve sonunda izin verilmez.\n" +
                    "Ardışık noktaya izin verilmez.");
        }
    }

    private void passwordControl(String password) {
        boolean isControl = password.length() > 7 && password.length() < 17;

        if (!isControl) {
            LoggerHandler.getLogger().log(Level.WARNING,
                    "AuthenticationService -->save()--> Şifre en az 8 en fazla 16 karakterden oluşabilir.");
            throw new UserNotCreateException("Şifre en az 8 en fazla 16 karakterden oluşabilir.");
        }
    }

    public UserResponse getUserResponse() {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow();
        UserResponse response = new UserResponse();
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        return response;
    }

    public UserTokenResponse login(UserLoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword())
        );

        UserDetails user = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(user);
        return new UserTokenResponse(accessToken);


    }
}
