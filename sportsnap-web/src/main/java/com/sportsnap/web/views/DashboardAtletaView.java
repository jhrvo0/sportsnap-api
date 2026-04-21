package com.sportsnap.web.views;

import com.sportsnap.web.service.ApiClient;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.Map;

@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard — Atleta")
public class DashboardAtletaView extends VerticalLayout {

    public DashboardAtletaView(ApiClient apiClient) {
        Long userId = (Long) VaadinSession.getCurrent().getAttribute("userId");
        String userName = (String) VaadinSession.getCurrent().getAttribute("userName");

        H2 titulo = new H2("Dashboard");

        // Carta Oficial
        VerticalLayout cartaCard = new VerticalLayout();
        cartaCard.getStyle()
                .set("background", "#2d2d44")
                .set("border-radius", "16px")
                .set("padding", "24px")
                .set("border", "2px solid #00FF7F");

        H3 cartaTitulo = new H3(userName != null ? userName : "Atleta");
        cartaTitulo.getStyle().set("color", "#00FF7F");

        Map<String, Object> carta = apiClient.buscarCarta(userId);
        String overallStr = carta != null && carta.get("overall") != null
                ? String.valueOf(((Number) carta.get("overall")).intValue()) : "0";

        Span overall = new Span("Overall: " + overallStr);
        overall.getStyle().set("font-size", "32px").set("font-weight", "bold").set("color", "#00FF7F");

        cartaCard.add(cartaTitulo, overall);

        // Shadow Stats
        VerticalLayout shadowCard = new VerticalLayout();
        shadowCard.getStyle()
                .set("background", "#2d2d44")
                .set("border-radius", "12px")
                .set("padding", "16px");

        H3 shadowTitulo = new H3("Shadow Stats");
        Map<String, Object> status = apiClient.buscarStatusPotencial(userId);
        String xp = status != null && status.get("xpAcumulado") != null
                ? String.valueOf(((Number) status.get("xpAcumulado")).intValue()) : "0";
        String streak = status != null && status.get("streakDeConsistencia") != null
                ? String.valueOf(status.get("streakDeConsistencia")) : "0";

        shadowCard.add(shadowTitulo,
                new Paragraph("XP Acumulado: " + xp),
                new Paragraph("Streak: " + streak + " dias"));

        // Botao Sincronizar
        Button sincronizarBtn = new Button("Sincronizar Carta (Reveal)", e -> {
            try {
                Map<String, Object> resultado = apiClient.sincronizarCarta(userId);
                if (resultado != null && resultado.containsKey("overall")) {
                    Notification.show("Sincronizacao realizada! Novo Overall: " + resultado.get("overall"));
                    getUI().ifPresent(ui -> ui.getPage().reload());
                } else if (resultado != null && resultado.containsKey("erro")) {
                    Notification.show("Erro: " + resultado.get("erro"));
                }
            } catch (Exception ex) {
                Notification.show("Erro ao sincronizar: " + ex.getMessage());
            }
        });
        sincronizarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sincronizarBtn.getStyle().set("background-color", "#00FF7F").set("color", "#1a1a2e");

        HorizontalLayout cards = new HorizontalLayout(cartaCard, shadowCard);
        cards.setWidthFull();

        add(titulo, cards, sincronizarBtn);
        setSpacing(true);
        setPadding(true);
    }
}
