package br.ce.wcaquino.servicos;

import br.ce.wcaquino.exceptions.NaoPodeDividirPorZeroException;

public class Calculadora {

    public Integer somar(Integer a, Integer b) {
        return a + b;
    }

    public Integer subtrair(int a, Integer b) {
        return  a - b;
    }

    public Integer divide(Integer a, Integer b) throws NaoPodeDividirPorZeroException {
        if (b== 0) {
            throw  new NaoPodeDividirPorZeroException();
        }
        return a / b;
    }
}
