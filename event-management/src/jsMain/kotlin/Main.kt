import at.mocode.events.ui.components.VeranstaltungsListe
import react.create

/**
 * Main entry point for the JavaScript build.
 *
 * This function serves as the entry point for the Kotlin/JS application.
 * It registers the React component as a web component using r2wc.
 */
fun main() {
    console.log("Event Management JS module loaded successfully!")

    // Import r2wc function from @r2wc/react-to-web-component npm package
    val r2wc = js("require('@r2wc/react-to-web-component')")

    // Convert React component to Web Component using r2wc
    val VeranstaltungsListeWebComponent = r2wc(VeranstaltungsListe, js("{}"))

    // Register the new component with a custom HTML tag
    js("customElements.define('veranstaltungs-liste', arguments[0])")(VeranstaltungsListeWebComponent)

    console.log("Web component 'veranstaltungs-liste' registered successfully!")
    console.log("You can now use <veranstaltungs-liste></veranstaltungs-liste> in your HTML")
}
