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
            link(rel = "stylesheet", href = "https://fonts.googleapis.com/css?family=Roboto:300,400,500,700&display=swap")
            link(rel = "stylesheet", href = "https://fonts.googleapis.com/icon?family=Material+Icons")
            link(rel = "stylesheet", href = "https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/css/materialize.min.css")
            style {
                +"""
                /* Base styles */
                :root {
                    --primary-color: #5d8aa8;
                    --primary-hover: #4a7a98;
                    --secondary-color: #7d9eb1;
                    --secondary-hover: #6a8ca1;
                    --text-color: #333;
                    --light-text: #666;
                    --lighter-text: #999;
                    --border-color: #e0e0e0;
                    --light-bg: #f5f7fa;
                    --container-bg: #fff;
                    --success-color: #66bb6a;
                    --warning-color: #ffa726;
                    --error-color: #ef5350;
                }

                * {
                    box-sizing: border-box;
                    margin: 0;
                    padding: 0;
                }

                body {
                    font-family: 'Roboto', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    line-height: 1.6;
                    color: var(--text-color);
                    background-color: var(--light-bg);
                    padding: 0;
                    margin: 0;
                }

                .container {
                    width: 100%;
                    max-width: 1200px;
                    margin: 0 auto;
                    padding: 0 20px;
                }

                /* Navigation */
                nav.nav-extended {
                    background-color: var(--primary-color);
                    margin-bottom: 20px;
                }

                nav .brand-logo {
                    font-size: 1.6rem;
                    font-weight: 500;
                    padding-left: 10px;
                }

                nav .brand-logo i {
                    margin-right: 8px;
                }

                nav ul li a {
                    font-weight: 500;
                    transition: background-color 0.3s;
                }

                nav ul li a:hover {
                    background-color: rgba(255,255,255,0.1);
                }

                .sidenav {
                    width: 280px;
                }

                .sidenav .user-view {
                    padding: 20px 16px 12px;
                }

                .sidenav .user-view .name {
                    font-size: 1.4rem;
                    font-weight: 500;
                    margin-top: 8px;
                    color: var(--primary-color);
                }

                .sidenav li > a {
                    display: flex;
                    align-items: center;
                    font-weight: 500;
                }

                .sidenav li > a > i {
                    margin-right: 16px;
                    color: var(--primary-color);
                }

                /* Main content */
                main {
                    padding: 2rem 0;
                }

                .content-card {
                    background-color: var(--container-bg);
                    border-radius: 8px;
                    box-shadow: 0 2px 10px rgba(0,0,0,0.05);
                    padding: 2rem;
                    margin-bottom: 2rem;
                }

                /* Typography */
                h1, h2, h3, h4, h5, h6 {
                    margin-bottom: 1rem;
                    line-height: 1.2;
                    color: var(--text-color);
                }

                h1 {
                    font-size: 2.2rem;
                    text-align: center;
                    margin-bottom: 1.5rem;
                }

                h2 {
                    font-size: 1.8rem;
                    margin-top: 1.5rem;
                }

                h3 {
                    font-size: 1.5rem;
                    margin-top: 1.2rem;
                }

                p {
                    margin-bottom: 1rem;
                }

                /* Forms */
                .form-group {
                    margin-bottom: 1.5rem;
                }

                label {
                    display: block;
                    margin-bottom: 0.5rem;
                    font-weight: 600;
                }

                input[type="text"],
                input[type="email"],
                input[type="tel"],
                input[type="number"],
                textarea,
                select {
                    width: 100%;
                    padding: 1.2rem;
                    border: none;
                    border-radius: 6px;
                    font-size: 2rem;
                    transition: all 0.3s;
                    margin-bottom: 1.2rem;
                    box-shadow: 0 1px 3px rgba(0,0,0,0.05);
                    background-color: white;
                }

                input[type="text"]:focus,
                input[type="email"]:focus,
                input[type="tel"]:focus,
                input[type="number"]:focus,
                textarea:focus,
                select:focus {
                    border-color: var(--primary-color);
                    outline: none;
                    box-shadow: 0 2px 8px rgba(93,138,168,0.2);
                }

                .required:after {
                    content: " *";
                    color: var(--error-color);
                }

                /* Buttons */
                .button, button {
                    display: inline-block;
                    background-color: var(--primary-color);
                    color: white;
                    padding: 0.9rem 1.8rem;
                    border: none;
                    border-radius: 4px;
                    font-size: 1.1rem;
                    cursor: pointer;
                    text-decoration: none;
                    transition: all 0.3s;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                    font-weight: 500;
                    box-shadow: 0 2px 5px rgba(0,0,0,0.2);
                    width: 100%;
                    text-align: center;
                }

                .button:hover, button:hover {
                    background-color: var(--primary-hover);
                    box-shadow: 0 4px 8px rgba(0,0,0,0.3);
                    transform: translateY(-2px);
                }

                .button-secondary {
                    background-color: var(--secondary-color);
                }

                .button-secondary:hover {
                    background-color: var(--secondary-hover);
                }

                /* Tables */
                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 1.5rem;
                }

                th, td {
                    padding: 0.75rem;
                    text-align: left;
                    border-bottom: 1px solid var(--border-color);
                }

                th {
                    background-color: var(--light-bg);
                    font-weight: 600;
                }

                tr:hover {
                    background-color: rgba(0,0,0,0.02);
                }

                /* Lists */
                ul, ol {
                    margin-bottom: 1rem;
                    padding-left: 1.5rem;
                }

                li {
                    margin-bottom: 0.5rem;
                }

                /* Utilities */
                .text-center {
                    text-align: center;
                }

                .mt-1 { margin-top: 0.5rem; }
                .mt-2 { margin-top: 1rem; }
                .mt-3 { margin-top: 1.5rem; }
                .mt-4 { margin-top: 2rem; }

                .mb-1 { margin-bottom: 0.5rem; }
                .mb-2 { margin-bottom: 1rem; }
                .mb-3 { margin-bottom: 1.5rem; }
                .mb-4 { margin-bottom: 2rem; }

                /* Footer */
                footer {
                    background-color: var(--text-color);
                    color: white;
                    padding: 2rem 0;
                    margin-top: 2rem;
                }

                footer a {
                    color: white;
                    text-decoration: none;
                }

                footer a:hover {
                    text-decoration: underline;
                }

                /* Responsive design */
                @media (max-width: 768px) {
                    .menu-toggle {
                        display: block;
                    }

                    nav ul {
                        display: none;
                        position: absolute;
                        top: 60px;
                        left: 0;
                        right: 0;
                        flex-direction: column;
                        background-color: var(--primary-color);
                        padding: 1rem;
                        box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                    }

                    nav ul.show {
                        display: flex;
                    }

                    nav ul li {
                        margin: 0.5rem 0;
                    }

                    .content-card {
                        padding: 1.5rem;
                    }

                    h1 {
                        font-size: 1.8rem;
                    }

                    h2 {
                        font-size: 1.5rem;
                    }

                    h3 {
                        font-size: 1.3rem;
                    }
                }

                @media (max-width: 480px) {
                    .container {
                        padding: 0 15px;
                    }

                    .content-card {
                        padding: 1rem;
                    }

                    h1 {
                        font-size: 1.6rem;
                    }

                    .button, button {
                        width: 100%;
                        text-align: center;
                        margin-bottom: 0.5rem;
                    }
                }
                """
            }
            script(src = "https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js") {}
            script {
                unsafe {
                    +"""
                    document.addEventListener('DOMContentLoaded', function() {
                        // Mobile menu toggle
                        const menuToggle = document.querySelector('.menu-toggle');
                        const navMenu = document.querySelector('nav ul');

                        if (menuToggle && navMenu) {
                            menuToggle.addEventListener('click', function() {
                                navMenu.classList.toggle('show');
                            });
                        }

                        // Initialize Materialize components
                        M.AutoInit();

                        // Enhance form elements
                        const inputs = document.querySelectorAll('input, textarea, select');
                        inputs.forEach(input => {
                            input.classList.add('browser-default');
                        });
                    });
                    """
                }
            }
        }
        body {
            if (showNavbar) {
                nav(classes = "nav-extended z-depth-1") {
                    div("nav-wrapper") {
                        div("container") {
                            a(href = "/", classes = "brand-logo") {
                                i("material-icons left") { +"sports_handball" }
                                +"Meldestelle Portal"
                            }
                            a(href = "#", classes = "sidenav-trigger") {
                                attributes["data-target"] = "mobile-nav"
                                i("material-icons") { +"menu" }
                            }
                            ul(classes = "right hide-on-med-and-down") {
                                li {
                                    a(href = "/") {
                                        i("material-icons left") { +"home" }
                                        +"Home"
                                    }
                                }
                                if (showAdminLink) {
                                    li {
                                        a(href = "/admin/tournaments") {
                                            i("material-icons left") { +"event" }
                                            +"Turnierverwaltung"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Mobile sidenav
                ul(classes = "sidenav") {
                    attributes["id"] = "mobile-nav"
                    li {
                        div("user-view") {
                            div("background blue-grey lighten-4") {
                                style = "height: 80px;"
                            }
                            span("name") { +"Meldestelle Portal" }
                        }
                    }
                    li {
                        a(href = "/") {
                            i("material-icons") { +"home" }
                            +"Home"
                        }
                    }
                    if (showAdminLink) {
                        li {
                            a(href = "/admin/tournaments") {
                                i("material-icons") { +"event" }
                                +"Turnierverwaltung"
                            }
                        }
                    }
                }
            }
            main {
                div("container") {
                    div("content-card") {
                        content()
                    }
                }
            }
            footer {
                div("container") {
                    div("text-center") {
                        p { +"© ${java.time.Year.now().value} Meldestelle Portal. Alle Rechte vorbehalten." }
                        p {
                            +"Entwickelt von "
                            a(href = "#") { +"mocode" }
                        }
                    }
                }
            }
        }
    }
}
