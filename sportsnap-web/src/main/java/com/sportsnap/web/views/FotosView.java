package com.sportsnap.web.views;

import com.sportsnap.web.service.ApiClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.List;
import java.util.Map;

@Route(value = "fotos", layout = MainLayout.class)
@PageTitle("Fotos Sugeridas")
public class FotosView extends VerticalLayout {

    public FotosView(ApiClient apiClient) {
        Long userId = (Long) VaadinSession.getCurrent().getAttribute("userId");

        H2 titulo = new H2("Fotos Sugeridas");

        List<Map<String, Object>> licencas = apiClient.listarLicencas(userId);

        if (licencas.isEmpty()) {
            add(titulo, new Paragraph("Nenhuma foto sugerida no momento. Faca check-in em uma sessao primeiro."));
        } else {
            HorizontalLayout grid = new HorizontalLayout();
            grid.setFlexWrap(HorizontalLayout.FlexWrap.WRAP);

            for (Map<String, Object> licenca : licencas) {
                VerticalLayout card = new VerticalLayout();
                card.getStyle()
                        .set("background", "#2d2d44")
                        .set("border-radius", "12px")
                        .set("padding", "16px")
                        .set("width", "250px");

                card.add(
                        new Paragraph("Licenca #" + licenca.get("id")),
                        new Paragraph("Preco: R$ " + licenca.get("preco")),
                        new Paragraph("Adquirida em: " + licenca.get("adquiridaEm"))
                );
                grid.add(card);
            }
            add(titulo, grid);
        }

        // Secao para comprar nova licenca
        Button comprarBtn = new Button("Comprar Licenca (Foto ID)", e -> {
            Notification.show("Use o endpoint POST /api/marketplace/fotos/{id}/comprar para comprar uma licenca");
        });
        comprarBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        add(comprarBtn);
        setPadding(true);
    }
}
