package br.ce.wcaquino.servicos;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;
import junit.framework.TestCase;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import java.util.Date;

import static org.hamcrest.core.Is.is;

public class LocacaoServiceTest extends TestCase {

    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup(){
        System.out.println("Before");
    }

    @After
    public void tearDown(){
        System.out.println("After");
    }

    @Test
    public void testeLocacao() throws Exception {
        //cenario
        LocacaoService service = new LocacaoService();
        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 1", 0, 5.0);

        //acao
        Locacao locacao = service.alugarFilme(usuario, filme);

        //verificacao
        error.checkThat(locacao.getValor(), CoreMatchers.is(CoreMatchers.equalTo(5.0)));
        error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), is(true));
    }

    @Test(expected = FilmeSemEstoqueException.class)
    public void testLocacao_filmeSemEstoque() throws Exception {
        //cenario
        LocacaoService service = new LocacaoService();
        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 2", 0, 4.0);

        //acao
        service.alugarFilme(usuario, filme);
    }

    @Test
    public void testLocacao_usuarioVazioa() throws FilmeSemEstoqueException {
        //cenario
        LocacaoService service = new LocacaoService();
        Filme filme = new Filme("Filme 2", 2, 4.0);

        //acao
        try {
            service.alugarFilme(null, filme);
            Assert.fail();
        } catch (LocadoraException e) {
            Assert.assertThat(e.getMessage(), is("Usuario vazio"));
        }
    }

    @Test
    public void testLocacao_FilmeVazia() throws FilmeSemEstoqueException, LocadoraException {
        //cenario
        LocacaoService service = new LocacaoService();
        Usuario usuario = new Usuario("Usuario 1");


        exception.expect(LocadoraException.class);
        exception.expectMessage("Filme vazio");

        //acao
        service.alugarFilme(usuario, null);

    }


}