package com.sportsnap.web.views;

import com.sportsnap.web.service.ApiClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Route(value = "upload", layout = MainLayout.class)
@PageTitle("Upload de Fotos")
public class UploadFotosView extends VerticalLayout {

    public UploadFotosView(ApiClient apiClient) {
        Long userId = (Long) VaadinSession.getCurrent().getAttribute("userId");

        H2 titulo = new H2("Upload de Fotos");

        TextField sessionIdField = new TextField("ID da Session");
        sessionIdField.setWidth("400px");

        TextField spotIdField = new TextField("ID do Spot");
        spotIdField.setWidth("400px");

        TextArea fotosField = new TextArea("Caminhos das fotos (um por linha)");
        fotosField.setWidth("400px");
        fotosField.setHeight("150px");
        fotosField.setPlaceholder("foto1.jpg\nfoto2.jpg\nfoto3.jpg");

        Button uploadBtn = new Button("Criar Lote e Upload", e -> {
            try {
                Long sessionId = Long.parseLong(sessionIdField.getValue());
                Long spotId = Long.parseLong(spotIdField.getValue());

                Map<String, Object> lote = apiClient.criarLote(userId, sessionId, spotId);
                Long loteId = ((Number) lote.get("id")).longValue();

                List<String> caminhos = Arrays.asList(fotosField.getValue().split("\n"));
                caminhos = caminhos.stream().filter(c -> !c.isBlank()).toList();

                Map<String, Object> resultado = apiClient.uploadFotos(loteId, caminhos);
                Notification.show("Upload realizado! Lote #" + loteId + " — " +
                        resultado.get("totalFotos") + " fotos processadas");
            } catch (Exception ex) {
                Notification.show("Erro: " + ex.getMessage());
            }
        });
        uploadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        uploadBtn.getStyle().set("background-color", "#00FF7F").set("color", "#1a1a2e");

        add(titulo, sessionIdField, spotIdField, fotosField, uploadBtn);
        setPadding(true);
    }
}
