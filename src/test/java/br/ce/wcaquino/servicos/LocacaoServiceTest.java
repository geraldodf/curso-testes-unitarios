package br.ce.wcaquino.servicos;


import static br.ce.wcaquino.utils.DataUtils.isMesmaData;
import static br.ce.wcaquino.utils.DataUtils.obterDataComDiferencaDias;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.ce.wcaquino.buiders.FilmeBuilder;
import br.ce.wcaquino.buiders.LocacaoBuilder;
import br.ce.wcaquino.buiders.UsuarioBuilder;
import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.matchers.MatchersProprios;
import org.hamcrest.MatcherAssert;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({LocacaoService.class})
public class LocacaoServiceTest {

    @InjectMocks
    private LocacaoService service;

    @Mock
    private SPCService spc;
    @Mock
    private LocacaoDAO dao;
    @Mock
    private EmailService email;

    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void deveAlugarFilme() throws Exception {
        //cenario
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().comValor(5.0).agora());

        PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(3, 3, 2022));

        //acao
        Locacao locacao = service.alugarFilme(usuario, filmes);

        //verificacao
        error.checkThat(locacao.getValor(), is(equalTo(5.0)));
        error.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));
        error.checkThat(DataUtils.isMesmaData
                (locacao.getDataLocacao(), DataUtils.obterData(3, 3, 2022)), is(true));
        error.checkThat(DataUtils.isMesmaData
                (locacao.getDataRetorno(), DataUtils.obterData(4, 3, 2022)), is(true));
    }

    @Test(expected = FilmeSemEstoqueException.class)
    public void naoDeveAlugarFilmeSemEstoque() throws Exception {
        //cenario
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilmeSemEstoque().agora());

        //acao
        service.alugarFilme(usuario, filmes);
    }

    @Test
    public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {
        //cenario
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

        //acao
        try {
            service.alugarFilme(null, filmes);
            Assert.fail();
        } catch (LocadoraException e) {
            assertThat(e.getMessage(), is("Usuario vazio"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void naoDeveAlugarFilmeSemFilme() throws Exception {
        //cenario
        Usuario usuario = UsuarioBuilder.umUsuario().agora();

        exception.expect(LocadoraException.class);
        exception.expectMessage("Filme vazio");

        //acao
        service.alugarFilme(usuario, null);
    }

    @Test
    public void deveDevolverNaSegundaAoAlugarNoSabado() throws Exception {
        //cenario
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

//      PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(5, 3, 2022));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 03);
        calendar.set(Calendar.MONTH, Calendar.APRIL);
        calendar.set(Calendar.YEAR, 2022);
        PowerMockito.mockStatic(Calendar.class);
        PowerMockito.when(Calendar.getInstance()).thenReturn(calendar);

        //acao
        Locacao retorno = service.alugarFilme(usuario, filmes);

        //verificacao
        boolean ehSegunda = DataUtils.verificarDiaSemana(retorno.getDataRetorno(), Calendar.MONDAY);
        Assert.assertTrue(ehSegunda);

        PowerMockito.verifyStatic(Mockito.times(2));
        Calendar.getInstance();
    }

    @Test
    public void naoDeveALugarFilmeParaNegativadoSPC() throws Exception {
        //cenario
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

        when(spc.possuiNegativacao(Mockito.any(Usuario.class))).thenReturn(true);

        //acao
        try {
            service.alugarFilme(usuario, filmes);
            Assert.fail();
            //verificacao
        } catch (FilmeSemEstoqueException e) {
            e.printStackTrace();
        } catch (LocadoraException e) {
            Assert.assertEquals("Usuário negativado", e.getMessage());
        }


        Mockito.verify(spc).possuiNegativacao(usuario);
    }

    @Test
    public void deveEnviarEmailParaLocacoesAtrasadas() {
        //cenario
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        Usuario usuario2 = UsuarioBuilder.umUsuario().comNome("Usuario em dia").agora();
        Usuario usuario3 = UsuarioBuilder.umUsuario().comNome("Outro atrasado").agora();
        List<Locacao> locacoes = Arrays.asList(LocacaoBuilder.umLocacao().atrasado().comUsuario(usuario).agora(),
                LocacaoBuilder.umLocacao().comUsuario(usuario2).agora(),
                LocacaoBuilder.umLocacao().atrasado().comUsuario(usuario3).agora(),
                LocacaoBuilder.umLocacao().atrasado().comUsuario(usuario3).agora()
        );
        when(dao.obterLocacoesPendentes()).thenReturn(locacoes);

        //acao
        service.notificarAtrasos();

        //verificacao
        Mockito.verify(email, Mockito.times(3)).notificarAtraso(Mockito.any(Usuario.class));
        Mockito.verify(email).notificarAtraso(usuario);
        Mockito.verify(email, Mockito.atLeastOnce()).notificarAtraso(usuario3);
        Mockito.verify(email, Mockito.never()).notificarAtraso(usuario2);
        Mockito.verifyNoMoreInteractions(email);
    }

    @Test
    public void deveTratarErroNoSPC() throws Exception {
        //cenario
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

        when(spc.possuiNegativacao(usuario)).thenThrow(new Exception("Falha catastrófica"));

        //verificacao
        exception.expect(LocadoraException.class);
        exception.expectMessage("Problemas com SPC, tente novamente");

        //acao
        service.alugarFilme(usuario, filmes);
    }

    @Test
    public void deveProrrogarUmaLocacao() {
        //cenario
        Locacao locacao = LocacaoBuilder.umLocacao().agora();

        //acao
        service.prorrogarLocacao(locacao, 3);

        //verificacao
        ArgumentCaptor<Locacao> argCapt = ArgumentCaptor.forClass(Locacao.class);
        Mockito.verify(dao).salvar(argCapt.capture());
        Locacao locacaoRetornada = argCapt.getValue();

        error.checkThat(locacaoRetornada.getValor(), is(12.0));
        error.checkThat(locacaoRetornada.getDataLocacao(), is(MatchersProprios.ehHoje()));
        error.checkThat(locacaoRetornada.getDataRetorno(), is(MatchersProprios.ehHojeComDiferencaDias(1)));

    }
}
