package com.polstat.penyediaanserver.dto;

import java.util.Collection;
import java.util.Collections;
import com.polstat.penyediaanserver.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto implements UserDetails {
    @Schema(example = "1")
    private Long id;

    @Schema(example = "Rehan")
    private String name;

    @Schema(example = "rehan@stis.ac.id")
    private String email;

    @Schema(example = "$2a$10$.46RUm/kWmgL0kBIBknPAuceDY0wEi1jyjeGVu9nvGZZxSWSGUda6")
    private String password;

    @Schema(example = "MAHASISWA")
    private Role role;

    @Schema(example = "[{ \"authority\": \"ROLE_MAHASISWA\" }]")
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
