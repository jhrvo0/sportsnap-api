package com.sportsnap.web.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route(value = "perfil", layout = MainLayout.class)
@PageTitle("Perfil")
public class PerfilView extends VerticalLayout {

    public PerfilView() {
        String userName = (String) VaadinSession.getCurrent().getAttribute("userName");
        String tipoUsuario = (String) VaadinSession.getCurrent().getAttribute("tipoUsuario");
        Long userId = (Long) VaadinSession.getCurrent().getAttribute("userId");

        H2 titulo = new H2("Perfil");

        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background", "#2d2d44")
                .set("border-radius", "12px")
                .set("padding", "24px")
                .set("max-width", "500px");

        H3 nomeLabel = new H3(userName != null ? userName : "Usuario");
        Paragraph tipoLabel = new Paragraph("Tipo: " + (tipoUsuario != null ? tipoUsuario : "N/A"));
        Paragraph idLabel = new Paragraph("ID: " + (userId != null ? userId : "N/A"));

        TextField nomeField = new TextField("Nome");
        nomeField.setValue(userName != null ? userName : "");
        nomeField.setWidthFull();

        EmailField emailField = new EmailField("Email");
        emailField.setWidthFull();

        Button salvarBtn = new Button("Salvar Alteracoes", e -> {
            VaadinSession.getCurrent().setAttribute("userName", nomeField.getValue());
            Notification.show("Perfil atualizado com sucesso!");
        });
        salvarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        card.add(nomeLabel, tipoLabel, idLabel, nomeField, emailField, salvarBtn);

        add(titulo, card);
        setPadding(true);
    }
}
