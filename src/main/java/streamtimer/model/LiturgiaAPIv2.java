package streamtimer.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LiturgiaAPIv2 {

    private Leitura leituras;

    public LiturgiaAPIv2() {
    }

    public LiturgiaAPIv2(Leitura leituras) {
        this.leituras = leituras;
    }

    public Leitura getLeituras() {
        return leituras;
    }

    public void setLeituras(Leitura leituras) {
        this.leituras = leituras;
    }

    public static class Leitura {
        private static final List<String> ACCEPTED_EXTRA_TYPES = Arrays.asList("Terceira Leitura", "Quarta Leitura", "Quinta Leitura",
                "Sexta Leitura", "Sétima Leitura", "Epístola");

        private List<Texto> primeiraLeitura;
        private List<Texto> salmo;
        private List<Texto> segundaLeitura;
        private List<Texto> evangelho;
        private List<Texto> extras;

        public Leitura() {
        }

        public String getPrimeiraLeitura() {
            return primeiraLeitura.isEmpty() ? "" : formatReference(primeiraLeitura.get(0).getReferencia());
        }

        public void setPrimeiraLeitura(List<Texto> primeiraLeitura) {
            this.primeiraLeitura = primeiraLeitura;
        }

        public String getSalmo() {
            return salmo.isEmpty() ? "" : salmo.get(0).getReferencia();
        }

        public String getSalmoResposta() {
            return salmo.isEmpty() ? "" : salmo.get(0).getRefrao();
        }

        public void setSalmo(List<Texto> salmo) {
            this.salmo = salmo;
        }

        public String getSegundaLeitura() {
            return segundaLeitura.isEmpty() ? "" : formatReference(segundaLeitura.get(0).getReferencia());
        }

        public void setSegundaLeitura(List<Texto> segundaLeitura) {
            this.segundaLeitura = segundaLeitura;
        }

        public String getEvangelho() {
            return evangelho.isEmpty() ? "" : formatReference(evangelho.get(0).getReferencia());
        }

        public void setEvangelho(List<Texto> evangelho) {
            this.evangelho = evangelho;
        }

        public String getExtras() {
            return extras.stream().filter(texto -> ACCEPTED_EXTRA_TYPES.stream().anyMatch(extraType -> extraType.equals(texto.getTipo())))
                    .map(texto -> formatReference(texto.getReferencia()))
                    .collect(Collectors.joining(";"));
        }

        public void setExtras(List<Texto> extras) {
            this.extras = extras;
        }

        public String getAleluiaLeituras() {
            return formatReference(primeiraLeitura.get(0).getReferencia())
                    .concat(";")
                    .concat(formatReference(segundaLeitura.get(0).getReferencia()))
                    .concat(";")
                    .concat(getExtras());
        }

        public String getAleluiaSalmos() {
            return salmo.stream().map(Texto::getReferencia).collect(Collectors.joining(";"));
        }

        public String getAleluiaSalmosRespostas() {
            return salmo.stream().map(Texto::getRefrao).collect(Collectors.joining(";"));
        }

        private String formatReference(String reference) {
            return reference.split("\\(Forma")[0].trim();
        }
    }

    public static class Texto {
        private String referencia;
        private String refrao;
        private String tipo;

        public String getReferencia() {
            return referencia;
        }

        public void setReferencia(String referencia) {
            this.referencia = referencia;
        }

        public String getRefrao() {
            return refrao;
        }

        public void setRefrao(String refrao) {
            this.refrao = refrao;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }
    }

}