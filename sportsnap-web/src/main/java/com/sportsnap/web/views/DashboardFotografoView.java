package com.sportsnap.web.views;

import com.sportsnap.web.service.ApiClient;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.List;
import java.util.Map;

@Route(value = "dashboard-fotografo", layout = MainLayout.class)
@PageTitle("Dashboard — Fotógrafo")
public class DashboardFotografoView extends VerticalLayout {

    public DashboardFotografoView(ApiClient apiClient) {
        Long userId = (Long) VaadinSession.getCurrent().getAttribute("userId");
        String userName = (String) VaadinSession.getCurrent().getAttribute("userName");

        H2 titulo = new H2("Dashboard");

        // Resumo financeiro
        VerticalLayout financeiroCard = new VerticalLayout();
        financeiroCard.getStyle()
                .set("background", "#2d2d44")
                .set("border-radius", "12px")
                .set("padding", "24px");

        H3 finTitulo = new H3("Resumo Financeiro");
        Span vendas = new Span("Vendas este mes: R$ 0,00");
        vendas.getStyle().set("font-size", "20px").set("color", "#00FF7F");

        financeiroCard.add(finTitulo, vendas, new Paragraph("Saldo disponivel: R$ 0,00"));

        // Lotes recentes
        VerticalLayout lotesCard = new VerticalLayout();
        lotesCard.getStyle()
                .set("background", "#2d2d44")
                .set("border-radius", "12px")
                .set("padding", "24px");

        H3 lotesTitulo = new H3("Lotes Recentes");
        List<Map<String, Object>> lotes = apiClient.listarLotes(userId);

        if (lotes.isEmpty()) {
            lotesCard.add(lotesTitulo, new Paragraph("Nenhum lote ainda. Va para Upload para criar seu primeiro lote."));
        } else {
            lotesCard.add(lotesTitulo);
            for (Map<String, Object> lote : lotes) {
                Paragraph p = new Paragraph("Lote #" + lote.get("id") + " — Session: " + lote.get("sessionId"));
                lotesCard.add(p);
            }
        }

        HorizontalLayout cards = new HorizontalLayout(financeiroCard, lotesCard);
        cards.setWidthFull();

        add(titulo, cards);
        setPadding(true);
    }
}
