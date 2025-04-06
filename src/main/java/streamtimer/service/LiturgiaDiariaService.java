package streamtimer.service;

import streamtimer.model.LiturgiaDiaria;

import java.time.LocalDate;

public interface LiturgiaDiariaService {

    LiturgiaDiaria getLiturgiaDiaria(LocalDate dataLiturgia);

}
