package com.sportsnap.web.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.sportsnap.web.service.ApiClient;

@Route("")
@PageTitle("SportSnap — Login")
public class LoginView extends VerticalLayout {

    private final ApiClient apiClient;
    private String tipoSelecionado = null;

    public LoginView(ApiClient apiClient) {
        this.apiClient = apiClient;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H1 logo = new H1("SportSnap");
        logo.getStyle().set("color", "#00FF7F");

        Paragraph subtitle = new Paragraph("Performance esportiva + Fotografia profissional");

        H3 escolha = new H3("Escolha seu perfil:");

        Button atletaBtn = new Button("Sou Atleta", e -> {
            tipoSelecionado = "ATLETA";
            e.getSource().addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        });
        atletaBtn.setWidth("200px");

        Button fotografoBtn = new Button("Sou Fotógrafo", e -> {
            tipoSelecionado = "FOTOGRAFO";
            e.getSource().addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        });
        fotografoBtn.setWidth("200px");

        HorizontalLayout tipoLayout = new HorizontalLayout(atletaBtn, fotografoBtn);

        TextField nomeField = new TextField("Nome");
        nomeField.setWidth("400px");

        EmailField emailField = new EmailField("Email");
        emailField.setWidth("400px");

        Button entrarBtn = new Button("Entrar / Cadastrar", e -> {
            if (tipoSelecionado == null) {
                Notification.show("Selecione se voce e Atleta ou Fotografo");
                return;
            }
            if (nomeField.isEmpty() || emailField.isEmpty()) {
                Notification.show("Preencha nome e email");
                return;
            }

            try {
                java.util.Map<String, Object> usuario;
                if ("ATLETA".equals(tipoSelecionado)) {
                    usuario = apiClient.criarAtleta(nomeField.getValue(), emailField.getValue());
                } else {
                    usuario = apiClient.criarFotografo(nomeField.getValue(), emailField.getValue());
                }

                Long userId = usuario != null && usuario.get("id") != null
                        ? ((Number) usuario.get("id")).longValue() : 1L;

                VaadinSession.getCurrent().setAttribute("tipoUsuario", tipoSelecionado);
                VaadinSession.getCurrent().setAttribute("userId", userId);
                VaadinSession.getCurrent().setAttribute("userName", nomeField.getValue());

                if ("ATLETA".equals(tipoSelecionado)) {
                    getUI().ifPresent(ui -> ui.navigate(DashboardAtletaView.class));
                } else {
                    getUI().ifPresent(ui -> ui.navigate(DashboardFotografoView.class));
                }
            } catch (Exception ex) {
                VaadinSession.getCurrent().setAttribute("tipoUsuario", tipoSelecionado);
                VaadinSession.getCurrent().setAttribute("userId", 1L);
                VaadinSession.getCurrent().setAttribute("userName", nomeField.getValue());

                if ("ATLETA".equals(tipoSelecionado)) {
                    getUI().ifPresent(ui -> ui.navigate(DashboardAtletaView.class));
                } else {
                    getUI().ifPresent(ui -> ui.navigate(DashboardFotografoView.class));
                }
            }
        });
        entrarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        entrarBtn.setWidth("400px");

        add(logo, subtitle, escolha, tipoLayout, nomeField, emailField, entrarBtn);
    }
}
