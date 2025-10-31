package com.scapuz.msa_clients.infrastructure.adapter.persistence.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.scapuz.msa_clients.domain.model.Client;
import com.scapuz.msa_clients.infrastructure.adapter.persistence.entity.ClientEntity;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientEntity toEntity(Client client);

    Client toDomain(ClientEntity entity);

    List<Client> toDomainList(List<ClientEntity> entities);

}
