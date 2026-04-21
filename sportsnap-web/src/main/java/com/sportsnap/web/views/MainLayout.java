package com.sportsnap.web.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;

public class MainLayout extends AppLayout {

    public MainLayout() {
        String tipoUsuario = (String) VaadinSession.getCurrent().getAttribute("tipoUsuario");
        if (tipoUsuario == null) {
            tipoUsuario = "ATLETA";
        }

        createHeader(tipoUsuario);
        createDrawer(tipoUsuario);
    }

    private void createHeader(String tipoUsuario) {
        H2 logo = new H2("SportSnap");
        logo.getStyle().set("color", "#00FF7F").set("margin", "0");

        Span tipo = new Span(tipoUsuario.equals("ATLETA") ? "Atleta" : "Fotógrafo");
        tipo.getStyle().set("color", "#888").set("font-size", "14px");

        VerticalLayout branding = new VerticalLayout(logo, tipo);
        branding.setSpacing(false);
        branding.setPadding(false);

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), branding);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.getStyle().set("padding", "0 16px");

        addToNavbar(header);
    }

    private void createDrawer(String tipoUsuario) {
        SideNav nav = new SideNav();

        if ("FOTOGRAFO".equals(tipoUsuario)) {
            nav.addItem(new SideNavItem("Dashboard", DashboardFotografoView.class, VaadinIcon.HOME.create()));
            nav.addItem(new SideNavItem("Upload", UploadFotosView.class, VaadinIcon.UPLOAD.create()));
            nav.addItem(new SideNavItem("Meus Lotes", MeusLotesView.class, VaadinIcon.FOLDER.create()));
            nav.addItem(new SideNavItem("Perfil", PerfilView.class, VaadinIcon.USER.create()));
        } else {
            nav.addItem(new SideNavItem("Dashboard", DashboardAtletaView.class, VaadinIcon.HOME.create()));
            nav.addItem(new SideNavItem("Sessões", SessoesView.class, VaadinIcon.CALENDAR.create()));
            nav.addItem(new SideNavItem("Fotos", FotosView.class, VaadinIcon.PICTURE.create()));
            nav.addItem(new SideNavItem("Ranking", RankingView.class, VaadinIcon.TROPHY.create()));
            nav.addItem(new SideNavItem("Perfil", PerfilView.class, VaadinIcon.USER.create()));
        }

        Button sairBtn = new Button("Sair", VaadinIcon.SIGN_OUT.create(), e -> {
            VaadinSession.getCurrent().setAttribute("tipoUsuario", null);
            VaadinSession.getCurrent().setAttribute("userId", null);
            e.getSource().getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        });
        sairBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        sairBtn.setWidthFull();

        VerticalLayout drawerContent = new VerticalLayout(nav);
        drawerContent.setSizeFull();
        drawerContent.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        VerticalLayout bottomSection = new VerticalLayout(sairBtn);
        bottomSection.setPadding(false);

        VerticalLayout drawer = new VerticalLayout(nav, bottomSection);
        drawer.setSizeFull();
        drawer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        addToDrawer(drawer);
    }
}
