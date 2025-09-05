package br.com.galsystem.construction.finance.mapper;

import br.com.galsystem.construction.finance.dto.user.UserCreateDTO;
import br.com.galsystem.construction.finance.dto.user.UserDTO;
import br.com.galsystem.construction.finance.dto.user.UserUpdateDTO;
import br.com.galsystem.construction.finance.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User entity);

    User toEntity(UserCreateDTO dto);

    void updateEntity(@MappingTarget User entity, UserUpdateDTO dto);
}
