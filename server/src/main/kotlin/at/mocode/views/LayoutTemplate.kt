package at.mocode.views

import kotlinx.html.*

/**
 * Common layout template for all pages in the application.
 * Provides consistent styling, header, footer, and responsive design.
 */
class LayoutTemplate {
    /**
     * Applies the common layout template to the provided content.
     * @param title The page title
     * @param showNavbar Whether to show the navigation bar
     * @param showAdminLink Whether to show the admin link in the navbar
     * @param content The content builder function
     */
    fun HTML.applyLayout(
        title: String,
        showNavbar: Boolean = true,
        showAdminLink: Boolean = true,
        content: FlowContent.() -> Unit
    ) {
        head {
            meta(charset = "UTF-8")
            meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
            title { +title }
            link(rel = "stylesheet", href = "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css")
            link(rel = "stylesheet", href = "https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=Roboto:wght@300;400;500;700&display=swap")
            link(rel = "stylesheet", href = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css")
            link(rel = "stylesheet", href = "https://cdn.jsdelivr.net/npm/aos@2.3.4/dist/aos.css")
            link(rel = "stylesheet", href = "/css/main.css")
            script(src = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js") {}
            script(src = "https://cdn.jsdelivr.net/npm/aos@2.3.4/dist/aos.js") {}
            script {
                unsafe {
                    +"""
                    document.addEventListener('DOMContentLoaded', function() {
                        // Initialize Bootstrap tooltips
                        const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
                        const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));

                        // Initialize Bootstrap popovers
                        const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
                        const popoverList = [...popoverTriggerList].map(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl));

                        // Add Bootstrap validation classes to forms
                        const forms = document.querySelectorAll('.needs-validation');
                        Array.from(forms).forEach(form => {
                            form.addEventListener('submit', event => {
                                if (!form.checkValidity()) {
                                    event.preventDefault();
                                    event.stopPropagation();
                                }
                                form.classList.add('was-validated');
                            }, false);
                        });

                        // Initialize AOS (Animate On Scroll)
                        AOS.init({
                            duration: 800,
                            easing: 'ease-in-out',
                            once: true
                        });

                        // Navbar scroll effect
                        const navbar = document.querySelector('.navbar');
                        if (navbar) {
                            window.addEventListener('scroll', function() {
                                if (window.scrollY > 50) {
                                    navbar.classList.add('navbar-scrolled');
                                } else {
                                    navbar.classList.remove('navbar-scrolled');
                                }
                            });
                        }

                        // Add ripple effect to buttons
                        const buttons = document.querySelectorAll('.button, .btn');
                        buttons.forEach(button => {
                            button.addEventListener('click', function(e) {
                                const x = e.clientX - e.target.getBoundingClientRect().left;
                                const y = e.clientY - e.target.getBoundingClientRect().top;

                                const ripple = document.createElement('span');
                                ripple.classList.add('ripple-effect');
                                ripple.style.left = x + 'px';
                                ripple.style.top = y + 'px';

                                this.appendChild(ripple);

                                setTimeout(() => {
                                    ripple.remove();
                                }, 600);
                            });
                        });
                    });
                    """
                }
            }
        }
        body {
            if (showNavbar) {
                nav(classes = "navbar navbar-expand-lg navbar-dark fixed-top") {
                    div("container") {
                        a(href = "/", classes = "navbar-brand") {
                            i("fas fa-horse-head") {}
                            +"Meldestelle Portal"
                        }
                        button(classes = "navbar-toggler") {
                            type = ButtonType.button
                            attributes["data-bs-toggle"] = "collapse"
                            attributes["data-bs-target"] = "#navbarContent"
                            attributes["aria-controls"] = "navbarContent"
                            attributes["aria-expanded"] = "false"
                            attributes["aria-label"] = "Toggle navigation"
                            span(classes = "navbar-toggler-icon") {}
                        }

                        div(classes = "collapse navbar-collapse") {
                            id = "navbarContent"
                            ul(classes = "navbar-nav ms-auto mb-2 mb-lg-0") {
                                li(classes = "nav-item") {
                                    a(href = "/", classes = "nav-link active") {
                                        i("fas fa-home") {}
                                        +"Home"
                                    }
                                }
                                if (showAdminLink) {
                                    li(classes = "nav-item") {
                                        a(href = "/admin/tournaments", classes = "nav-link") {
                                            i("fas fa-calendar-alt") {}
                                            +"Turnierverwaltung"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Add padding to account for fixed navbar
            div(classes = "navbar-spacer") {}

            main(classes = "py-5") {
                div("container") {
                    div(classes = "card shadow") {
                        attributes["data-aos"] = "fade-up"
                        attributes["data-aos-delay"] = "100"
                        div("card-body") {
                            content()
                        }
                    }
                }
            }
            footer(classes = "footer mt-5") {
                attributes["data-aos"] = "fade-up"
                attributes["data-aos-delay"] = "200"
                div("container") {
                    div("footer-content") {
                        div("row gy-4") {
                            div("col-lg-4 col-md-6") {
                                div("footer-info") {
                                    h3(classes = "gradient-text") { +"Meldestelle Portal" }
                                    p {
                                        +"Ihre zentrale Plattform für Turnierorganisation und Anmeldungen."
                                    }
                                    div("social-links mt-3") {
                                        a(href = "#", classes = "facebook") { i("fab fa-facebook-f") {} }
                                        a(href = "#", classes = "twitter") { i("fab fa-twitter") {} }
                                        a(href = "#", classes = "instagram") { i("fab fa-instagram") {} }
                                        a(href = "#", classes = "linkedin") { i("fab fa-linkedin-in") {} }
                                    }
                                }
                            }
                            div("col-lg-4 col-md-6") {
                                div("footer-links") {
                                    h4 { +"Nützliche Links" }
                                    ul {
                                        li { a(href = "/") { +"Home" } }
                                        li { a(href = "#") { +"Über uns" } }
                                        li { a(href = "#") { +"Turniere" } }
                                        li { a(href = "#") { +"Kontakt" } }
                                    }
                                }
                            }
                            div("col-lg-4 col-md-6") {
                                div("footer-contact") {
                                    h4 { +"Kontakt" }
                                    p {
                                        i("fas fa-envelope me-2") {}
                                        +"info@meldestelle-portal.at"
                                    }
                                    p {
                                        i("fas fa-phone me-2") {}
                                        +"+43 123 456 789"
                                    }
                                }
                            }
                        }
                    }
                    div("footer-legal text-center") {
                        div("copyright") {
                            +"© ${java.time.Year.now().value} "
                            strong { +"Meldestelle Portal" }
                            +". Alle Rechte vorbehalten."
                        }
                        div("credits") {
                            +"Entwickelt von "
                            a(href = "#") { +"mocode" }
                        }
                    }
                }
            }
        }
    }
}
