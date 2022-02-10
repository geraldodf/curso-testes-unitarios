package br.ce.wcaquino.servicos;

import br.ce.wcaquino.exceptions.NaoPodeDividirPorZeroException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CalculadoraTest {

    private Calculadora calc;

    @Before
    public void setup() {
        calc = new Calculadora();
    }

    @Test
    public void deveSomarDoisValores() {
        //cenario
        Integer a = 5;
        Integer b = 3;

        //acao
        Integer resultado = calc.somar(a, b);

        //verificacao
        Assert.assertEquals(8, resultado, 0.000);
    }

    @Test
    public void deveSubtrairDoisValores() {
        //cenario
        Integer a = 5;
        Integer b = 3;

        //acao
        Integer resultado = calc.subtrair(a, b);

        Assert.assertEquals(2, resultado, 0.000);
    }

    @Test
    public void deveDividirDoisValores() throws NaoPodeDividirPorZeroException {
        //cenario
        Integer a = 6;
        Integer b = 3;

        //acao
        Integer resultado = calc.divide(a, b);

        Assert.assertEquals(2, resultado, 0.000);
    }

    @Test(expected = NaoPodeDividirPorZeroException.class)
    public void deveLancarExcecaoAodividirPorZero() throws NaoPodeDividirPorZeroException {
        Integer a = 10;
        Integer b = 0;
        Calculadora calc = new Calculadora();

        calc.divide(a, b);
    }
}
