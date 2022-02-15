package br.ce.wcaquino.servicos.matchers;

import br.ce.wcaquino.utils.DataUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DiaSemanaMatcher extends TypeSafeMatcher<Date> {

    private Integer diaSemana;

    public DiaSemanaMatcher(Integer diaSemana) {
        this.diaSemana = diaSemana;
    }

    @Override
    protected boolean matchesSafely(Date data) {
        return DataUtils.verificarDiaSemana(data, diaSemana);
    }

    @Override
    public void describeTo(Description desc) {
        Calendar data = Calendar.getInstance();
        data.set(Calendar.DAY_OF_WEEK, diaSemana);
        String dataExtenso = data.getDisplayName
                (Calendar.DAY_OF_WEEK, Calendar.LONG, new Locale("p", "BR"));
        desc.appendText(dataExtenso);
    }
}
