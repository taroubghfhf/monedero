package co.edu.uniquindio.monedero.dominio.puerto.cliente.dao;

import co.edu.uniquindio.monedero.dominio.dto.cliente.ClienteDTO;

import org.springframework.stereotype.Component;


@Component
public interface ClienteDao {
    ClienteDTO buscarPorCedula(String cedula);
}
